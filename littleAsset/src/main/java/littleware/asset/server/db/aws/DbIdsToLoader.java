/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.Item;
import com.amazonaws.services.simpledb.model.SelectRequest;
import com.amazonaws.services.simpledb.model.SelectResult;
import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetType;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.base.validate.ValidationException;
import littleware.db.DbReader;

/**
 *
 * @author pasquini
 */
public class DbIdsToLoader implements DbReader<Set<UUID>, String> {

    private static final Logger log = Logger.getLogger(DbIdsToLoader.class.getName());
    private final AmazonSimpleDB db;
    private final AwsConfig config;
    private final String query;
    private final Option<AssetType> maybeType;

    public DbIdsToLoader(AmazonSimpleDB db, AwsConfig config, String query, Option<AssetType> maybeType) {
        this.db = db;
        this.config = config;
        this.query = query;
        this.maybeType = maybeType;
    }

    @Override
    public Set<UUID> loadObject(String ignore) throws SQLException {
        final ImmutableSet.Builder<UUID> builder = ImmutableSet.builder();
        try {
            // We shouldn't have to worry about multiple pages of results ...
            log.log(Level.FINE, "Running query: {0}", query);
            SelectResult result = db.select(new SelectRequest(query).withConsistentRead(Boolean.TRUE));
            while (true) {
                for (Item item : result.getItems()) {
                    UUID id = null;
                    AssetType type = null;

                    for (Attribute attr : item.getAttributes()) {
                        if (attr.getName().equals("id")) {
                            id = UUIDFactory.parseUUID(attr.getValue());
                        } else if (attr.getName().equals("typeId")) {
                            type = AssetType.getMember(UUIDFactory.parseUUID(attr.getValue()));
                        }
                    }
                    if ((null == id) || (null == type)) {
                        throw new AssertionFailedException("Unexpected query result");
                    }
                    // apply type filter
                    // Note: cheaper to filter here than to run an intersect query against asset-type
                    if (maybeType.isEmpty() || type.isA(maybeType.get())) {
                        builder.add(id);
                    }
                }
                if (null == result.getNextToken()) {
                    break;
                } else {
                    result = db.select(new SelectRequest(query).withConsistentRead(Boolean.TRUE).withNextToken(result.getNextToken()));
                }
            }
            return builder.build();
        } catch (Exception ex) {
            throw new SQLException("Failed query", ex);
        }
    }

    public static class Builder {

        private UUID toId = null;
        private Option<AssetType> maybeType = Maybe.empty();
        private final AmazonSimpleDB db;
        private final AwsConfig config;

        @Inject
        public Builder(AmazonSimpleDB db, AwsConfig config) {
            this.db = db;
            this.config = config;
        }

        public Builder toId(UUID value) {
            toId = value;
            return this;
        }

        public Builder type(AssetType value) {
            maybeType = Maybe.something(value);
            return this;
        }

        public Builder type(Option<AssetType> value) {
            maybeType = value;
            return this;
        }

        public DbIdsToLoader build() {
            ValidationException.validate(toId != null, "Must specify toId");
            final String query = "Select id, typeId From " + config.getDbDomain() + " Where `toId`='"
                    + UUIDFactory.makeCleanString(toId)
                    + "'";

            return new DbIdsToLoader(db, config, query, maybeType);
        }
    }
}

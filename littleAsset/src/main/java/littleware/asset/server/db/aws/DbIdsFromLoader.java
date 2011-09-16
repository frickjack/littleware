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
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetType;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.base.validate.ValidationException;
import littleware.db.DbReader;

/**
 *
 * @author pasquini
 */
public class DbIdsFromLoader implements DbReader<Map<String, UUID>, String> {

    private static final Logger log = Logger.getLogger(DbIdsFromLoader.class.getName());
    private final AmazonSimpleDB db;
    private final AwsConfig config;
    private final Collection<String> queryList;

    public DbIdsFromLoader(AmazonSimpleDB db, AwsConfig config, Collection<String> queryList) {
        this.db = db;
        this.config = config;
        this.queryList = queryList;
    }

    @Override
    public Map<String, UUID> loadObject(String ignore) throws SQLException {
        final ImmutableMap.Builder<String, UUID> builder = ImmutableMap.builder();
        try {
            // We shouldn't have to worry about multiple pages of results ...
            for (String query : queryList) {
                log.log(Level.FINE, "Running query: {0}", query);
                final SelectResult result = db.select(new SelectRequest(query).withConsistentRead(Boolean.TRUE));
                Whatever.get().check("Query overflow ...", null == result.getNextToken());
                for (Item item : result.getItems()) {
                    String name = null;
                    UUID id = null;

                    for (Attribute attr : item.getAttributes()) {
                        if (attr.getName().equals("id")) {
                            id = UUIDFactory.parseUUID(attr.getValue());
                        } else {
                            name = attr.getValue();
                        }
                    }
                    if ((null == name) || (null == id)) {
                        throw new AssertionFailedException("Unexpected query result");
                    }
                    builder.put(name, id);
                }
            }
            return builder.build();
        } catch (Exception ex) {
            throw new SQLException("Failed fromId query", ex);
        }
    }

    public static class Builder {

        private UUID fromId = null;
        private Option<AssetType> maybeType = Maybe.empty();
        private Option<Integer> maybeState = Maybe.empty();
        private final AmazonSimpleDB db;
        private final AwsConfig config;

        @Inject
        public Builder(AmazonSimpleDB db, AwsConfig config) {
            this.db = db;
            this.config = config;
        }

        public Builder fromId(UUID value) {
            fromId = value;
            return this;
        }

        public Builder type(AssetType value) {
            maybeType = Maybe.emptyIfNull(value);
            return this;
        }

        public Builder state(Integer value) {
            maybeState = Maybe.emptyIfNull(value);
            return this;
        }

        public Builder type(Option<AssetType> value) {
            maybeType = value;
            return this;
        }

        public Builder state(Option<Integer> value) {
            maybeState = value;
            return this;
        }

        public DbIdsFromLoader build() {
            ValidationException.validate(fromId != null, "Must specify fromId");
            String query = "Select id, name From " + config.getDbDomain() + " Where `fromId`='"
                    + UUIDFactory.makeCleanString(fromId)
                    + "'";
            for (Integer state : maybeState) {
                query += " intersection (`state`='" + DbAssetSaver.encodeState(state) + "')";
            }
            final List<String> queryList = new ArrayList<String>();
            if (maybeType.isEmpty()) {
                queryList.add(query);
            } else {
                final AssetType baseType = maybeType.get();
                final Set<AssetType> typeSet = new HashSet<AssetType>( AssetType.getSubtypes(baseType) );
                typeSet.add(baseType);
                
                for (AssetType type : typeSet ) {
                    queryList.add(query + " intersection `typeId`='" + UUIDFactory.makeCleanString(type.getObjectId()) + "'");
                }
            }

            return new DbIdsFromLoader(db, config, queryList);
        }
    }
}

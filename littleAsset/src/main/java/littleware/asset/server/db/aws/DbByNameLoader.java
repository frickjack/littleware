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
import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
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
public class DbByNameLoader implements DbReader<Option<Asset>, String> {

    private static final Logger log = Logger.getLogger(DbByNameLoader.class.getName());
    private final AmazonSimpleDB db;
    private final AwsConfig config;
    private final DbAssetLoader loader;
    private final Collection<String> queryList;

    public DbByNameLoader(AmazonSimpleDB db, AwsConfig config, DbAssetLoader loader,
            Collection<String> queryList
            ) {
        this.db = db;
        this.config = config;
        this.loader = loader;
        this.queryList = queryList;
    }

    @Override
    public Option<Asset> loadObject(String ignore) throws SQLException {
        try {
            // We shouldn't have to worry about multiple pages of results ...
            for (String query : queryList) {
                log.log(Level.FINE, "Running query: {0}", query);
                final SelectResult result = db.select(new SelectRequest(query).withConsistentRead(Boolean.TRUE));
                Whatever.get().check("Query overflow ...", null == result.getNextToken());
                for (Item item : result.getItems()) {
                    UUID id = null;

                    for (Attribute attr : item.getAttributes()) {
                        if (attr.getName().equals("id")) {
                            id = UUIDFactory.parseUUID(attr.getValue());
                            break;
                        } 
                    }
                    if ( null == id ) {
                        throw new AssertionFailedException("Unexpected query result");
                    }
                    return Maybe.emptyIfNull( loader.loadObject(id) );
                }
            }
            return Maybe.empty();
        } catch (Exception ex) {
            throw new SQLException("Failed query", ex);
        }
    }

    public static class Builder {

        private AssetType type = null;
        private String name = null;
        private final AmazonSimpleDB db;
        private final AwsConfig config;
        private final DbAssetLoader loader;

        @Inject
        public Builder(AmazonSimpleDB db, AwsConfig config, DbAssetLoader loader ) {
            this.db = db;
            this.config = config;
            this.loader = loader;
        }

        public Builder name(String value) {
            name = value;
            return this;
        }

        public Builder type(AssetType value) {
            type = value;
            return this;
        }



        public DbByNameLoader build() {
            ValidationException.validate(name != null, "Must specify name");
            ValidationException.validate(type != null, "Must specify asset type");
            final String query = "Select id From " + config.getDbDomain() + " Where `name`='"
                    + name
                    + "'";
            final List<String> queryList = new ArrayList<String>();
            {
                final Set<AssetType> typeSet = new HashSet<AssetType>( AssetType.getSubtypes(type) );
                typeSet.add(type);
                
                for (AssetType t : typeSet ) {
                    queryList.add(query + " intersection `typeId`='" + UUIDFactory.makeCleanString(t.getObjectId()) + "'");
                }
            }

            return new DbByNameLoader(db, config, loader, queryList);
        }
    }
}

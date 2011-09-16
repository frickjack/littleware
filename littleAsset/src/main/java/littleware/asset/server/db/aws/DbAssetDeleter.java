/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.google.inject.Inject;
import java.sql.SQLException;
import littleware.asset.Asset;
import littleware.base.UUIDFactory;
import littleware.db.DbWriter;

public class DbAssetDeleter implements DbWriter<Asset> {
    private final AmazonSimpleDB db;
    private final AwsConfig config;

    @Inject
    public DbAssetDeleter(AmazonSimpleDB db, AwsConfig config) {
        this.db = db;
        this.config = config;
    }

    @Override
    public void saveObject(Asset asset) throws SQLException {
        try {
            db.deleteAttributes( new DeleteAttributesRequest( config.getDbDomain(), UUIDFactory.makeCleanString( asset.getId() ) ) );
        } catch ( Exception ex ) {
            throw new SQLException( "Failed asset delete", ex );
        }
    }
    
}

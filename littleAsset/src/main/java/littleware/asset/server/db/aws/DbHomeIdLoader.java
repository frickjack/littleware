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
import java.util.Map;
import java.util.UUID;
import littleware.asset.LittleHome;
import littleware.base.AssertionFailedException;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.db.DbReader;

/**
 *
 * @author pasquini
 */
public class DbHomeIdLoader implements DbReader<Map<String, UUID>, String> {
    private final AmazonSimpleDB db;
    private final AwsConfig config;
    private final String query;

    @Inject
    public DbHomeIdLoader(AmazonSimpleDB db, AwsConfig config) {
        this.db = db;
        this.config = config;
        this.query = "Select id, name From " + config.getDbDomain() + " Where typeId='" + 
            UUIDFactory.makeCleanString(LittleHome.HOME_TYPE.getObjectId() )
            + "'";
    }

    @Override
    public Map<String, UUID> loadObject(String ignore ) throws SQLException {
        final ImmutableMap.Builder<String,UUID> builder = ImmutableMap.builder();
        try {
            // We shouldn't have to worry about multiple pages of results ...
            final SelectResult result = db.select( new SelectRequest( query ).withConsistentRead(Boolean.TRUE) );
            Whatever.get().check( "Query overflow ...", null == result.getNextToken() );
            for( Item item : result.getItems() ) {
                String name = null;
                UUID   id = null;
                
                for( Attribute attr : item.getAttributes() ) {
                    if ( attr.getName().equals( "id" ) ) {
                        id = UUIDFactory.parseUUID(attr.getValue() );
                    } else {
                        name = attr.getValue();
                    }
                }
                if ( (null == name) || (null == id ) ) {
                    throw new AssertionFailedException( "Unexpected query result" );
                }
                builder.put(name, id);
            }        
            return builder.build();
        } catch ( Exception ex ) {
            throw new SQLException( "Failed homeId query", ex );
        }
    }
    
}

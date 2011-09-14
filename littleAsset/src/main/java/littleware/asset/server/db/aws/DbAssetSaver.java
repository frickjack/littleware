/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.spi.AbstractAsset;
import littleware.base.UUIDFactory;
import littleware.db.DbWriter;



public class DbAssetSaver implements DbWriter<Asset> {
    private final AmazonSimpleDB db;

    public DbAssetSaver( AmazonSimpleDB db ) {
        this.db = db;
    }
    
    /**
     * Add the given id to the attrList with the given attribute name in a clean UUID format if it is not null
     * 
     * @return attrList
     */
    public static void  addAttribute( List<ReplaceableAttribute> attrList, String name, UUID value ) {
        if ( null != value ) {
            attrList.add( new ReplaceableAttribute( name, UUIDFactory.makeCleanString( value ), true ) );
        }
    }
    
    public static ReplaceableItem  assetToItem( AbstractAsset asset ) {
        final List<ReplaceableAttribute> attrList = new ArrayList<ReplaceableAttribute>();
        addAttribute( attrList, "id", asset.getId() );
        addAttribute( attrList, "toId", asset.getToId() );
        addAttribute( attrList, "fromId", asset.getFromId() );
        addAttribute( attrList, "ownerId", asset.getOwnerId() );
        addAttribute( attrList, "creatorId", asset.getCreatorId() );
        addAttribute( attrList, "aclId", asset.getAclId() );
        return null;
    }
    
    
    @Override
    public void saveObject( Asset asset ) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }
    
}

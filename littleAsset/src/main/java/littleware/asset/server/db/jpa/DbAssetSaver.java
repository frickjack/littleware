/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.jpa;

import com.google.inject.Inject;
import java.sql.SQLException;
import javax.persistence.EntityManager;
import littleware.asset.Asset;
import littleware.base.UUIDFactory;
import littleware.db.DbWriter;

/**
 * Simple asset saver.  Does not attempt to maintain history table.
 */
public class DbAssetSaver implements DbWriter<Asset> {
    private final JpaLittleTransaction trans;

    @Inject
    public DbAssetSaver( JpaLittleTransaction trans ) {
        this.trans = trans;
    }


    @Override
    public void saveObject(Asset asset) throws SQLException {
        if ( asset.getTimestamp() != trans.getTimestamp() ) {
            throw new IllegalStateException( "Transaction not established" );
        }
        final EntityManager entMgr = trans.getEntityManager();
        AssetEntity entity = entMgr.find( AssetEntity.class,
                UUIDFactory.makeCleanString( asset.getId() )
                );
        if ( null == entity ) {
            entity = AssetEntity.buildEntity(asset);
            entMgr.merge( entity );
        } else {
            entity.filter( asset );
        }
    }

}

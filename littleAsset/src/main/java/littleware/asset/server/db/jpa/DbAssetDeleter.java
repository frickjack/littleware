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
 * Remove an asset from the database
 */
public class DbAssetDeleter implements DbWriter<Asset> {
    private final JpaLittleTransaction trans;
    

    @Inject
    public DbAssetDeleter( JpaLittleTransaction trans ) {
        this.trans = trans;
    }


    @Override
    public void saveObject(Asset asset) throws SQLException {
        final EntityManager entMgr = trans.getEntityManager();
        final AssetEntity entity = entMgr.find( AssetEntity.class, UUIDFactory.makeCleanString( asset.getId() ) );
        entMgr.remove(entity);
    }

}

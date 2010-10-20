/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.jpa;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.SQLException;
import java.util.UUID;
import javax.persistence.EntityManager;
import littleware.asset.Asset;
import littleware.base.UUIDFactory;
import littleware.db.DbWriter;

/**
 * Remove an asset from the database
 */
public class DbAssetDeleter implements DbWriter<Asset> {
    private Provider<JpaLittleTransaction> oprovideTrans;

    @Inject
    public DbAssetDeleter( Provider<JpaLittleTransaction> provideTrans ) {
        oprovideTrans = provideTrans;
    }


    @Override
    public void saveObject(Asset asset) throws SQLException {
        final EntityManager entMgr = oprovideTrans.get().getEntityManager();
        final AssetEntity entity = entMgr.find( AssetEntity.class, UUIDFactory.makeCleanString( asset.getId() ) );
        entMgr.remove(entity);
    }

}

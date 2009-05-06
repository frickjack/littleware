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
import javax.persistence.EntityManager;
import littleware.asset.Asset;
import littleware.db.DbWriter;

/**
 * Simple asset saver.  Does not attempt to maintain history table.
 */
public class DbAssetSaver implements DbWriter<Asset> {
    private final Provider<JpaLittleTransaction> oprovideTrans;

    @Inject
    public DbAssetSaver( Provider<JpaLittleTransaction> provideTrans ) {
        oprovideTrans = provideTrans;
    }


    @Override
    public void saveObject(Asset asset) throws SQLException {
        final EntityManager entMgr = oprovideTrans.get().getEntityManager();
        final TransactionEntity trans = entMgr.find( TransactionEntity.class, new Integer(0) );
        trans.setTransaction( trans.getTransaction() + 1 );
        asset.setTransactionCount( trans.getTransaction() );
        final AssetEntity entity = AssetEntity.buildEntity(asset);
        entMgr.merge( entity );
    }

}

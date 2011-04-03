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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.persistence.EntityManager;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;

/**
 *
 * @author pasquini
 */
public class DbAssetLoader implements DbReader<Asset, UUID> {
    private static final Logger olog = Logger.getLogger( DbAssetLoader.class.getName() );
    private Provider<JpaLittleTransaction> oprovideTrans;

    @Inject
    public DbAssetLoader( Provider<JpaLittleTransaction> provideTrans ) {
        oprovideTrans = provideTrans;
    }

    @Override
    public Asset loadObject(UUID id) throws SQLException {
        final EntityManager entMgr = oprovideTrans.get().getEntityManager();
        final AssetEntity ent = entMgr.find( AssetEntity.class,
                UUIDFactory.makeCleanString(id)
                );
        try {
            if ( null != ent ) {
                return ent.buildAsset();
            } else {
                return null;
            }
        } catch (AssetException ex) {
            throw new SQLException( "Failed data load for " + id, ex );
        }
    }

}

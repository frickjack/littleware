/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.postgres;

import littleware.asset.server.CacheManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.name.Named;
import java.util.*;
import java.sql.SQLException;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.asset.server.JdbcTransaction;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.*;
import littleware.db.*;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;

/**
 * Postgres RDBMS DbAssetManager implementation
 */
public class DbAssetPostgresManager implements DbAssetManager {

    private static final Logger olog_generic = Logger.getLogger(DbAssetPostgresManager.class.getName());
    private int oi_client_id = 1;
    private final Provider<JdbcTransaction> oprovideTrans;

    /**
     * Inject DataSource dependency
     */
    @Inject
    public DbAssetPostgresManager(@Named("int.database_client_id") int i_client_id,
            Provider<JdbcTransaction> provideTrans) {
        oi_client_id = i_client_id;
        oprovideTrans = provideTrans;
    }

    @Override
    public DbWriter<Asset> makeDbAssetSaver() {
        return new DbAssetSaver(getClientId(), oprovideTrans);
    }

    @Override
    public DbReader<Asset, UUID> makeDbAssetLoader() {
        return new DbAssetLoader(getClientId(), oprovideTrans);
    }

    @Override
    public DbWriter<Asset> makeDbAssetDeleter() {
        return new DbAssetDeleter(getClientId(), oprovideTrans);
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader() {
        return new DbHomeIdLoader(getClientId(), oprovideTrans);
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(UUID uFrom, Maybe<AssetType> maybeType, Maybe<Integer> maybeState) {
        throw new UnsupportedOperationException("This module is out of data - use JPA");
        //return new DbChildIdLoader ( uFrom, maybeType.getOr(null), getClientId (), oprovideTrans );
    }

    @Override
    public DbReader<Set<Asset>, String> makeDbAssetsByNameLoader(String s_name, AssetType n_type) {
        return new DbAssetsByNameLoader(s_name, n_type, getClientId(), oprovideTrans);
    }

    @Override
    public DbWriter<String> makeDbCacheSyncClearer() {
        return new DbCacheSyncClearer(getClientId(), oprovideTrans);
    }

    @Override
    public DbReader<Map<UUID, Asset>, String> makeDbCacheSyncLoader() {
        return new DbCacheSyncLoader(getClientId(), oprovideTrans);
    }
    private static boolean ob_cachesync = false;

    /**
     * TODO - split this out as an OSGi service
     *
     * @param m_cache
     * @throws java.sql.SQLException
     */
    @Override
    public void launchCacheSyncThread(
            final CacheManager m_cache) throws SQLException {
        if (ob_cachesync) {
            throw new AssertionFailedException("Cache sync relaunch attempted");
        }

        DbWriter<String> db_clear = makeDbCacheSyncClearer();

        // Clear out old data
        db_clear.saveObject(null);
        // Setup a thread to keep us in sync
        Runnable run_sync = new Runnable() {

            @Override
            public void run() {
                Date t_last_error = new Date(0);
                final DbReader<Map<UUID, Asset>, String> db_sync = makeDbCacheSyncLoader();
                final LittleTransaction trans_save = oprovideTrans.get();
                while (true) {
                    try {
                        trans_save.startDbAccess();
                        Map<UUID, Asset> v_data = db_sync.loadObject(null);
                        for (Map.Entry<UUID, Asset> x_entry : v_data.entrySet()) {
                            Asset a_update = x_entry.getValue();
                            if (null != a_update) {
                                // Verify transaction
                                Asset a_old = (Asset) m_cache.get(x_entry.getKey());
                                if ((a_old == null) || (a_update.getTransactionCount() >= a_old.getTransactionCount())) {
                                    m_cache.put(x_entry.getKey(), a_update);
                                } else {
                                    olog_generic.log(Level.INFO, "Sync thread not updating cache for asset: " + a_old);
                                }
                            } else {
                                m_cache.remove(x_entry.getKey());
                            }
                        }
                    } catch (Exception e) {
                        Date t_now = new Date();

                        if (t_now.getTime() > t_last_error.getTime() + 60000) {
                            olog_generic.log(Level.WARNING, "Sync thread failed to sync cache, caught: " + e, e);
                            t_last_error = t_now;
                        }
                    } finally {
                        trans_save.endDbAccess();
                    }
                    try {
                        Thread.sleep(500);
                    } catch (InterruptedException e) {
                        olog_generic.log(Level.INFO, "Ignoring InterruptedException: " + e);
                    }
                }
            }
        };

        new Thread(run_sync).start();
        ob_cachesync = true;
    }

    @Override
    public int getClientId() {
        return oi_client_id;
    }

    @Override
    public void setClientId(int i_id) {
        oi_client_id = i_id;
    }

    @Override
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(UUID u_to, AssetType n_type) {
        return new DbAssetIdsToLoader(u_to, n_type, getClientId(), oprovideTrans);
    }
}

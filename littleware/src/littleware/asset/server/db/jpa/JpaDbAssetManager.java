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
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.transaction.TransactionManager;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.server.CacheManager;
import littleware.asset.server.db.DbAssetManager;
import littleware.db.DbReader;
import littleware.db.DbWriter;

/**
 * JPA implementation of DbAssetManager
 */
public class JpaDbAssetManager implements DbAssetManager {
    private static final Logger      olog = Logger.getLogger( JpaDbAssetManager.class.getName() );
    private final Provider<DbAssetLoader> oprovideLoader;
    private final Provider<DbAssetSaver>  oprovideSaver;
    private final Provider<DbAssetDeleter> oprovideDeleter;

    @Inject
    public JpaDbAssetManager (
            Provider<DbAssetLoader> provideLoader,
            Provider<DbAssetSaver>  provideSaver,
            Provider<DbAssetDeleter>  provideDeleter
            )
    {
        oprovideLoader = provideLoader;
        oprovideSaver = provideSaver;
        oprovideDeleter = provideDeleter;
    }
    
    @Override
    public int getClientId() {
        return 0;
    }

    @Override
    public void setClientId(int i_id) {
        olog.log( Level.INFO, "JpaDbAssetManager ignores clientId property" );
    }

    @Override
    public DbWriter<Asset> makeDbAssetSaver() {
        return oprovideSaver.get();
    }

    @Override
    public DbReader<Asset, UUID> makeDbAssetLoader() {
        return oprovideLoader.get();
    }

    @Override
    public DbWriter<Asset> makeDbAssetDeleter() {
        return oprovideDeleter.get();
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(UUID u_from, AssetType n_child_type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(UUID u_from, AssetType n_child_type) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Set<Asset>, String> makeDbAssetsByNameLoader(String s_name, AssetType n_type, UUID u_home) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbWriter<String> makeDbCacheSyncClearer() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public DbReader<Map<UUID, Asset>, String> makeDbCacheSyncLoader() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void launchCacheSyncThread(CacheManager m_cache) throws SQLException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

}

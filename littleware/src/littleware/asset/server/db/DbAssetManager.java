package littleware.asset.server.db;

import java.util.*;
import java.sql.SQLException;
import javax.sql.DataSource;

import littleware.asset.*;
import littleware.db.*;

/**
 * Interface abstracts away littleware.asset db interactions.
 * TODO: ADD TRANSACTION SUPPORT
 */
public interface DbAssetManager {

    /**
     * Get the client id that this manager is
     * supplying to the database for 
     * database backend based cache synchronization.
     */
    public int getClientId();

    /**
     * Set the database client-id.  Intended only for testing -
     * normally the client-id gets setup at startup time.
     */
    public void setClientId(int i_id);

    /**
     * Create asset-creator handler
     */
    public DbWriter<Asset> makeDbAssetSaver();

    /**
     * Create asset-loader handler
     */
    public DbReader<Asset, UUID> makeDbAssetLoader();

    /**
     * Create asset-deleter handler
     */
    public DbWriter<Asset> makeDbAssetDeleter();

    /**
     * Create handler to load our home-asset ids
     */
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader();

    /**
     * Create handler to load the ids of assets linking FROM a given asset id
     */
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(UUID u_from, AssetType n_child_type);

    /**
     * Create handler to load the ids of assets linking TO a given asset id
     */
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader(UUID u_from, AssetType n_child_type);

    /**
     * Create assets by name loader handler
     */
    public DbReader<Set<Asset>, String> makeDbAssetsByNameLoader(String s_name, AssetType n_type, UUID u_home);

    /**
     * Create database cache-data clearer.  Want to do this once at boot time.
     * Expose this mostly for testing - usually just want to invoke
     * {@link littleware.asset.db.DbAssetManager launchCacheCheckThread}
     * to sync the local cache with changes from other db clients.
     *
     * @return DbWriter that clears the cache for the registered source -
     *                   String parameter is ignored.
     */
    public DbWriter<String> makeDbCacheSyncClearer();

    /**
     * Create database cache-sync loader.  
     * Returns mapping of asset-ids that other clients have changed
     * since the last time the cache-sync loader was called.
     * Loading the data also clears out the changes from the db -
     * code assumes you don't want to load the same changes in
     * subsequent calls.
     * Expose this mostly for testing - usually just want to invoke
     * {@link littleware.asset.db.DbAssetManager launchCacheCheckThread}
     * to sync the local cache with changes from other db clients.
     *
     * @return DbReader that loads and clears the cache-changes for the registered source -
     *                   String parameter is ignored.
     */
    public DbReader<Map<UUID, Asset>, String> makeDbCacheSyncLoader();

    /**
     * Call only once.  Launch a background thread to 
     * periodically sync the given Cache manager with
     * changes by other clients in the database.
     *
     * @param sql_pool source of db connections to access the cache-tracking
     *              tables in the backend DB
     * @param m_cache to update with the cache data pulled from the database
     * @exception SQLException on failure to perform initial sync with cache-data database
     */
    public void launchCacheSyncThread(CacheManager m_cache) throws SQLException;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


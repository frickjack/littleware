package littleware.asset.server.db;

import java.util.*;

import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.db.*;

/**
 * Factory interface for cache database command-object creation.
 * Note that the results of cache-queries are only valid if
 * the query-result has been stashed in the cache by a previous query.
 */
public interface DbCacheManager {

    /**
     * Create handler to load the cached result of the
     *    AssertRetriever.getDbAssetIdsFrom
     * call with the given argument.
     *
     * @return reader or null if the requested query has not yet been cached
     */
    public JdbcDbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(UUID u_from, AssetType n_type);

    /**
     * Create handler to save child-id data
     */
    public JdbcDbWriter<Map<String, UUID>> makeDbAssetIdsFromSaver(UUID u_from, AssetType n_type);

    /**
     * Create handler to load the cached result of the
     *    AssetSearchManager.getDbAssetIdsTo
     * call with the given arguments.
     *
     * @return handler or null if data not available in cache
     */
    public JdbcDbReader<Set<UUID>, String> makeDbAssetIdsToLoader(UUID u_to, AssetType n_type);

    /**
     * Create handler to save AssetSearchManager.getDbAssetIdsTo data
     */
    public JdbcDbWriter<Set<UUID>> makeDbAssetIdsToSaver(UUID u_to, AssetType n_type);

    /** 
     * Create a handler to load the home asset id mapping 
     *
     * @return handler, or null if data not yet available in cache
     */
    public JdbcDbReader<Map<String, UUID>, String> makeDbHomeIdsLoader();

    /**
     * Create handler to save home-id data
     */
    public JdbcDbWriter<Map<String, UUID>> makeDbHomeIdsSaver();

    /**
     * Create a handler to update the query-cache when updated
     * asset data becomes available.
     * The handler does not perform an update for an Asset if the
     * cache already contains data for the Asset with equal or greater Transaction count.
     */
    public JdbcDbWriter<Asset> makeDbAssetSaver();

    /**
     * Create a handler to remove deleted-asset data from the query database.
     * If the DbEraser is passed a 'null' asset UUID, then all the
     * data is earsed out of the database, and the query-log is cleared.
     */
    public JdbcDbWriter<UUID> makeDbEraser();

    /**
     * Create a handler to save asset-by-name query data
     */
    public JdbcDbWriter<Set<Asset>> makeDbAssetsByNameSaver(String s_name, AssetType n_type, UUID u_home);

    /**
     * Create a handler to save asset-by-name query data
     *
     * @return handler or null if data not yet available in cache
     */
    public JdbcDbReader<Set<UUID>, String> makeDbAssetsByNameLoader(String s_name, AssetType n_type,
            UUID u_home);
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.asset.db;

import java.util.*;

import littleware.asset.*;
import littleware.asset.LittleTransaction;
import littleware.db.*;

/**
 * Factory interface for data storage CRUD handlers
 */
public interface DbCommandManager {

    /**
     * Create asset-creator handler
     */
    public DbWriter<Asset> makeDbAssetSaver( LittleTransaction trans );

    /**
     * Create asset-loader handler
     */
    public DbReader<Asset, UUID> makeDbAssetLoader( LittleTransaction trans );

    /**
     * Create asset-deleter handler
     */
    public DbWriter<Asset> makeDbAssetDeleter( LittleTransaction trans );

    /**
     * Create handler to load our home-asset ids
     */
    public DbReader<Map<String, UUID>, String> makeDbHomeIdLoader( LittleTransaction trans );

    /**
     * Create handler to load the ids of assets linking FROM a given asset id
     */
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader( LittleTransaction trans, UUID fromId, Optional<AssetType> maybeType, Optional<Integer> maybeState);

    /**
     * Create handler to load the ids of assets linking TO a given asset id
     */
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader( LittleTransaction trans, UUID toId, Optional<AssetType> assetType);

    /**
     * Create assets by name loader handler
     */
    public DbReader<Optional<Asset>, String> makeDbAssetsByNameLoader( LittleTransaction trans, String name, AssetType assetType );
    
    /**
     * Asset loader given parent id and asset name
     */
    public DbReader<Optional<Asset>, String> makeDbAssetByParentLoader(LittleTransaction trans, String name, UUID parentId);

}


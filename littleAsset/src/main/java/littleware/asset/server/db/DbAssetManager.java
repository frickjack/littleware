/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db;

import java.util.*;

import littleware.asset.*;
import littleware.asset.server.LittleTransaction;
import littleware.base.Option;
import littleware.db.*;

/**
 * Factory interface for data storage CRUD handlers
 */
public interface DbAssetManager {

    /**
     * Create asset-creator handler
     */
    public DbWriter<Asset> makeDbAssetSaver( LittleTransaction trans );

    /**
     * Create asset-type saver for the AssetTypeActivator
     */
    public DbWriter<AssetType> makeTypeChecker( LittleTransaction trans );

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
    public DbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader( LittleTransaction trans, UUID fromId, Option<AssetType> maybeType, Option<Integer> maybeState);

    /**
     * Create handler to load the ids of assets linking TO a given asset id
     */
    public DbReader<Set<UUID>, String> makeDbAssetIdsToLoader( LittleTransaction trans, UUID toId, Option<AssetType> assetType);

    /**
     * Create assets by name loader handler
     */
    public DbReader<Option<Asset>, String> makeDbAssetsByNameLoader( LittleTransaction trans, String name, AssetType assetType );


    /**
     * Transaction log loader
     */
    public DbReader<List<IdWithClock>, Long> makeLogLoader( LittleTransaction trans, UUID homeId );
}


/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client;

import java.util.UUID;

import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;


/**
 * In-memory asset model cache.
 * Helps different parts of a UI to share the same asset-model data,
 * and communicate updates/etc.
 * May work in conjunction with a cache-update thread
 * that periodically syncs with the backend littleware asset repository.
 * Provides the only means for creating an AssetRef.
 * Update the library via the syncAsset method.
 */
public interface AssetLibrary {
    public AssetRef  getById( UUID id );

    /**
     * Lookup an asset by name.
     * 
     * @param name of asset to lookup
     * @param atype of asset to lookup - must be a name-unique asset type
     * @return reference to cached asset - empty if not cached
     * @throws InvalidAssetTypeException if atype is not name-unique
     */
    public AssetRef getByName( String name, AssetType atype
            ) throws InvalidAssetTypeException;
    

    /**
     * If an AssetRef is already in the repository,
     * then invoke AssetRef.syncAsset, otherwise
     * create an AssetRef for the given asset, and add it to the
     * repository 
     * This is the AssetRef factory method.
     *
     * @param asset to sync into the repository
     * @return AssetRef in the repository wrapping a_new or the AssetRef already in the
     *             repository if it has a newer transaction-count.
     */
    public AssetRef syncAsset ( Asset asset );
    
    
    /**
     * Notify the library that the given asset has been deleted
     * from the littleware asset repository.
     * Remove the asset from the library, and fire an Operation.assetDeleted AssetModelEvent
     * to listeners.
     * The AssetLibrary implementation should implement a ServiceListener
     * interface and listen for asset-delete events so that client code
     * should not have to explicity invoke this method.
     *
     * @param id of asset that has been deleted
     */
    public void assetDeleted ( UUID id );
}



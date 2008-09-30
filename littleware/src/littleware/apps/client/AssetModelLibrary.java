package littleware.apps.client;

import com.google.inject.ImplementedBy;
import java.util.UUID;
import java.util.Collection;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.InvalidAssetTypeException;
import littleware.base.BaseException;
import littleware.base.Cache;


/**
 * In-memory asset model cache.
 * Helps different parts of a UI to share the same asset-model data,
 * and communicate updates/etc.
 * May work in conjunction with a cache-update thread
 * that periodically syncs with the backend littleware asset repository.
 * Provides the only means for creating an AssetModel.
 * The put/get methods put/get copies of the cached AssetModel,
 * so that a client can change his object withouth corrupting the
 * cache.  The cache is updated via the syncAsset method.
 */
@ImplementedBy(SimpleAssetModelLibrary.class)
public interface AssetModelLibrary extends Cache<UUID,AssetModel> {
    /**
     * Lookup an asset by name.
     * 
     * @param s_name of asset to lookup
     * @param atype of asset to lookup - must be a name-unique asset type
     * @return cached asset-model or null
     * @exception InvalidAssetTypeException if atype is not name-unique
     */
    public AssetModel getByName( String s_name, AssetType<? extends Asset> atype
            ) throws InvalidAssetTypeException;
    
    /**
     * Retrieve asset by name - use m_search to access the asset
     * repository of the requested asset is not in cache.
     * 
     * @param s_name
     * @param atype
     * @param m_search
     * @return asset-model from cache first, then try repository, finally
     *                  return null if no match
     * @throws littleware.asset.InvalidAssetTypeException
     * @throws littleware.base.BaseException
     * @throws littleware.asset.AssetException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     */
    public AssetModel getByName( String s_name, AssetType<? extends Asset> atype,
            AssetSearchManager m_search
            ) throws InvalidAssetTypeException,
        BaseException, 
        AssetException, GeneralSecurityException, RemoteException;

    /**
     * If an AssetModel is already in the repostory,
     * then invoke AssetModel.syncAsset, otherwise
     * create an AssetModel for the given asset, and add it to the
     * repository 
     * This is the AssetModel factory method.
     *
     * @param a_new asset to sync into the repository
     * @return AssetModel in the repository wrapping a_new or the AssetModel already in the
     *             repository if it has a newer transaction-count.
     */
    public AssetModel syncAsset ( Asset a_new );
    
    /**
     * Convenience method - retrieves the asset from the library if
     * present, otherwise uses the AssetRetriever to retrieve the asset,
     * and sync it into the library.
     *
     * @param u_id of asset to retrieve AssetModel for
     * @return the asset or NULL if the asset does not exist
     * @param m_retriever to access the repository with if necessary
     * @exception BaseException may be thrown by AssetRetriever.getAssetOrNull
     * @exception GeneralSecurityException may be thrown by AssetRetriever.getAssetOrNull
     * @exception AssetException may be thrown by AssetRetriever.getAssetOrNull
     * @exception RemoteException may be thrown by AssetRetriever.getAssetOrNull
     */
    public AssetModel retrieveAssetModel ( UUID u_id, AssetRetriever m_retriever ) throws BaseException, 
        AssetException, GeneralSecurityException, RemoteException;
    
    /**
     * Convenience method - retrieves the asset from the library if
     * present, otherwise uses the AssetSearchManager to retrieve the asset,
     * and sync it into the library.
     *
     * @param s_name of asset to retrieve AssetModel for
     * @param n_type of asset to retrieve AssetModel for - must by a name-unique asset
     * @return the asset or NULL if the asset does not exist
     * @param m_search to access the repository with if necessary - via getByName
     * @exception BaseException may be thrown by AssetRetriever.getAssetOrNull
     * @exception GeneralSecurityException may be thrown by AssetRetriever.getAssetOrNull
     * @exception AssetException may be thrown by AssetRetriever.getAssetOrNull
     * @exception RemoteException may be thrown by AssetRetriever.getAssetOrNull
     *
     * TODO: fill this in
     *
    public AssetModel retrieveAssetModel ( String s_name, AssetType n_type, 
                                           AssetSearchManager m_search ) throws BaseException, 
        AssetException, GeneralSecurityException, RemoteException;
    */
    
    /**
     * Convenience method to sync the results of a repository search
     * with the in-memory library 
     *
     * @param v_assets collection of assets to sync
     * @return the collection of synced AssetModels
     */
    public Collection<AssetModel> syncAsset ( Collection<? extends Asset> v_assets );
    
    /**
     * Notify the library that the given asset has been deleted
     * from the littleware asset repository.
     * Remove the asset from the library, and fire an Operation.assetDeleted AssetModelEvent
     * to listeners.
     *
     * @param u_deleted id of asset that has been deleted
     * @return the AssetModel removed from the library or null if not in library
     */
    public AssetModel assetDeleted ( UUID u_deleted );
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


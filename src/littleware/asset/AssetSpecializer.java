package littleware.asset;

import java.rmi.RemoteException;
import java.util.Map;
import java.util.UUID;
import java.security.GeneralSecurityException;

import littleware.base.*;
import littleware.security.AccessDeniedException;

/**
 * Interface for managers of different AssetTypes to implement.
 * The AssetRetriever calls out to an external specializer to decorate
 * the basic Asset pulled out of the database.
 * A specializer may decorate an Asset with support assets
 * and external data as necessary.
 *
 * Implementations go into .server packages, but
 * interface stays in littleware.asset due to our AssetType
 * based specializer mechanism.
 */
public interface AssetSpecializer {
	/**
	 * Decorate the input asset with whatever supplemental data
	 * is necessary to implement the AssetType supported by this
	 * specializer.  Throws the same set of exceptions as getAsset().
	 *
	 * @param a_in asset instance of class returned by a_in.getAssetType ().create ()
	 * @param m_retriever manager making the callback
	 * @return a_in decorated with new data, or a new Asset consistent with the data in a_in
	 */
	public <T extends Asset> T narrow ( T a_in, AssetRetriever m_retriever
						  ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException;

	/**
	 * Post asset-creation callback made by the AssetManager up to the specializer
	 * responsible for the AssetType of the just created asset.
	 * Throws the same set of exceptions as AssetManager.createAsset...
	 *
	 * @param a_new reference to just created asset
	 * @param m_asset manager making the callback
	 */
	public void postCreateCallback ( Asset a_new, AssetManager m_asset  							   
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;
	
	/**
	 * Post asset-update callback made by the AssetManager up to the specializer
	 * responsible for the AssetType of the just updated asset.
	 * Throws the same set of exceptions as AssetManager.postUpdateCallback...
	 *
	 * @param a_pre_update copy of the asset loaded by the AssetManager
	 *                    before applying the update
	 * @param a_now current state of the asset after update
	 * @param m_asset manager making the callback
	 */
	public void postUpdateCallback ( Asset a_pre_update, Asset a_now, AssetManager m_asset 
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;

	
	/**
	 * Post asset-delete callback made by the AssetManager up to the specializer
	 * responsible for the AssetType of the just deleted asset.
	 * Throws the same set of exceptions as AssetManager.postUpdateCallback...
	 *
	 * @param a_deleted asset that just got cleared out
	 * @param m_asset manager making the callback
	 */
	public void postDeleteCallback ( Asset a_deleted, AssetManager m_asset 
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException;

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.asset.server;

import littleware.asset.*;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.base.*;

/**
 * Do nothing specializer.  3rd party implementations ought to 
 * extend this class, so we can safely extend the AssetSpecializer
 * interface without forcing a recompile of all 3rd party extentions.
 */
public class NullAssetSpecializer implements AssetSpecializer {

	public <T extends Asset> T narrow ( T a_in, AssetRetriever m_retriever
						  ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException
	{
		return a_in;
	}
	
	public void postCreateCallback ( Asset a_new, AssetManager m_asset  							   
									 ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException
	{}
	
	
	public void postUpdateCallback ( Asset a_pre_update, Asset a_now, AssetManager m_asset 
									 ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException
	{}
	
	
	public void postDeleteCallback ( Asset a_deleted, AssetManager m_asset
									 ) throws BaseException, AssetException, 
	GeneralSecurityException, RemoteException
	{}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


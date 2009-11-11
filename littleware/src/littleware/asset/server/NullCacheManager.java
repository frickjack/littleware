/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import java.rmi.RemoteException;
import littleware.asset.*;
import java.util.*;
import java.security.GeneralSecurityException;

import littleware.base.*;
import littleware.security.*;

/**
 * Do-nothing CacheManager.  Does nothing on a cache-insert operation,
 * and always misses on a cache-lookup operation.
 * Mostly useful for testing with cacheing disabled.
 */
public class NullCacheManager implements CacheManager {

	public NullCacheManager () {}
	
    @Override
	public Cache.Policy getPolicy () { return Cache.Policy.FIFO; }
	
    @Override
	public int getMaxSize () { return 1000000; }
	
    @Override
	public int getMaxEntryAgeSecs () { return 1000000; }
	
	
    @Override
	public Asset put ( UUID u_key, Asset a_value ) {
		return null;
	}
	
    @Override
	public Asset get ( UUID u_key ) {
		return null;
	}
	
	
    @Override
	public Asset remove ( UUID u_key ) {
		return null;
	}
	
	
    @Override
	public void clear () {
	}
	
    @Override
	public int size () {
		return 0;
	}
	
	
    @Override
	public boolean isEmpty () {
		return true;
	}
	
	
    @Override
	public Map<UUID,Asset> cacheContents () {
		return new HashMap<UUID,Asset> ();
	}
	
	
    @Override
	public Maybe<Asset> getAsset ( UUID u_id ) throws DataAccessException, AssetException, NoSuchThingException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
	public Asset getAssetOrNull ( UUID u_id ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
    @Override
	public List<Asset> getAssets ( Collection<UUID> v_id ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	public Asset getAsset ( UUID u_id, Map<UUID,Asset> v_cycle_cache ) throws DataAccessException, AssetException, NoSuchThingException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}


	public Asset getAssetOrNull ( UUID u_id, Map<UUID,Asset> v_cycle_cache ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}


	public Set<Asset> getAssets ( Collection<UUID> v_id, Map<UUID,Asset> v_cycle_cache ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	

    @Override
	public synchronized Map<String,UUID> getHomeAssetIds () throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
    @Override
	public synchronized void setHomeAssetIds ( Map<String, UUID> v_home_ids ) 
	{
	}
	
	
    @Override
	public synchronized Map<String,UUID> getAssetIdsFrom ( UUID u_source,
														   AssetType n_type
														   ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
    @Override
	public synchronized void setAssetIdsFrom ( UUID u_source,
											   AssetType n_type,
											   Map<String,UUID> v_data
											   )
	{
	}
    
    
    @Override
	public synchronized Set<UUID> getAssetIdsTo ( UUID u_to,
														   AssetType n_type
														   ) throws DataAccessException, AssetException, GeneralSecurityException
    {
        throw new CacheMissException ();
    }


    @Override
    public synchronized void setAssetIdsTo ( UUID u_to,
                                               AssetType n_type,
                                               Set<UUID> v_data
                                               )
    {
    }

	
	public String getSourceName () { return "nullcache"; }
	

    @Override
    public Maybe<Asset> getByName ( String s_name, AssetType n_type ) throws DataAccessException,
        AssetException, NoSuchThingException, AccessDeniedException, GeneralSecurityException
    {
        throw new CacheMissException ();
    }


    @Override
	public List<Asset> getAssetHistory ( UUID u_id, java.util.Date t_start, java.util.Date t_end )
		throws NoSuchThingException, AccessDeniedException, GeneralSecurityException, 
		DataAccessException, AssetException
	{
		throw new CacheMissException ();
	}

    @Override
	public void setAssetsByName ( String s_name, AssetType n_type, UUID u_home, Set<Asset> v_data ) 
	{
	}

    

    @Override
    public Maybe<Asset> getAssetFrom ( UUID u_from, String s_name
                                ) throws BaseException, AssetException, 
    GeneralSecurityException
    {
        throw new CacheMissException ();
    }

    public Asset getAssetFromOrNull ( UUID u_from, String s_name
                                      ) throws BaseException, AssetException,
    GeneralSecurityException
    {
        throw new CacheMissException ();
    }


    public Map<AssetPath,Asset> getAssetsAlongPath ( AssetPath path_asset
                                                     ) throws BaseException, AssetException,
    GeneralSecurityException
    {
        throw new CacheMissException ();
    }


    @Override
    public Maybe<Asset> getAssetAtPath ( AssetPath path_asset
                                  ) throws BaseException, AssetException,
    GeneralSecurityException
    {
        throw new CacheMissException ();
    }

    @Override
    public Map<UUID,Long> checkTransactionCount( Map<UUID,Long> v_check
                                                 ) throws BaseException
    {
        throw new CacheMissException ();
    }

    @Override
    public void setMaxSize(int iSize) {
    }

    @Override
    public void setMaxEntryAgeSecs(int iSecs) {
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType n_type, int i_state) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new CacheMissException ();
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new CacheMissException ();
    }

    @Override
    public List<IdWithClock> checkTransactionLog(UUID homeId, long minTransaction) throws BaseException, RemoteException {
        throw new CacheMissException ();
    }

}


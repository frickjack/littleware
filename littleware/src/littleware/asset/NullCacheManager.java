/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

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
	
	public Cache.Policy getPolicy () { return Cache.Policy.FIFO; }
	
	public int getMaxSize () { return -1; }
	
	public int getMaxEntryAgeSecs () { return -1; }
	
	
	public Asset put ( UUID u_key, Asset a_value ) {
		return null;
	}
	
	public Asset get ( UUID u_key ) {
		return null;
	}
	
	
	public Asset remove ( UUID u_key ) {
		return null;
	}
	
	
	public void clear () {
	}
	
	public int size () {
		return 0;
	}
	
	
	public boolean isEmpty () {
		return true;
	}
	
	
	public Map<UUID,Asset> cacheContents () {
		return new HashMap<UUID,Asset> ();
	}
	
	
	public Asset getAsset ( UUID u_id ) throws DataAccessException, AssetException, NoSuchThingException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
	public Asset getAssetOrNull ( UUID u_id ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
	public Set<Asset> getAssets ( Collection<UUID> v_id ) throws DataAccessException, AssetException, GeneralSecurityException
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
	

	public synchronized Map<String,UUID> getHomeAssetIds () throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
	public synchronized void setHomeAssetIds ( Map<String, UUID> v_home_ids ) 
	{
	}
	
	
	public synchronized Map<String,UUID> getAssetIdsFrom ( UUID u_source,
														   AssetType n_type
														   ) throws DataAccessException, AssetException, GeneralSecurityException
	{
		throw new CacheMissException ();
	}
	
	
	public synchronized void setAssetIdsFrom ( UUID u_source,
											   AssetType n_type,
											   Map<String,UUID> v_data
											   )
	{
	}
    
    
	public synchronized Set<UUID> getAssetIdsTo ( UUID u_to,
														   AssetType n_type
														   ) throws DataAccessException, AssetException, GeneralSecurityException
    {
        throw new CacheMissException ();
    }


    public synchronized void setAssetIdsTo ( UUID u_to,
                                               AssetType n_type,
                                               Set<UUID> v_data
                                               )
    {
    }

	
	public String getSourceName () { return "nullcache"; }
	

    public Asset getByName ( String s_name, AssetType n_type ) throws DataAccessException, 
        AssetException, NoSuchThingException, AccessDeniedException, GeneralSecurityException
    {
        throw new CacheMissException ();
    }


	public List<Asset> getAssetHistory ( UUID u_id, java.util.Date t_start, java.util.Date t_end )
		throws NoSuchThingException, AccessDeniedException, GeneralSecurityException, 
		DataAccessException, AssetException
	{
		throw new CacheMissException ();
	}

	public void setAssetsByName ( String s_name, AssetType n_type, UUID u_home, Set<Asset> v_data ) 
	{
	}

    

    public Asset getAssetFrom ( UUID u_from, String s_name 	
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


    public Asset getAssetAtPath ( AssetPath path_asset
                                  ) throws BaseException, AssetException,
    GeneralSecurityException
    {
        throw new CacheMissException ();
    }

    public Map<UUID,Long> checkTransactionCount( Map<UUID,Long> v_check
                                                 ) throws BaseException
    {
        throw new CacheMissException ();
    }

}


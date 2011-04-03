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

import littleware.asset.*;
import java.util.*;

import littleware.base.cache.Cache;

/**
 * Internal API for managing the asset-cache on and between servers.
 * Maintains an internal UUID to Asset dictionary,
 * and also maintains a local database tracking interasset
 * relationships.
 *
 * Whenever an asset is put into or removed from the cache,
 * its data is updated in the internal cache database too,
 * so the results of AssetRetriever calls are affected.
 * The CacheManager assumes that once an assetRetriever call is
 * cached, then that result stays valid forever - the CacheManager assumes
 * update/delete/insert calls to the underlying asset database are
 * also applied to the cache.  The API must maintain the cache.
 * The exception to this is the clear() call - which empties out
 * the cache of all data.
 *
 * AssertSearchManager calls whose underlying data has not yet been
 * cached throw a CacheMissException (subtype of AssetException).
 * A CacheManager client should catch each CacheMissException, and
 * treat that as a signal to retrieve the desired data from the primary
 * database, then add it to the cache via on of the set* methods.
 */
public interface CacheManager extends Cache<UUID,Asset>, AssetSearchManager {
	/**
	 * Update the cached view of the home-asset ids
	 *
	 * @param v_home_ids up to date info
	 */
	public void setHomeAssetIds ( Map<String, UUID> v_home_ids ); 
	
	
	/**
	 * Update the cached view of the asset-child Ids
	 *
	 * @param u_source search parameter
	 * @param n_type search parameter
	 * @param v_data that satisfies the search
	 */
	public void setAssetIdsFrom ( UUID u_source,
								  AssetType n_type,
								  Map<String,UUID> v_data
							);
	
    /**
     * Update the cached view of the assets-linking-to Ids
	 *
	 * @param u_to search parameter
	 * @param n_type search parameter
	 * @param v_data that satisfies the search
	 */
	public void setAssetIdsTo ( UUID u_to,
								  AssetType n_type,
								  Set<UUID> v_data
                                  );
    
	/**
	 * Update the cached view of the assets-by-name query
	 */
	public void setAssetsByName ( String s_name, AssetType n_type, UUID u_home, Set<Asset> v_data ); 
		
}

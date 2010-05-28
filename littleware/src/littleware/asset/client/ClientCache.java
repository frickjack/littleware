/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import com.google.inject.ImplementedBy;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.base.Cache;


/**
 * Transaction-watching cache manager.
 * When the ClientCache observes an AssetLoadEvent it
 * automatically updates the cache entry
 *    getCache().put( asset.getObjectId().toString(), asset ).
 * When the ClientCache observes an AssetLoadEvent where
 * the asset-transaction exceeds the cache transaction count,
 * then the cache flushes everything out.
 * When the ClientCache observes any other ServiceEvent,
 * then the cache flushes everything.
 * The cache has some extra littleware specific logic too.
 * For example - an asset that has not changed in over
 * 5 days are cached with a long ageout period (15 minutes or so)
 * under the assumption that such an asset is mostly read only.
 * Finally, the ClientCache obeys the ageout and replacement
 * conventions of the injected littleware.base.Cache.
 */
@ImplementedBy(CacheActivator.class)
public interface ClientCache {
    /**
     * Get the current transaction count that an asset load greater
     * than that would cause a cache flush.
     */
    public long getTransaction();

    /**
     * Get the cache - can add, query, whatever in user-specific way.
     */
    public Cache<String, Object> getCache();

    /**
     * Shortcut for getCache().put( asset.getObjectId(), asset ).
     *
     * @param asset to add to the cache
     * @return what was in the cache before if anything
     */
    public Asset put(Asset asset);

    /** Shortcut for (Asset) getCache().get( uuid.toString() ) */
    public Asset get(UUID uId);

    /** Add the object to the cache with a long-term ageout */
    public Object putLongTerm( String key, Object value );
    
}

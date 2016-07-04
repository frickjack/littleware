package littleware.asset.client.spi;

import com.google.inject.ImplementedBy;
import java.util.UUID;
import littleware.asset.Asset;


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
public interface ClientCache {
    /**
     * Get the current transaction count that an asset load greater
     * than that would cause a cache flush.
     */
    public long getTimestamp();

    /**
     * Shortcut for put( asset.getObjectId(), asset ).
     *
     * @param asset to add to the cache
     * @return what was in the cache before if anything
     */
    public void put(Asset asset);
    
    /**
     * Cache an object associated with some key
     * @param key may be object id, asset path, or whatever
     * @param asset 
     */
    public void put( String key, Asset asset );

    /** Shortcut for (Asset) get( uuid.toString() ) */
    public Asset get(UUID id);
    
    /**
     * Return null if no entry available
     * @param key
     * @return Asset or null
     */
    public Asset get( String key );

}

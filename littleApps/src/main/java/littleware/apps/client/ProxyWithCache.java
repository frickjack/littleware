/**
 * Copyright 2008, Reuben Pasquini
 * All Rights Reserved
 */

package littleware.apps.client;

/**
 * Marking interface decorating cacheing-proxies
 * returned by ProxyWithCacheBuilder.
 * Different proxies may include other properties
 * in addition to the basic AssetModelLibrary asset cache.
 */
public interface ProxyWithCache<T> {
    public AssetModelLibrary getCache ();
    public void setCache( AssetModelLibrary cache );
    public T  getCore();
    public void setCore( T value );

}

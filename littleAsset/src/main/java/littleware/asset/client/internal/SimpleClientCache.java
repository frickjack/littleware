/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import littleware.asset.client.spi.ClientCache;
import littleware.asset.client.spi.AssetLoadEvent;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Date;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleListener;
import littleware.security.Everybody;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;

/**
 * OSGi BundleActivator registers itself as a cache service
 */
@Singleton
public class SimpleClientCache implements LittleListener, ClientCache {

    private static final Logger log = Logger.getLogger(SimpleClientCache.class.getName());
    private final Cache<String, Asset> cacheLong;
    private final Cache<String, Asset> cacheShort;
    
    
    private long timestamp = -1;

    /**
     * Default constructor injects a cache builder factory
     * with which SimpleClientCache allocates its internal "short" and "long"
     * caches.
     *
     * @param eventBus constructor self-registers as a listener - should move this
     *                out to a module-startup handler or something - ugh
     */
    @Inject
    public SimpleClientCache( 
            LittleServiceBus  eventBus
            ) {
        cacheLong = CacheBuilder.newBuilder().expireAfterWrite( 900, TimeUnit.SECONDS ).maximumSize( 20000 ).build();
        cacheShort = CacheBuilder.newBuilder().expireAfterWrite( 30, TimeUnit.SECONDS ).maximumSize( 20000 ).build();
        
        // ugh - should refactor this, but put up with it for now ...
        eventBus.addLittleListener(this);
    }

    
    @Override
    public long getTimestamp() {
        return timestamp;
    }

    
    @Override
    public void put(String key, Asset asset) {
        cacheShort.put( key, asset );
        if ((null != asset.getLastUpdateDate()) && (((new Date()).getTime() - 432000) > asset.getLastUpdateDate().getTime())) {
            // not modified in 5 days
            cacheLong.put( key, asset);
        }
        if (asset.getAssetType().isA(LittleGroup.GROUP_TYPE)
                && (!(asset instanceof Everybody))) {
            // go ahead and harvest group members
            final LittleGroup group = asset.narrow();
            for (LittlePrincipal member : group.getMembers()) {
                if (!asset.equals(member)) {
                    put(member);  // recurse over nexted groups
                }
            }
        }
    }

    @Override
    public Asset get( String key ) {
        final Asset shortEntry = cacheShort.getIfPresent( key );
        if ( null != shortEntry ) {
            return shortEntry;
        }
        return cacheLong.getIfPresent(key);
    }

    @Override
    public void receiveLittleEvent(LittleEvent eventLittle) {
        if (eventLittle instanceof AssetLoadEvent) {
            final AssetLoadEvent eventLoad = (AssetLoadEvent) eventLittle;
            if (eventLoad.getAsset().getTimestamp() > getTimestamp()) {
                log.log( Level.FINE, "Clearing cache on transaction advance" );
                // very paranoid cache-flush policy
                cacheShort.invalidateAll();
                cacheLong.invalidateAll();
                timestamp = eventLoad.getAsset().getTimestamp();
            }
            put(eventLoad.getAsset());
        } else {
            log.log( Level.FINE, "Clearing cache on non-load service event" );
            cacheShort.invalidateAll();
            cacheLong.invalidateAll();
        }
    }

    @Override
    public void put(Asset asset) {
        put( asset.getId().toString(), asset );
    }

    @Override
    public Asset get(UUID id) {
        return get( id.toString() );
    }


}

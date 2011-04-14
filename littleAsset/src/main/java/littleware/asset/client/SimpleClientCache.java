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

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.base.cache.Cache;
import littleware.base.cache.Cache.Policy;
import littleware.base.cache.InMemoryCacheBuilder;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import littleware.security.SecurityAssetType;
import littleware.security.auth.LittleSession;

/**
 * OSGi BundleActivator registers itself as a cache service
 */
@Singleton
public class SimpleClientCache implements LittleServiceListener, ClientCache {

    private static final Logger log = Logger.getLogger(SimpleClientCache.class.getName());
    private final Cache<String, Object> cacheLong;
    private final Cache<String, Object> cacheShort;
    /** Extend cache to add some special asset handling */
    private final Cache<String, Object> ocache = new Cache<String, Object>() {

        @Override
        public Policy getPolicy() {
            return cacheShort.getPolicy();
        }

        @Override
        public int getMaxSize() {
            return cacheShort.getMaxSize();
        }


        @Override
        public int getMaxEntryAgeSecs() {
            return cacheShort.getMaxEntryAgeSecs();
        }


        @Override
        public Object put(String key, Object value) {
            final Object result = cacheShort.put(key, value);
            if (value instanceof Asset) {
                final Asset asset = (Asset) value;
                if ((null != asset.getLastUpdateDate()) && (((new Date()).getTime() - 432000) > asset.getLastUpdateDate().getTime())) {
                    // not modified in 5 days
                    cacheLong.put(key, value);
                }
                if (asset.getAssetType().isA(SecurityAssetType.GROUP)) {
                    // go ahead and harvest group members
                    final LittleGroup group = asset.narrow();
                    for ( LittlePrincipal member : group.getMembers() ) {
                        if ( ! asset.equals( member ) ) {
                            put(member.getId().toString(), member);  // recurse over nexted groups
                        }
                    }
                }
            }
            return result;
        }

        @Override
        public Object get(String x_key) {
            Object result = cacheShort.get(x_key);
            if (null != result) {
                return result;
            }
            // fall through to long cache
            return cacheLong.get(x_key);
        }

        @Override
        public Object remove(String x_key) {
            cacheLong.remove(x_key);
            return cacheShort.remove(x_key);
        }

        @Override
        public void clear() {
            cacheLong.clear();
            cacheShort.clear();
        }

        @Override
        public int size() {
            return cacheShort.size();
        }

        @Override
        public boolean isEmpty() {
            return cacheShort.isEmpty();
        }

        @Override
        public Map<String, Object> cacheContents() {
            throw new UnsupportedOperationException("Not supported yet.");
        }
    };
    private long olTransaction;

    /**
     * Default constructor self-injects a SimpleCache
     * with a SimpleCache.MIN_AGEOUT_SECONDS age-out
     * and 20000 max size.
     *
     * @param session initializes transaction to session.getTransactionCount,
     *           and puts the session in the cache.
     * @param helper to register this as a listener with, and to retrieve session from
     */
    @Inject
    public SimpleClientCache( LittleSession session,
            InMemoryCacheBuilder cacheBuilder
            ) {
        cacheLong = cacheBuilder.maxAgeSecs( 900 ).maxSize( 20000 ).build();
        cacheShort = cacheBuilder.maxAgeSecs( 30 ).maxSize( 20000 ).build();
        olTransaction = session.getTransaction();
        ocache.put(session.getId().toString(), session);
    }

    /**
     * Constructor enforces singleton,
     * injects the cache to use,
     * and the transaction-count.
     * Probably want the cache to have a small timeout.
     *
     * @param lTransaction to init the cache to.
     *           If the cache observes an asset load with
     *           a transaction greater than lTransaction,
     *           then the internal transaction property advances,
     *           and the cache clears.
     *
    protected ClientCache( Cache<String,Object> cache, long lTransaction ) {
    ocacheShort = cache;
    olTransaction = lTransaction;
    if ( null != osingleton ) {
    throw new AssertionFailedException( "ClientCache not a singleton" );
    }
    osingleton = this;
    }
     */
    @Override
    public long getTransaction() {
        return olTransaction;
    }

    @Override
    public Cache<String, Object> getCache() {
        return ocache;
    }

    @Override
    public Asset put(Asset asset) {
        return (Asset) getCache().put(asset.getId().toString(), asset);
    }

    @Override
    public Asset get(UUID uId) {
        return (Asset) getCache().get(uId.toString());
    }

    @Override
    public void receiveServiceEvent(LittleServiceEvent eventLittle) {
        if (eventLittle instanceof AssetLoadEvent) {
            final AssetLoadEvent eventLoad = (AssetLoadEvent) eventLittle;
            if (eventLoad.getAsset().getTransaction() > getTransaction()) {
                log.log( Level.FINE, "Clearing cache on transaction advance" );
                getCache().clear();
            }
            put(eventLoad.getAsset());
        } else {
            log.log( Level.FINE, "Clearing cache on non-load service event" );
            getCache().clear();
        }
    }

    @Override
    public Object putLongTerm(String key, Object value) {
        cacheShort.remove(key);
        return cacheLong.put(key, value);
    }
}
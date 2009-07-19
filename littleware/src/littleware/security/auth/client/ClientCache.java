/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.security.Principal;
import java.util.Enumeration;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.LittleService;
import littleware.asset.client.ServiceEvent;
import littleware.asset.client.ServiceListener;
import littleware.base.AssertionFailedException;
import littleware.base.Cache;
import littleware.base.SimpleCache;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import littleware.security.SecurityAssetType;
import littleware.security.auth.LittleSession;
import littleware.security.auth.SessionHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

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
 * Finally, the ClientCache obeys the ageout and replacement
 * conventions of the injected littleware.base.Cache.
 */
@Singleton
public class ClientCache implements ServiceListener, BundleActivator {
    private static ClientCache  osingleton = null;
    private final Cache<String, Object> ocache;
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
    public ClientCache( SessionHelper helper
            ) {
        final LittleSession session;
        try {
            session = helper.getSession();
        } catch ( Exception ex ) {
            throw new AssertionFailedException( "Failed to retrieve session" );
        }
        olTransaction = session.getTransactionCount();
        ocache = new SimpleCache<String,Object>( SimpleCache.MIN_AGEOUT_SECS, 20000 );
        ocache.put( session.getObjectId().toString(), session );
        if ( null != osingleton ) {
            throw new AssertionFailedException( "ClientCache not a singleton!" );
        }
        ((LittleService) helper).addServiceListener(this);
        osingleton = this;
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
     */
    protected ClientCache( Cache<String,Object> cache, long lTransaction ) {
        ocache = cache;
        olTransaction = lTransaction;
        if ( null != osingleton ) {
            throw new AssertionFailedException( "ClientCache not a singleton" );
        }
        osingleton = this;
    }

    /**
     * Get the current transaction count that an asset load greater
     * than that would cause a cache flush.
     */
    public long getTransaction () {
        return olTransaction;
    }

    /**
     * Get the cache - can add, query, whatever in user-specific way.
     */
    public Cache<String,Object> getCache() {
        return ocache;
    }

    /**
     * Shortcut for getCache().put( asset.getObjectId(), asset )
     *
     * @param asset to add to the cache
     * @return what was in the cache before if anything
     */
    public Asset put( Asset asset ) {
        if ( asset.getAssetType().isA( SecurityAssetType.GROUP ) ) {
            // go ahead and harvest group members
            final LittleGroup group = asset.narrow();
            for( Enumeration<? extends Principal> i = group.members();
                    i.hasMoreElements();) {
                final LittlePrincipal p = (LittlePrincipal) i.nextElement();
                put( p );  // recurse over nexted groups
            }
        }
        return (Asset) getCache().put( asset.getObjectId().toString(), asset );
    }

    /** Shortcut for (Asset) getCache().get( uuid.toString() ) */
    public Asset get( UUID uId ) {
        return (Asset) getCache().get( uId.toString() );
    }

    @Override
    public void receiveServiceEvent(ServiceEvent eventLittle) {
        if ( eventLittle instanceof AssetLoadEvent ) {
            final AssetLoadEvent eventLoad = (AssetLoadEvent) eventLittle;
            if ( eventLoad.getAsset().getTransactionCount() > getTransaction() ) {
                getCache().clear();
            }
            put( eventLoad.getAsset() );
        } else {
            getCache().clear();
        }
    }


    /** Return the singleton */
    public static ClientCache getSingleton() {
        return osingleton;
    }

    /** NOOP - setup done in constructor */
    @Override
    public void start(BundleContext arg0) throws Exception {
    }

    /** NOOP */
    @Override
    public void stop(BundleContext arg0) throws Exception {
    }
}

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
import java.util.Date;
import java.util.Enumeration;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.LittleService;
import littleware.asset.client.LittleServiceEvent;
import littleware.asset.client.LittleServiceListener;
import littleware.base.AssertionFailedException;
import littleware.base.Cache;
import littleware.base.Cache.Policy;
import littleware.base.SimpleCache;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import littleware.security.SecurityAssetType;
import littleware.security.auth.LittleSession;
import littleware.security.auth.SessionHelper;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * OSGi BundleActivator registers itself as a cache service
 */
@Singleton
public class CacheActivator implements BundleActivator, LittleServiceListener, ClientCache {

    private static final Logger log = Logger.getLogger(CacheActivator.class.getName());
    private final Cache<String, Object> cacheLong = new SimpleCache<String, Object>(900, 20000);
    private final Cache<String, Object> cacheShort = new SimpleCache<String, Object>(30, 20000);
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
        public void setMaxSize(int iSize) {
            cacheShort.setMaxSize(iSize);
        }

        @Override
        public int getMaxEntryAgeSecs() {
            return cacheShort.getMaxEntryAgeSecs();
        }

        @Override
        public void setMaxEntryAgeSecs(int iSecs) {
            cacheShort.setMaxEntryAgeSecs(iSecs);
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
                    for (Enumeration<? extends Principal> i = group.members();
                            i.hasMoreElements();) {
                        final LittlePrincipal p = (LittlePrincipal) i.nextElement();
                        if ( ! asset.equals( p ) ) {
                            put(p.getId().toString(), p);  // recurse over nexted groups
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
    public CacheActivator(SessionHelper helper) {
        final LittleSession session;
        try {
            session = helper.getSession();
        } catch (Exception ex) {
            throw new AssertionFailedException("Failed to retrieve session");
        }
        olTransaction = session.getTransaction();
        ocache.put(session.getId().toString(), session);
        ((LittleService) helper).addServiceListener(this);
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

    /**
     * Publish this cache into the ctx,
     * and listen on the whiteboard for new services 
     * as they come online.
     */
    @Override
    public void start(final BundleContext ctx) throws Exception {
        // register self to whiteboard
        ctx.registerService(ClientCache.class.getName(), this, new Properties());
        log.log(Level.FINE, "ClientCache registered with OSGi service whiteboard");
        // listen on whiteboard for new services
        /** ... not necessary at this point - SessionHelperProxy manages this stuff currently ...
         * TODO: implement AbstractLittleServiceListener OSGi activator ...
        final ServiceListener listener = new org.osgi.framework.ServiceListener() {
        @Override
        public void serviceChanged(org.osgi.framework.ServiceEvent event) {
        final ServiceReference ref = event.getServiceReference();
        final LittleService service;
        {
        final Object x = ctx.getService(ref);
        if (x instanceof LittleService) {
        service = (LittleService) x;
        } else {
        return;
        }
        }
        switch (event.getType()) {
        case org.osgi.framework.ServiceEvent.REGISTERED: {
        service.addServiceListener(CacheActivator.this);
        }
        break;
        case ServiceEvent.UNREGISTERING: {
        service.removeServiceListener( CacheActivator.this );
        } break;
        }
        }
        };
        ctx.addServiceListener( listener );
        // invoke the listener on all already-registered services
        for ( ServiceReference ref : ctx.getAllServiceReferences(null, null)) {
        listener.serviceChanged( new ServiceEvent( ServiceEvent.REGISTERED, ref ) );
        }
         */
    }

    /** Remove this cache from the context */
    @Override
    public void stop(BundleContext ctx) throws Exception {
    }

    @Override
    public Object putLongTerm(String key, Object value) {
        cacheShort.remove(key);
        return cacheLong.put(key, value);
    }
}

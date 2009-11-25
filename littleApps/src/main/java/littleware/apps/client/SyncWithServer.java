/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.client;

import com.google.common.collect.ImmutableSet;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.IdWithClock;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.LittleService;
import littleware.asset.client.LittleServiceEvent;
import littleware.asset.client.LittleServiceListener;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.client.ClientCache;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Little module periodically calls AssetSearchManager.checkTransactionLog,
 * and triggers an update of assets in the AssetModelLibrary
 * out of sync with the log.
 * There are several parts to this module.
 * First, the module registers with OSGi as a BundleActivator
 * to start and stop the periodic server check.
 * Second, the module registers as a ServiceListener with
 * the SessionHelper to listen for which home projects
 * the application is loading assets from.
 * Finally, the module implements a Runnable interface intended
 * to run with a ScheduledExecutorService that invokes findOldAssets,
 * and update the AssetModelLibrary as necessary.
 */
@Singleton
public class SyncWithServer implements BundleActivator, Runnable, LittleServiceListener {
    private static final Logger log = Logger.getLogger( SyncWithServer.class.getName() );

    private final ScheduledExecutorService execSchedule = Executors.newScheduledThreadPool(1);
    private Set<UUID> homeIdSet = ImmutableSet.of();
    private final AssetSearchManager search;
    private long minTransaction = 0;
    private final SessionHelper helper;
    private final UUID littleHomeId;
    private final AssetModelLibrary libAsset;
    private final ClientCache cache;

    @Inject
    public SyncWithServer(SessionHelper helper, AssetSearchManager search,
            AssetPathFactory pathFactory, AssetModelLibrary libAsset,
            ClientCache cache ) {
        this.search = search;
        this.helper = helper;
        this.libAsset = libAsset;
        this.cache = cache;
        try {
            littleHomeId = search.getAssetAtPath(pathFactory.createPath("/littleware.home")).get().getId();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new littleware.base.AssertionFailedException("Failed to load littleware.home id", ex);
        }
    }

    @Override
    public void start(BundleContext bundle) throws Exception {
        ((LittleService) helper).addServiceListener(this);
        final ScheduledFuture<?> handle = execSchedule.scheduleWithFixedDelay(this, 60, 20, TimeUnit.SECONDS );
    }

    @Override
    public void stop(BundleContext bundle) throws Exception {
        execSchedule.shutdown();
    }

    /** Get an immutable copy of the set of home ids currently under consideration */
    public Set<UUID> getHomeIdSet() {
        return homeIdSet;
    }

    /**
     * Get the maximum transaction from the last scan,
     * which will be the minimum transaction for the next scan.
     */
    public long getMinTransaction() {
        return minTransaction;
    }

    /**
     * Just exposed for testing - call checkTransactionLog,
     * then return a list of ids for
     * assets in the AssetModelLibrary out of sync with
     * the log.  Updates minTransaction property as a side effect.
     */
    public List<UUID> findOldAssets() throws BaseException, GeneralSecurityException, RemoteException {
        final List<UUID> result = new ArrayList<UUID>();
        long newMin = minTransaction;
        for (UUID homeId : homeIdSet) {
            if (homeId.equals(littleHomeId)) {
                continue;
            }
            for (IdWithClock data : search.checkTransactionLog(homeId, minTransaction)) {
                if ( data.getTransaction() > newMin ) {
                    newMin = data.getTransaction();
                }
                final AssetModel model = libAsset.get( data.getId() );

                if ( (null != model) 
                        && (model.getAsset().getTransaction() < data.getTransaction()) ) {
                    result.add( data.getId() );
                } else if ( (null == model)
                        && data.getFrom().isSet()
                        && (null != libAsset.get( data.getFrom().get()))
                        ) {
                    // tracking parent, so go ahead and add this child
                    // to inform parent listeners of new child
                    result.add( data.getFrom().get() );
                }
            }
        }
        minTransaction = newMin;
        return result;
    }

    @Override
    public void receiveServiceEvent(LittleServiceEvent eventIn) {
        if (eventIn instanceof AssetLoadEvent) {
            final AssetLoadEvent event = (AssetLoadEvent) eventIn;
            if ((!event.getAsset().getHomeId().equals(littleHomeId)) && (!homeIdSet.contains(event.getAsset().getHomeId()))) {
                log.log( Level.FINE, "Adding " + event.getAsset().getHomeId() + " to home id set" );
                homeIdSet = new ImmutableSet.Builder<UUID>().addAll(homeIdSet).
                        add(event.getAsset().getHomeId()).
                        build();
            }
        }
    }

    private boolean running = false;

    /**
     * Synchronized - try to avoid running multiple syncs at same time
     */
    @Override
    public void run() {
        synchronized ( this ) {
            // try to avoid running multiple syncs at same time
            if ( running ) {
                return;
            }
            running = true;
        }
        try {
            final List<UUID> idList = findOldAssets();
            log.log( Level.FINE, "Scanning transaction log " + idList.size() + ", up to transaction " + minTransaction );
            for( UUID id : idList ) {
                try {
                    cache.getCache().remove( id.toString() );
                    final Maybe<Asset> maybe = search.getAsset( id );
                    if ( maybe.isSet() ) {
                        final Asset asset = maybe.get();
                        log.log( Level.FINE, "Syncing asset " + asset.getId() + ", " + asset.getName() +
                                ", " + asset.getTransaction()
                                );
                        libAsset.syncAsset( maybe.get() );
                    }
                } catch ( Exception ex ) {
                    log.log( Level.INFO, "Failed to sync asset: " + id );
                }
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Server sync failed", ex );
        } finally {
            running = false;
        }
    }
}

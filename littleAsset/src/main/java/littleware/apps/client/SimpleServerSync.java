/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
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
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.IdWithClock;
import littleware.asset.client.AssetLoadEvent;
import littleware.asset.client.ClientCache;
import littleware.asset.client.LittleServiceEvent;
import littleware.base.BaseException;
import littleware.base.Maybe;

public class SimpleServerSync implements ServerSync {

    private static final Logger log = Logger.getLogger(SimpleServerSync.class.getName());
    private Set<UUID> homeIdSet = ImmutableSet.of();
    private final AssetSearchManager search;
    private long minTransaction = 0;
    private final UUID littleHomeId;
    private final AssetModelLibrary libAsset;
    private final ClientCache cache;

    @Inject
    public SimpleServerSync(
            AssetSearchManager search,
            AssetPathFactory pathFactory, AssetModelLibrary libAsset,
            ClientCache cache) {
        this.search = search;
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

    /** Get an immutable copy of the set of home ids currently under consideration */
    @Override
    public Set<UUID> getHomeIdSet() {
        return homeIdSet;
    }

    @Override
    public long getMinTransaction() {
        return minTransaction;
    }

    @Override
    public List<UUID> findOldAssets() throws BaseException, GeneralSecurityException, RemoteException {
        final List<UUID> result = new ArrayList<UUID>();
        long newMin = minTransaction;
        for (UUID homeId : homeIdSet) {
            if (homeId.equals(littleHomeId)) {
                continue;
            }
            for (IdWithClock data : search.checkTransactionLog(homeId, minTransaction)) {
                if (data.getTransaction() > newMin) {
                    newMin = data.getTransaction();
                }
                final AssetModel model = libAsset.get(data.getId());

                if ((null != model)
                        && (model.getAsset().getTransaction() < data.getTransaction())) {
                    result.add(data.getId());
                } else if ((null == model)
                        && data.getFrom().isSet()
                        && (null != libAsset.get(data.getFrom().get()))) {
                    // tracking parent, so go ahead and add this child
                    // to inform parent listeners of new child
                    result.add(data.getFrom().get());
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
                log.log(Level.FINE, "Adding " + event.getAsset().getHomeId() + " to home id set");
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
    public List<UUID> syncWithServer() throws BaseException, GeneralSecurityException, RemoteException {
        synchronized (this) {
            // try to avoid running multiple syncs at same time
            if (running) {
                return Collections.emptyList();
            }
            running = true;
        }
        try {
            final List<UUID> idList = findOldAssets();
            log.log(Level.FINE, "Scanning transaction log " + idList.size() + ", up to transaction " + minTransaction);
            for (UUID id : idList) {
                try {
                    cache.getCache().remove(id.toString());
                    final Maybe<Asset> maybe = search.getAsset(id);
                    if (maybe.isSet()) {
                        final Asset asset = maybe.get();
                        log.log(Level.FINE, "Syncing asset " + asset.getId() + ", " + asset.getName()
                                + ", " + asset.getTransaction());
                        libAsset.syncAsset(maybe.get());
                    }
                } catch (Exception ex) {
                    log.log(Level.INFO, "Failed to sync asset: " + id);
                }
            }
            return idList;
        } finally {
            running = false;
        }
    }

    @Override
    public AssetModelLibrary getLibrary() {
        return libAsset;
    }
}
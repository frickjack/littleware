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

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import littleware.asset.client.LittleServiceListener;
import littleware.base.BaseException;

/**
 * Interface manages client synchronization with server.
 * Listens for the client's AssetLoadEvents to keep track
 * of which assets have loaded, and periodically checks with
 * the server checkTransactionLog() for updates.
 */
public interface ServerSync extends LittleServiceListener {

    /**
     * The library to check against the server
     */
    public AssetModelLibrary getLibrary();

    /** Get an immutable copy of the set of home ids currently under consideration */
    public Set<UUID> getHomeIdSet();

    /**
     * Get the maximum transaction from the last scan,
     * which will be the minimum transaction for the next scan.
     * Initialized to the session transaction-count.
     */
    public long getMinTransaction();

    /**
     * Just exposed for testing - call checkTransactionLog,
     * then return a list of ids for
     * assets in the AssetModelLibrary out of sync with
     * the log.  Updates minTransaction property as a side effect.
     */
    public List<UUID> findOldAssets() throws BaseException, GeneralSecurityException, RemoteException;

    /**
     * Reload the assets from findOldAssets, and sync them into the AssetModelLibrary.
     * Also removes deleted assets from the littleware.asset.client.ClientCache.
     */
    public List<UUID> syncWithServer() throws BaseException, GeneralSecurityException, RemoteException;
}

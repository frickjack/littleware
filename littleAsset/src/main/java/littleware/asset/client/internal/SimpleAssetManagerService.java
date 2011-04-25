/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.AssetDeleteEvent;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.client.AssetManagerService;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.base.BaseException;

/**
 * Simple implementation of AssetManagerService wrapper around AssetManager
 */
public class SimpleAssetManagerService implements AssetManagerService {

    private static final long serialVersionUID = 4377427321241771838L;
    private final AssetManager     server;
    private final LittleServiceBus eventBus;


    /**
     * Inject the server to wrap with LittleService event throwing support
     */
    public SimpleAssetManagerService(AssetManager server, LittleServiceBus eventBus ) {
        this.server = server;
        this.eventBus = eventBus;
    }

    @Override
    public void deleteAsset(UUID assetId, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        server.deleteAsset(assetId, updateComment);
        eventBus.fireEvent(new AssetDeleteEvent(this, assetId));
    }

    @Override
    public <T extends Asset> T saveAsset(T asset, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        T result = server.saveAsset(asset, updateComment);
        eventBus.fireEvent(new AssetLoadEvent(this, result));
        return result;
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(Collection<Asset> assetList, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Collection<Asset> vResult = server.saveAssetsInOrder(assetList, updateComment);
        for (Asset result : vResult) {
            eventBus.fireEvent(new AssetLoadEvent(this, result));
        }
        return vResult;
    }
}

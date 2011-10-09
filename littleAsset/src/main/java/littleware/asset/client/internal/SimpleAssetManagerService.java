/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import littleware.asset.client.spi.AssetLoadEvent;
import littleware.asset.client.spi.AssetDeleteEvent;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetManager;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.asset.internal.RemoteAssetManager;
import littleware.base.BaseException;
import littleware.security.auth.client.KeyChain;

/**
 * Simple implementation of AssetManagerService wrapper around AssetManager
 */
public class SimpleAssetManagerService implements AssetManager {

    private static final long serialVersionUID = 4377427321241771838L;
    private final RemoteAssetManager server;
    private final LittleServiceBus eventBus;
    private final KeyChain keychain;
    private final AssetLibrary library;

    /**
     * Inject the server to wrap with LittleService event throwing support
     */
    @Inject
    public SimpleAssetManagerService(RetryRemoteAstMgr server, LittleServiceBus eventBus, KeyChain keychain, AssetLibrary library) {
        this.server = server;
        this.eventBus = eventBus;
        this.keychain = keychain;
        this.library = library;
    }

    @Override
    public void deleteAsset(UUID assetId, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        server.deleteAsset(sessionId, assetId, updateComment);
        eventBus.fireEvent(new AssetDeleteEvent(this, assetId));
    }

    @Override
    public <T extends Asset> T saveAsset(T asset, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final Map<UUID, Asset> result = server.saveAsset(sessionId, asset, updateComment);
        for (Asset scan : result.values()) {
            library.syncAsset(scan);
            eventBus.fireEvent(new AssetLoadEvent(this, scan));
        }
        return (T) result.get( asset.getId() );
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(Collection<Asset> assetList, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final UUID sessionId = keychain.getDefaultSessionId().get();
        final Map<UUID,Asset> savedAssets = server.saveAssetsInOrder(sessionId, assetList, updateComment);
        for (Asset scan : savedAssets.values() ) {
            library.syncAsset(scan);
            eventBus.fireEvent(new AssetLoadEvent(this, scan));
        }
        final ImmutableList.Builder<Asset> resultBuilder = ImmutableList.builder();
        for( Asset scan : assetList ) {
            final Asset saved = savedAssets.get( scan.getId() );
            if ( null != saved ) {
                resultBuilder.add( saved );
            }
        }
        return resultBuilder.build();
    }
}

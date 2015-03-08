/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.internal.RemoteAssetManager;
import littleware.base.BaseException;



public class InMemoryAssetMgrProxy implements RemoteAssetMgrProxy {
    private final RemoteAssetManager delegate;

    @Inject
    public InMemoryAssetMgrProxy(RemoteAssetManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public void deleteAsset(UUID sessionId, UUID assetId, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        delegate.deleteAsset(sessionId, assetId, updateComment);
    }

    @Override
    public ImmutableMap<UUID, Asset> saveAsset(UUID sessionId, Asset asset, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.saveAsset(sessionId, asset, updateComment);
    }

    @Override
    public ImmutableMap<UUID, Asset> saveAssetsInOrder(UUID sessionId, Collection<Asset> assetList, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.saveAssetsInOrder(sessionId, assetList, updateComment);
    }
    
}

/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.asset.internal.RemoteSearchManager.AssetResult;
import littleware.asset.internal.RemoteSearchManager;
import littleware.base.BaseException;
import littleware.base.Option;

public class InMemorySearchMgrProxy implements RemoteSearchMgrProxy {

    private final RemoteSearchManager delegate;

    @Inject
    public InMemorySearchMgrProxy(RemoteSearchManager delegate) {
        this.delegate = delegate;
    }

    @Override
    public Option<Asset> getByName(UUID sessionId, String name, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getByName(sessionId, name, type);
    }

    @Override
    public List<Asset> getAssetHistory(UUID sessionId, UUID assetId, Date start, Date end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetHistory(sessionId, assetId, start, end);
    }

    @Override
    public Option<Asset> getAssetFrom(UUID sessionId, UUID parentId, String name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetFrom(sessionId, parentId, name);
    }


    @Override
    public Set<UUID> getAssetIdsTo(UUID sessionId, UUID toId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetIdsTo(sessionId, toId, type);
    }

    @Override
    public AssetResult getAsset(UUID sessionId, UUID assetId, long cacheTimestamp) throws BaseException, GeneralSecurityException, RemoteException {
        return delegate.getAsset(sessionId, assetId, cacheTimestamp);
    }

    @Override
    public Map<UUID, AssetResult> getAssets(UUID sessionId, Map<UUID, Long> idToCacheTStamp) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssets(sessionId, idToCacheTStamp);
    }

    @Override
    public Map<String, UUID> getHomeAssetIds(UUID sessionId) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getHomeAssetIds(sessionId);
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID fromId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetIdsFrom(sessionId, fromId, type);
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID fromId) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetIdsFrom(sessionId, fromId);
    }
}

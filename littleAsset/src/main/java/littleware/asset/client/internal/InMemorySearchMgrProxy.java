package littleware.asset.client.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.asset.internal.RemoteSearchManager.AssetResult;
import littleware.asset.internal.RemoteSearchManager;
import littleware.base.BaseException;

public class InMemorySearchMgrProxy implements RemoteSearchManager {

    private final RemoteSearchManager delegate;

    @Inject
    public InMemorySearchMgrProxy(RemoteSearchManager delegate) {
        this.delegate = delegate;
    }


    @Override
    public ImmutableList<Asset> getAssetHistory(UUID sessionId, UUID assetId, Date start, Date end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetHistory(sessionId, assetId, start, end);
    }


    @Override
    public AssetResult getAsset(UUID sessionId, UUID assetId, long cacheTimestamp) throws BaseException, GeneralSecurityException, RemoteException {
        return delegate.getAsset(sessionId, assetId, cacheTimestamp);
    }


    @Override
    public ImmutableMap<UUID, AssetResult> getAssets(UUID sessionId, Map<UUID, Long> idToCacheTStamp) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssets( sessionId, idToCacheTStamp );
    }

    @Override
    public InfoMapResult getHomeAssetIds(UUID sessionId, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getHomeAssetIds( sessionId, cacheTimestamp, sizeInCache );
    }

    @Override
    public InfoMapResult getAssetIdsFrom(UUID sessionId, UUID fromId, AssetType type, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetIdsFrom( sessionId, fromId, type, cacheTimestamp, sizeInCache );
    }

    @Override
    public InfoMapResult getAssetIdsFrom(UUID sessionId, UUID fromId, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetIdsFrom( sessionId, fromId, cacheTimestamp, sizeInCache );
    }

    @Override
    public AssetResult getByName(UUID sessionId, String name, AssetType type, long cacheTimestamp) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getByName( sessionId, name, type, cacheTimestamp );
    }

    @Override
    public AssetResult getAssetFrom(UUID sessionId, UUID parentId, String name, long cacheTimestamp) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetFrom( sessionId, parentId, name, cacheTimestamp );
    }

    @Override
    public InfoMapResult getAssetIdsTo(UUID sessionId, UUID toId, AssetType type, long cacheTimestamp, int sizeInCache) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return delegate.getAssetIdsTo( sessionId, toId, type, cacheTimestamp, sizeInCache );
    }
}

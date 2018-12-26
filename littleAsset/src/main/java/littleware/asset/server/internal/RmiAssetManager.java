package littleware.asset.server.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.*;
import java.security.*;

import littleware.asset.*;
import littleware.asset.client.RemoteException;
import littleware.asset.internal.RemoteAssetManager;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerAssetManager;
import littleware.base.*;

/**
 * Adapter converts remote supplied session id to login context and cache
 */
public class RmiAssetManager extends RmiSearchManager implements RemoteAssetManager {

    private final ServerAssetManager assetMgr;
    private final LittleContext.ContextFactory ctxFactory;

    @Inject
    public RmiAssetManager(ServerAssetManager proxy, LittleContext.ContextFactory ctxFactory ) throws RemoteException {
        super( proxy, ctxFactory );
        assetMgr = proxy;
        this.ctxFactory = ctxFactory;
    }

    @Override
    public void deleteAsset(UUID sessionId, UUID assetId,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        assetMgr.deleteAsset( ctxFactory.build(sessionId), assetId, updateComment);
    }

    @Override
    public ImmutableMap<UUID,Asset> saveAsset(UUID sessionId, Asset asset,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return assetMgr.saveAsset( ctxFactory.build(sessionId), asset, updateComment);
    }

    @Override
    public ImmutableMap<UUID,Asset> saveAssetsInOrder(UUID sessionId, Collection<Asset> assets,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return assetMgr.saveAssetsInOrder( ctxFactory.build(sessionId), assets, updateComment);
    }
}


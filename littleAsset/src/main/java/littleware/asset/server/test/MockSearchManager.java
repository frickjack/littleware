/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server.test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetType;
import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.internal.RemoteSearchManager.AssetResult;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerSearchManager;
import littleware.base.BaseException;

/**
 * We want to test the ServerSearchManager, but we already
 * have a good test case for the client-side AssetSearchManager,
 * so this MockSearchManager wraps a ServerSearchManager to look
 * like an AssetSearchManager, so we can test the server-side
 * API with the client-side test cases.
 */
public class MockSearchManager implements AssetSearchManager {
    private final ServerSearchManager search;
    private final LittleContext ctx;

    @Inject
    public MockSearchManager( ServerSearchManager search,
            LittleContext ctx
            ) {
        this.search = search;
        this.ctx = ctx;
    }

    @Override
    public AssetRef getAsset(UUID assetId) throws BaseException, GeneralSecurityException, RemoteException {
        return new MockAssetRef( search.getAsset(ctx, assetId, -1L ).getAsset() );
    }

    @Override
    public Map<UUID,AssetRef> getAssets(Collection<UUID> idSet) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final ImmutableMap.Builder<UUID,Long> idBuilder = ImmutableMap.builder();
        for( UUID id : idSet ) {
            idBuilder.put( id, -1L );
        }
        final Map<UUID,AssetResult> serverResult = search.getAssets(ctx, idBuilder.build() );
        final ImmutableMap.Builder<UUID,AssetRef> resultBuilder = ImmutableMap.builder();
        for( Map.Entry<UUID,AssetResult> entry : serverResult.entrySet() ) {
            resultBuilder.put( entry.getKey(), new MockAssetRef( entry.getValue().getAsset() ) );
        }
        return resultBuilder.build();
    }

    @Override
    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getHomeAssetIds(ctx);
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID fromId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom(ctx, fromId, type );
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID fromId) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom(ctx, fromId);
    }

    @Override
    public AssetRef getByName(String name, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return new MockAssetRef( search.getByName(ctx, name, type));
    }

    @Override
    public AssetRef getAssetAtPath(AssetPath path) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AssetPath normalizePath(AssetPath pathIn) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AssetPath toRootedPath(AssetPath pathIn) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AssetPath toRootedPath(UUID uAsset) throws BaseException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public AssetRef getAssetFrom(UUID from, String name) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return new MockAssetRef( search.getAssetFrom( ctx, from, name ) );
    }


    @Override
    public Set<UUID> getAssetIdsTo(UUID toId, AssetType type) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getAssetIdsTo(ctx, toId, type);
    }

}

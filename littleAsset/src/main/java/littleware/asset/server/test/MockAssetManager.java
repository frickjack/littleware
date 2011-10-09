/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server.test;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.client.AssetManager;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerAssetManager;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;

/**
 * We want to test the ServerAssetManager, but we already
 * have a good test case for the client-side AssetManager,
 * so this mock wraps a ServerAssetManager to look
 * like an AssetManager, so we can test the server-side
 * API with the client-side test cases.
 */
public class MockAssetManager implements AssetManager {
    private final LittleContext ctx;
    private final ServerAssetManager mgr;

    @Inject
    public MockAssetManager( LittleContext ctx, ServerAssetManager mgr ) {
        this.ctx = ctx;
        this.mgr = mgr;
    }

    @Override
    public void deleteAsset(UUID assetId, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        mgr.deleteAsset(ctx, assetId, updateComment);
    }

    @Override
    public <T extends Asset> T saveAsset(T asset, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Asset result = mgr.saveAsset( ctx, asset, updateComment ).get( asset.getId() );
        if ( null == result ) {
            throw new AssertionFailedException( "save-result does not include expected asset" );
        }
        return (T) result;
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(Collection<Asset> assets, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Map<UUID,Asset> saveResult = mgr.saveAssetsInOrder(ctx, assets, updateComment);
        final ImmutableList.Builder<Asset> resultBuilder = ImmutableList.builder();
        for ( Asset scan : assets ) {
            final Asset saved = saveResult.get( scan.getId() );
            if ( null != saved ) {
                resultBuilder.add( saved);
            }
        }
        return resultBuilder.build();
    }

}

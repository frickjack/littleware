/*
 * Copyright 2011 catdogboy@yahoo.com
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.internal;

import com.google.inject.Inject;
import java.util.*;
import java.security.*;
import java.rmi.RemoteException;

import littleware.asset.*;
import littleware.asset.internal.RemoteAssetManager;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerAssetManager;
import littleware.base.*;
import littleware.net.LittleRemoteObject;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiAssetManager extends LittleRemoteObject implements RemoteAssetManager {

    private final ServerAssetManager assetMgr;
    private final LittleContext.ContextFactory ctxFactory;

    @Inject
    public RmiAssetManager(ServerAssetManager proxy, LittleContext.ContextFactory ctxFactory ) throws RemoteException {
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
    public <T extends Asset> T saveAsset(UUID sessionId, T asset,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return assetMgr.saveAsset( ctxFactory.build(sessionId), asset, updateComment);
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(UUID sessionId, Collection<Asset> assets,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return assetMgr.saveAssetsInOrder( ctxFactory.build(sessionId), assets, updateComment);
    }
}


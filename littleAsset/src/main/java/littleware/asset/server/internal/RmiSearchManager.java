/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.internal;

import com.google.common.collect.MapMaker;
import com.google.inject.Inject;
import java.util.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.asset.*;
import littleware.asset.internal.RemoteSearchManager;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleContext.ContextFactory;
import littleware.asset.server.ServerSearchManager;
import littleware.base.*;
import littleware.net.LittleRemoteObject;
import static littleware.asset.internal.RemoteSearchManager.AssetResult;


/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiSearchManager extends LittleRemoteObject implements RemoteSearchManager, Remote {
    private static final long serialVersionUID = 3426911488683486233L;

    private final ServerSearchManager search;
    private final Map<UUID,ServerSearchManager> sessionCache = (new MapMaker()).softValues().makeMap();
    private final ContextFactory contextFactory;
    
    @Inject
    public RmiSearchManager(
            ServerSearchManager proxy,
            LittleContext.ContextFactory contextFactory
            ) throws RemoteException {
        this.search = proxy;
        this.contextFactory = contextFactory;
    }

    @Override
    public Option<Asset> getByName(UUID sessionId, String name, AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getByName( contextFactory.build( sessionId ), name, type);
    }

    @Override
    public List<Asset> getAssetHistory(UUID sessionId, UUID u_id, Date t_start, Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetHistory( contextFactory.build( sessionId ), u_id, t_start, t_end);
    }

    @Override
    public AssetResult getAsset(UUID sessionId, UUID id, long clientCacheTStamp ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAsset( contextFactory.build( sessionId ), id, clientCacheTStamp );
    }


    @Override
    public Map<UUID,AssetResult> getAssets(UUID sessionId, Map<UUID,Long> id2TStamp) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssets( contextFactory.build( sessionId ), id2TStamp);
    }

    @Override
    public Map<String, UUID> getHomeAssetIds(UUID sessionId) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getHomeAssetIds( contextFactory.build( sessionId ) );
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID u_from,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom( contextFactory.build( sessionId ), u_from, n_type);
    }



    @Override
    public Option<Asset> getAssetFrom(UUID sessionId, UUID u_from, String s_name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetFrom( contextFactory.build( sessionId ), u_from, s_name);
    }



    @Override
    public Set<UUID> getAssetIdsTo(UUID sessionId, UUID u_to,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetIdsTo( contextFactory.build( sessionId ), u_to, n_type);
    }


    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID sessionId, UUID u_from) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom( contextFactory.build( sessionId ), u_from );
    }

}

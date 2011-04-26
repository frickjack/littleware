/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.internal;

import littleware.asset.client.AssetSearchManager;
import java.util.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;

import littleware.asset.*;
import littleware.base.*;
import littleware.net.LittleRemoteObject;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiSearchManager extends LittleRemoteObject implements AssetSearchManager, Remote {
    private static final long serialVersionUID = 3426911488683486233L;

    private final AssetSearchManager search;

    public RmiSearchManager(AssetSearchManager m_proxy) throws RemoteException {
        search = m_proxy;
    }

    @Override
    public Option<Asset> getByName(String s_name, AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getByName(s_name, n_type);
    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetHistory(u_id, t_start, t_end);
    }

    @Override
    public Option<Asset> getAsset(UUID u_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAsset(u_id);
    }


    @Override
    public List<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssets(v_id);
    }

    @Override
    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getHomeAssetIds();
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom(u_from, n_type);
    }


    @Override
    public Option<Asset> getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetAtPath(path_asset);
    }

    @Override
    public Option<Asset> getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetFrom(u_from, s_name);
    }


    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException, RemoteException {
        return search.checkTransactionCount(v_check);
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID u_to,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return search.getAssetIdsTo(u_to, n_type);
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType n_type, int i_state) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom( u_from, n_type, i_state );
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return search.getAssetIdsFrom( u_from );
    }

    @Override
    public List<IdWithClock> checkTransactionLog(UUID homeId, long minTransaction) throws BaseException, RemoteException {
        return search.checkTransactionLog(homeId, minTransaction);
    }
}

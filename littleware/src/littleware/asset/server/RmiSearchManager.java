/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server;

import java.util.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;

import littleware.asset.*;
import littleware.base.*;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiSearchManager extends LittleRemoteObject implements AssetSearchManager, Remote {
    private static final long serialVersionUID = 3426911488683486233L;

    private AssetSearchManager om_proxy = null;

    public RmiSearchManager(AssetSearchManager m_proxy) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        om_proxy = m_proxy;
    }

    @Override
    public Maybe<Asset> getByName(String s_name, AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getByName(s_name, n_type);
    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetHistory(u_id, t_start, t_end);
    }

    @Override
    public Maybe<Asset> getAsset(UUID u_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAsset(u_id);
    }


    @Override
    public List<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssets(v_id);
    }

    @Override
    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getHomeAssetIds();
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetIdsFrom(u_from, n_type);
    }


    @Override
    public Maybe<Asset> getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetAtPath(path_asset);
    }

    @Override
    public Maybe<Asset> getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetFrom(u_from, s_name);
    }


    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException, RemoteException {
        return om_proxy.checkTransactionCount(v_check);
    }

    @Override
    public Set<UUID> getAssetIdsTo(UUID u_to,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetIdsTo(u_to, n_type);
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType<? extends Asset> n_type, int i_state) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return om_proxy.getAssetIdsFrom( u_from, n_type, i_state );
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return om_proxy.getAssetIdsFrom( u_from );
    }
}

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

    private AssetSearchManager om_proxy = null;

    public RmiSearchManager(AssetSearchManager m_proxy) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        om_proxy = m_proxy;
    }

    public Asset getByName(String s_name, AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getByName(s_name, n_type);
    }

    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetHistory(u_id, t_start, t_end);
    }

    public Asset getAsset(UUID u_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAsset(u_id);
    }

    public Asset getAssetOrNull(UUID u_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetOrNull(u_id);
    }

    public Set<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssets(v_id);
    }

    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getHomeAssetIds();
    }

    public Map<String, UUID> getAssetIdsFrom(UUID u_from,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetIdsFrom(u_from, n_type);
    }

    public String getSourceName() throws RemoteException {
        return om_proxy.getSourceName();
    }

    public Map<AssetPath, Asset> getAssetsAlongPath(AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetsAlongPath(path_asset);
    }

    public Asset getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetAtPath(path_asset);
    }

    public Asset getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetFrom(u_from, s_name);
    }

    public Asset getAssetFromOrNull(UUID u_from, String s_name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetFromOrNull(u_from, s_name);
    }

    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException, RemoteException {
        return om_proxy.checkTransactionCount(v_check);
    }

    public Set<UUID> getAssetIdsTo(UUID u_to,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.getAssetIdsTo(u_to, n_type);
    }
}// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


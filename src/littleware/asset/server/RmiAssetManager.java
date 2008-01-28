package littleware.asset.server;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import java.security.acl.*;
import javax.security.auth.*;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.LittlePrincipal;
import littleware.security.AccessDeniedException;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiAssetManager extends UnicastRemoteObject implements AssetManager {

    private AssetManager om_proxy = null;

    public RmiAssetManager(AssetManager m_proxy) throws RemoteException {
        super();
        om_proxy = m_proxy;
    }

    public void deleteAsset(UUID u_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        om_proxy.deleteAsset(u_asset, s_update_comment);
    }

    public <T extends Asset> T saveAsset(T a_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.saveAsset(a_asset, s_update_comment);
    }

    public Collection<Asset> saveAssetsInOrder(Collection<Asset> v_assets,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.saveAssetsInOrder(v_assets, s_update_comment);
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


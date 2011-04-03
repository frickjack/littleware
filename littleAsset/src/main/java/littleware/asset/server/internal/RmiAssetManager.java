/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.internal;

import java.util.*;
import java.security.*;
import java.rmi.RemoteException;
//import java.rmi.server.UnicastRemoteObject;

import littleware.asset.*;
import littleware.base.*;

/**
 * RMI remote-ready wrapper around a real implementation.
 * Should usually wrap a SubjectInvocationHandler equiped DynamicProxy
 * of a base implementation class.
 */
public class RmiAssetManager extends LittleRemoteObject implements AssetManager {

    private AssetManager om_proxy = null;

    public RmiAssetManager(AssetManager m_proxy) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        om_proxy = m_proxy;
    }

    @Override
    public void deleteAsset(UUID u_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        om_proxy.deleteAsset(u_asset, s_update_comment);
    }

    @Override
    public <T extends Asset> T saveAsset(T a_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.saveAsset(a_asset, s_update_comment);
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(Collection<Asset> v_assets,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return om_proxy.saveAssetsInOrder(v_assets, s_update_comment);
    }
}


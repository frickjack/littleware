/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.base.BaseException;

/**
 * Simple implemntation of AssetManagerService wrapper around AssetManager
 */
public class SimpleAssetManagerService extends SimpleLittleService implements AssetManagerService {

    private static final long serialVersionUID = 4377427321241771838L;
    private AssetManager oserver;

    /** Only intended to support serialization */
    protected SimpleAssetManagerService() {
    }

    /**
     * Inject the server to wrap with LittleService event throwing support
     */
    public SimpleAssetManagerService(AssetManager server) {
        oserver = server;
        if (server instanceof LittleService) {
            throw new IllegalArgumentException("Attemp to double wrap server");
        }
    }

    public void deleteAsset(UUID u_asset, String s_update_comment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        oserver.deleteAsset(u_asset, s_update_comment);
        fireServiceEvent(new AssetDeleteEvent(this, u_asset));
    }

    public <T extends Asset> T saveAsset(T a_asset, String s_update_comment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        T result = oserver.saveAsset(a_asset, s_update_comment);
        fireServiceEvent(new AssetLoadEvent(this, result));
        return result;
    }

    public Collection<Asset> saveAssetsInOrder(Collection<Asset> v_assets, String s_update_comment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Collection<Asset> vResult = oserver.saveAssetsInOrder(v_assets, s_update_comment);
        for (Asset result : vResult) {
            fireServiceEvent(new AssetLoadEvent(this, result));
        }
        return vResult;
    }
}

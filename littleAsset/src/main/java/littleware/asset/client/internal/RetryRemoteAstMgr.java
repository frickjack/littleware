/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.client.internal;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.internal.RemoteAssetManager;
import littleware.base.BaseException;
import littleware.net.RemoteRetryHelper;

/**
 * Client-side RemoteAssetManager over RMI with auto-retry
 */
public class RetryRemoteAstMgr extends RemoteRetryHelper<RemoteAssetManager> implements RemoteAssetManager {

    @Inject
    public RetryRemoteAstMgr( @Named("littleware.jndi.prefix") String jndiPrefix ) {
        super(jndiPrefix + RemoteAssetManager.LOOKUP_PATH );
    }

    @Override
    public void deleteAsset(UUID sessionId, UUID assetId, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                getLazy().deleteAsset( sessionId, assetId, updateComment );
                return;
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public <T extends Asset> T saveAsset(UUID sessionId, T asset, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().saveAsset( sessionId, asset, updateComment );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

    @Override
    public Collection<Asset> saveAssetsInOrder(UUID sessionId, Collection<Asset> assetList, String updateComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return getLazy().saveAssetsInOrder( sessionId, assetList, updateComment );
            } catch (RemoteException ex) {
                handle(ex);
            } catch (NullPointerException ex) {
                handle(new RemoteException("Unexpected exception", ex));
            }
        }
    }

}

package littleware.asset.server;

import com.google.inject.Singleton;
import littleware.asset.*;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import littleware.base.BaseException;



/**
 * Do nothing specializer.  3rd party implementations ought to 
 * extend this class, so we can safely extend the AssetSpecializer
 * interface without forcing a recompile of all 3rd party extentions.
 */
@Singleton
public class NullAssetSpecializer implements AssetSpecializer {

    @Override
    public <T extends Asset> T narrow(T asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        return asset;
    }

    @Override
    public void postCreateCallback(Asset asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
    }

    @Override
    public void postUpdateCallback(Asset oldAsset, Asset currentAsset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
    }

    @Override
    public void postDeleteCallback(Asset asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
    }
}


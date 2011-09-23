package littleware.asset.server;

import com.google.inject.Singleton;
import littleware.asset.*;
import java.security.GeneralSecurityException;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.Option;



/**
 * Do nothing specializer.  3rd party implementations ought to 
 * extend this class, so we can safely extend the AssetSpecializer
 * interface without forcing a recompile of all 3rd party extentions.
 */
@Singleton
public class NullAssetSpecializer implements AssetSpecializer {

    @Override
    public <T extends Asset> T narrow(LittleContext ctx, T asset) throws BaseException, AssetException,
            GeneralSecurityException {
        return asset;
    }

    @Override
    public void postCreateCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
    }

    @Override
    public void postUpdateCallback(LittleContext ctx, Asset oldAsset, Asset currentAsset) throws BaseException, AssetException,
            GeneralSecurityException {
    }

    @Override
    public void postDeleteCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
    }

    @Override
    public Option<String> validate( LittleContext ctx, Asset asset) throws BaseException, AssetException, GeneralSecurityException {
        return Maybe.empty();
    }
}


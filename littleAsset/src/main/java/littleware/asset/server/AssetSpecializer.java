package littleware.asset.server;

import littleware.asset.*;
import java.security.GeneralSecurityException;
import java.util.Optional;
import java.util.Set;

import littleware.base.*;

/**
 * Interface for managers of different AssetTypes to implement.
 * The AssetRetriever calls out to an external specializer to decorate
 * the basic Asset pulled out of the database.
 * A specializer may decorate an Asset with support assets
 * and external data as necessary.
 *
 * Implementations go into .server packages, but
 * interface stays in littleware.asset due to our AssetType
 * based specializer mechanism.
 */
public interface AssetSpecializer {

    /**
     * Decorate the input asset with whatever supplemental data
     * is necessary to implement the AssetType supported by this
     * specializer.  Throws the same set of exceptions as getAsset().
     *
     * @return asset, or a new Asset consistent with the data in asset
     */
    public <T extends Asset> T narrow( LittleContext ctx, T asset) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Pre-save and pre-update callback to check sanity of asset.
     * Should only perform read-only operations on the repository.
     * Only type-specific checks are necessary - the AssetManager performs
     * general checks.
     * 
     * @param asset to check
     * @return empty if the asset is valid, otherwise a message describing the problem
     */
    public Optional<String> validate( LittleContext ctx, Asset asset ) throws BaseException, AssetException,
            GeneralSecurityException;
    
    /**
     * Post asset-creation callback made by the AssetManager up to the specializer
     * responsible for the AssetType of the just created asset.
     * Throws the same set of exceptions as AssetManager.createAsset...
     *
     * @param asset just created
     * @return set of assets to add to the caller's result set
     */
    public Set<Asset> postCreateCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Post asset-update callback made by the AssetManager up to the specializer
     * responsible for the AssetType of the just updated asset.
     * Throws the same set of exceptions as AssetManager.postUpdateCallback...
     *
     * @param oldAsset copy of the asset loaded by the AssetManager
     *                    before applying the update
     * @param currentAsset current state of the asset after update
     * @return set of assets to add to the caller's result set
     */
    public Set<Asset> postUpdateCallback(LittleContext ctx, Asset oldAsset, Asset currentAsset) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Post asset-delete callback made by the AssetManager up to the specializer
     * responsible for the AssetType of the just deleted asset.
     * Throws the same set of exceptions as AssetManager.postUpdateCallback...
     *
     * @param asset that just got cleared out
     */
    public void postDeleteCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException;
}

package littleware.asset.server;

import com.google.common.collect.ImmutableMap;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.base.AlreadyExistsException;
import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.security.AccessDeniedException;

/**
 * Server side AssetManager implementation interface
 */
public interface ServerAssetManager extends ServerSearchManager {

    /**
     * Delete the specified asset
     *
     * @param assetId id of asset to delete - must have WRITE permission
     * @param updateComment to attach to asset giving reason for deletion
     * @throws NoSuchThingException if the given asset does not exist in the database
     * @throws AccessDeniedException if do not have write-permission on the link source
     * @throws DataAccessException on database access/interaction failure
     */
    public void deleteAsset( LittleContext ctx, UUID assetId,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Save the specified asset - create or update as necessary.
     * Calls out to asset-specializer to perform post-save customization as necessary
     * based on asset-type.
     * If asset does not have an ObjectId, then one is assigned.
     * Enforces various rules including every asset must have an owner,
     * security checks, and no assets may link FROM an LinkAsset.LINK_TYPE type asset.
     *
     * @param asset to save - new asset created if object-id is null
     *           or asset with id does not exist, otherwise
     *                     attempt an update
     * @param updateComment to attach to asset giving reason for update -
     *                   updates asset's last-change info.
     * @return id to asset map including asset with updated transaction count, id, etc. resulting from
     *              save side-effects.  References asset on local JVM call,
     *              but critical that RMI clients collect the result to
     *              maintain up-to-date local data and any other assets added in specializer callbacks
     *              to notify client of server-side updates
     * @throws NoSuchThingException if the given asset references an
     *                   ACL or owner that does not exist in the database
     * @throws AccessDeniedException if do not have write-permission on the link source
     * @throws DataAccessException on database access/interaction failure
     * @throws IllegalArgumentException if supplied asset does not have a valid name
     * @throws AlreadyExistsException if asset save violates some uniqueness constraint
     */
    public ImmutableMap<UUID,Asset> saveAsset( LittleContext ctx, Asset asset,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Save (update or create) the given collection of assets in order
     * as a single transaction.
     *
     * @param assetList to save in order
     * @param updateComment applied to all assets
     * @return updated assets
     */
    public ImmutableMap<UUID,Asset> saveAssetsInOrder( LittleContext ctx, Collection<Asset> assetList,
            String updateComment) throws BaseException, AssetException,
            GeneralSecurityException;

}

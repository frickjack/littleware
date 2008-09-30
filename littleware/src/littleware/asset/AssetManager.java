package littleware.asset;

import java.util.*;
import java.security.GeneralSecurityException;
import java.security.acl.*;
import java.rmi.Remote;
import java.rmi.RemoteException;

import littleware.base.*;
import littleware.security.LittlePrincipal;
import littleware.security.AccessDeniedException;

/**
 * Interface for manipulating primitive Assets.
 * Does not extends Remote so we have the option of
 * sending cilents serializable proxies, but every method
 * does throw RemoteException so this interface is
 * ready for a Remote mixin.
 */
public interface AssetManager extends Remote {

    /**
     * Delete the specified asset 
     *
     * @param u_asset id of asset to delete - must have WRITE permission
     * @param s_update_comment to attach to asset giving reason for deletion
     * @exception NoSuchThingException if the given asset does not exist in the database
     * @exception AccessDeniedException if do not have write-permission on the link source
     * @exception DataAccessException on database access/interaction failure
     */
    public void deleteAsset(UUID u_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Save the specified asset - create or update as necessary.
     * Calls out to asset-specializer to perform post-save customization as necessary
     * based on asset-type.
     * If asset does not have an ObjectId, then one is assigned.  
     * Enforces various rules including every asset must have an owner,
     * security checks, and no assets may link FROM an AssetType.LINK type asset.
     *
     * @param a_asset to save - new asset created if object-id is null
     *           or asset with id does not exist, otherwise
     *                     attempt an update
     * @param s_update_comment to attach to asset giving reason for update -
     *                   updates asset's last-change info.
     * @return a_asset with updated transaction count, id, etc. resulting from
     *              save side-effects.  References a_asset on local JVM call,
     *              but critical that RMI clients collect the result to
     *              maintain up-to-date local data.
     * @exception NoSuchThingException if the given asset references an
     *                   ACL or owner that does not exist in the database
     * @exception AccessDeniedException if do not have write-permission on the link source
     * @exception DataAccessException on database access/interaction failure
     * @exception IllegalArgumentException if supplied asset does not have a valid name
     * @exception AlreadyExistsException if asset save violates some uniqueness constraint
     */
    public <T extends Asset> T saveAsset(T a_asset,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Save (update or create) the given collection of assets in order
     * as a single transaction.
     *
     * @param v_assets to save in order
     * @param s_update_comment applied to all assets
     * @return updated assets
     */
    public Collection<Asset> saveAssetsInOrder(Collection<Asset> v_assets,
            String s_update_comment) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


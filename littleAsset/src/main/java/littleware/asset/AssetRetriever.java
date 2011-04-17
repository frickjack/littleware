/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;


import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.base.Maybe;
import littleware.base.NoSuchThingException;
import littleware.base.ReadOnly;
import littleware.security.AccessDeniedException;




/**
 * Interface for retrieving assets from various sources -
 * different implementations may enforce security and cacheing
 * to different degrees.
 * Intended for internal (between servers and managers) use only - 
 * clients should interact with littleware.asset via 
 * the AssetSearchManager implementation - which may manage multiple
 * AssetRetriever's under the hood.
 *
 * An AssetRetriever uses the AssetSpecializer associated with a loaded
 * asset's type to fill in the type-specific data for the asset.
 * An AssetRetriever must maintain state to avoid asset-retrieval cycles
 * with the AssetSpecializer narrow() callbacks.
 * The cycle-cache
 * also allows the AssetRetriever to avoid data explosion when
 * loading asset graphs that may access various assets via multiple paths.
 *
 * Does not extends Remote so we have the option of
 * sending cilents serializable proxies, but every method
 * does throw RemoteException so this interface is
 * ready for a Remote mixin.
 */
public interface AssetRetriever extends java.rmi.Remote {

    /**
     * Get the asset with the specified id.
     *
     * @param assetId of asset to retrieve
     * @return fully initialized asset.
     *           If the asset is a PRIINCIPAL or ACL AssetType,
     *           then the returned object will implent the Principal
     *           and Acl interfaces respectively.
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException some other failure condition
     */
    public
    @ReadOnly
    Maybe<Asset> getAsset(UUID assetId) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Get as many of the assets in the given collection of ids as possible.
     *
     * @param idSet set of asset ids to retrieve
     * @return list of assets loaded in order - 2 entries
     *                with the same id may reference the same object,
     *                skips ids that do not exist 
     * @throws NoSuchThingException if requested asset does not exist in the db
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException if some other failure condition
     */
    public
    @ReadOnly
    List<Asset> getAssets(Collection<UUID> idSet) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the Home assets this server has access to
     * for open-ended searches.
     *
     * @return mapping from home name to UUID.
     * @throws DataAccessException on database access/interaction failure
     * @throws AccessDeniedException if caller is not an administrator
     */
    public
    @ReadOnly
    Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the links (assets with a_source as their FROM-asset)
     * out of the given asset-id of the given type.
     * Caller must have READ-access to the source asset.
     *
     * @param fromId asset - result&apos;s FROM-asset
     * @param type to limit search to
     * @param stateto limit search to
     * @return mapping from child-name to child-id
     * @throws AccessDeniedException if caller does not have read access
     *                to a_source
     * @throws DataAccessException on database access/interaction failure
     * @throws IllegalArgumentExcetion if limit is out of bounds
     * @throws AssetException if limit is too large
     */
    public
    @ReadOnly
    Map<String, UUID> getAssetIdsFrom(UUID fromId,
            AssetType type, int state) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    public
    @ReadOnly
    Map<String, UUID> getAssetIdsFrom(UUID fromId,
            AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;



    public
    @ReadOnly
    Map<String, UUID> getAssetIdsFrom(UUID fromId
            ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

}


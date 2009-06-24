/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.*;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;


import littleware.base.*;
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
     * @param u_id of asset to retrieve
     * @return fully initialized asset.
     *           If the asset is a PRIINCIPAL or ACL AssetType,
     *           then the returned object will implent the Principal
     *           and Acl interfaces respectively.
     * @exception AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @exception DataAccessException on database access/interaction failure
     * @exception AssetException some other failure condition
     */
    public
    @ReadOnly
    Maybe<Asset> getAsset(UUID u_id) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Get as many of the assets in the given set of ids as possible.
     *
     * @param v_id set of asset ids to retrieve
     * @return set of fully initialized assets (no duplicates).
     *           If the asset is a PRIINCIPAL or ACL AssetType,
     *           then the returned object will implent the Principal
     *           and Acl interfaces respectively.
     * @exception NoSuchThingException if requested asset does not exist in the db
     * @exception AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @exception DataAccessException on database access/interaction failure
     * @exception AssetException if some other failure condition
     */
    public
    @ReadOnly
    Set<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the Home assets this server has access to
     * for open-ended searches.
     *
     * @return mapping from home name to UUID.
     * @exception DataAccessException on database access/interaction failure
     * @exception AccessDeniedException if caller is not an administrator
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
     * @param u_from asset - result&apos;s FROM-asset
     * @param n_type to limit search to - may be null
     * @return mapping from child-name to child-id
     * @exception AccessDeniedException if caller does not have read access
     *                to a_source
     * @exception DataAccessException on database access/interaction failure
     * @exception IllegalArgumentExcetion if limit is out of bounds
     * @exception AssetException if limit is too large
     */
    public
    @ReadOnly
    Map<String, UUID> getAssetIdsFrom(UUID u_from,
            AssetType<? extends Asset> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

}


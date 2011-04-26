/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.internal;

import java.util.*;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.rmi.Remote;

import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.ReadOnly;


/**
 * Asset-search interface.  Searches the local server database only.
 * Does not extends Remote so we have the option of
 * sending cilents serializable proxies, but every method
 * does throw RemoteException so this interface is
 * ready for a Remote mixin.
 */
public interface RemoteAssetSearchManager extends AssetRetriever, Remote {

    /**
     * Convenience method - equivalent to: <br />
     *              getAssetsByName ( ... ).getIterator ().next () <br />
     * Handy for asset-types that are name unique.
     *
     * @param s_name to retrieve
     * @param n_type must be unique-name type
     * @return the asset or null if none found    
     * @throws InavlidAssetTypeException if n_type is not name-unique
     */
    @ReadOnly
    public  Option<Asset> getByName(String name, AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;


    /**
     * Convenience method just retrieves the asset referenced by the
     * last AssetPath returned by getAssetsAlongPath
     *
     * @param path_asset to traverse
     * @return the asset at the end of the path
     * @throws GeneralSecurityException if caller does not have read-access
     *             to every asset along the path
     * @throws AssetPathTooLongException if traversal exceeds limit on number of assets
     * @throws LinkLoopException if a loop is detected during automatic link traversal
     */
    public 
    @ReadOnly
    Option<Asset> getAssetAtPath( AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the history of changes on the specified asset going back to the specified date.
     * Asset must be local to this server's database.
     *
     * @param u_id of asset to get history for
     * @param t_start earliest date to go back to in history search
     * @param t_end most recent date to go up to in history search
     * @throws NoSuchThingException if the given asset does not exist in the database
     * @throws AccessDeniedException if do not CURRENTLY have read-access to the asset
     * @throws DataAccessException on database access/interaction failure
     */
    public 
    @ReadOnly
    List<Asset> getAssetHistory( UUID u_id,  Date t_start,  Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the asset linking FROM the given parent asset and
     * with the given name
     *
     * @param u_from result&apos;s FROM-asset id
     * @param s_name of result asset
     * @throws NoSuchThingException if requested asset does not exist
     */
    public 
    @ReadOnly
    Option<Asset> getAssetFrom( UUID u_from,  String s_name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;


    /**
     * Method for a client to verify the transaction-counts
     * the client has in cache for a set of assets
     *
     * @param v_check mapping from asset id to transaction count to verify
     * @return subset of v_check that is incorrect with correct mapping
     *              from id to transaction-count, or mapping from id
     *              to null if the specified id has been deleted from the asset repository
     */
    public 
    @ReadOnly
    Map<UUID, Long> checkTransactionCount( Map<UUID, Long> checkMap ) throws BaseException, RemoteException;


    /**
     * Return the assets modified by the 100 most recent transaction after lMin
     *
     * @param homeId to restrict to
     * @param minTransaction
     * @return list of (transaction,asset-id) info in transaction order
     */
    public @ReadOnly List<IdWithClock> checkTransactionLog( UUID homeId, long minTransaction ) throws BaseException, RemoteException;

    /**
     * Get the links (assets with a_to as their TO-asset)
     * out of the given asset-id of the given type.
     * Caller must have READ-access to the a_to asset.
     *
     * @param u_to asset - result&apos;s TO-asset
     * @param n_type to limit search to - may NOT be null
     * @return ids of children of type n_type linking TO a_to
     * @throws AccessDeniedException if caller does not have read access
     *                to a_source
     * @throws DataAccessException on database access/interaction failure
     * @throws IllegalArgumentExcetion if limit is out of bounds
     * @throws AssetException if limit is too large 
     */
    public 
    @ReadOnly
    Set<UUID> getAssetIdsTo( UUID toId,
             AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
}


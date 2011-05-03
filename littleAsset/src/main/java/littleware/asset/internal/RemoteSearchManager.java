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
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.asset.IdWithClock;

import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.base.NoSuchThingException;
import littleware.base.Option;
import littleware.security.AccessDeniedException;


/**
 * Asset-search interface.  Searches the local server database only.
 * Does not extends Remote so we have the option of
 * sending cilents serializable proxies, but every method
 * does throw RemoteException so this interface is
 * ready for a Remote mixin.
 */
public interface RemoteSearchManager extends RemoteAssetRetriever, Remote {

    /**
     * Convenience method - equivalent to: <br />
     *              getAssetsByName ( ... ).getIterator ().next () <br />
     * Handy for asset-types that are name unique.
     *
     * @param name to retrieve
     * @param type must be unique-name type
     * @return the asset or null if none found    
     * @throws InavlidAssetTypeException if n_type is not name-unique
     */
    public  Option<Asset> getByName( UUID sessionId, String name, AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;



    /**
     * Get the history of changes on the specified asset going back to the specified date.
     * Asset must be local to this server's database.
     *
     * @param assetId of asset to get history for
     * @param start earliest date to go back to in history search
     * @param end most recent date to go up to in history search
     * @throws NoSuchThingException if the given asset does not exist in the database
     * @throws AccessDeniedException if do not CURRENTLY have read-access to the asset
     * @throws DataAccessException on database access/interaction failure
     */
    public List<Asset> getAssetHistory( UUID sessionId, UUID assetId,  Date start,  Date end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the asset linking FROM the given parent asset and
     * with the given name
     *
     * @param parentId result&apos;s FROM-asset id
     * @param name of result asset
     * @throws NoSuchThingException if requested asset does not exist
     */
    public Option<Asset> getAssetFrom( UUID sessionId, UUID parentId,  String name) throws BaseException, AssetException,
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
    public Map<UUID, Long> checkTransactionCount( UUID sessionId, Map<UUID, Long> checkMap ) throws BaseException, RemoteException;


    /**
     * Return the assets modified by the 100 most recent transaction after lMin
     *
     * @param homeId to restrict to
     * @param minTransaction
     * @return list of (transaction,asset-id) info in transaction order
     */
    public List<IdWithClock> checkTransactionLog( UUID sessionId, UUID homeId, long minTransaction ) throws BaseException, RemoteException;

    /**
     * Get the links (assets with a_to as their TO-asset)
     * out of the given asset-id of the given type.
     * Caller must have READ-access to the a_to asset.
     *
     * @param toId asset - result&apos;s TO-asset
     * @param n_type to limit search to - may NOT be null
     * @return ids of children of type n_type linking TO a_to
     * @throws AccessDeniedException if caller does not have read access
     *                to a_source
     * @throws DataAccessException on database access/interaction failure
     * @throws IllegalArgumentExcetion if limit is out of bounds
     * @throws AssetException if limit is too large 
     */
    public Set<UUID> getAssetIdsTo( UUID sessionId, UUID toId,
             AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
}


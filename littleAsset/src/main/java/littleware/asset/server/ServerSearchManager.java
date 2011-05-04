/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server;

import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
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
 * Server-side SearchManager implementation
 */
public interface ServerSearchManager {

    /**
     * Get the asset with the specified id.
     *
     * @param assetId of asset to retrieve
     * @return fully initialized asset.
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException some other failure condition
     */
    public Option<Asset> getAsset( LittleContext context, UUID assetId) throws BaseException,
            GeneralSecurityException;

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
    public List<Asset> getAssets( LittleContext context, Collection<UUID> idSet) throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Get the Home assets this server has access to
     * for open-ended searches.
     *
     * @return mapping from home name to UUID.
     * @throws DataAccessException on database access/interaction failure
     * @throws AccessDeniedException if caller is not an administrator
     */
    public Map<String, UUID> getHomeAssetIds( LittleContext context ) throws BaseException, AssetException,
            GeneralSecurityException;


    public Map<String, UUID> getAssetIdsFrom( 
            LittleContext context,
            UUID fromId,
            AssetType type) throws BaseException, AssetException,
            GeneralSecurityException;



    public Map<String, UUID> getAssetIdsFrom( 
            LittleContext context,
            UUID fromId
            ) throws BaseException, AssetException,
            GeneralSecurityException;



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
    public  Option<Asset> getByName( LittleContext context, String name, AssetType type) throws BaseException, AssetException,
            GeneralSecurityException;



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
    public List<Asset> getAssetHistory( LittleContext context, UUID assetId,  Date start,  Date end)
            throws BaseException, AssetException,
            GeneralSecurityException;

    /**
     * Get the asset linking FROM the given parent asset and
     * with the given name
     *
     * @param parentId result&apos;s FROM-asset id
     * @param name of result asset
     * @throws NoSuchThingException if requested asset does not exist
     */
    public Option<Asset> getAssetFrom( LittleContext context, UUID parentId,  String name) throws BaseException, AssetException,
            GeneralSecurityException;


    /**
     * Method for a client to verify the transaction-counts
     * the client has in cache for a set of assets
     *
     * @param checkMap mapping from asset id to transaction count to verify
     * @return subset of v_check that is incorrect with correct mapping
     *              from id to transaction-count, or mapping from id
     *              to null if the specified id has been deleted from the asset repository
     */
    public Map<UUID, Long> checkTransactionCount( LittleContext context, Map<UUID, Long> checkMap ) throws BaseException;


    /**
     * Return the assets modified by the 100 most recent transaction after lMin
     *
     * @param homeId to restrict to
     * @param minTransaction
     * @return list of (transaction,asset-id) info in transaction order
     */
    public List<IdWithClock> checkTransactionLog( LittleContext context, UUID homeId, long minTransaction ) throws BaseException;

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
    public Set<UUID> getAssetIdsTo( LittleContext context, UUID toId,
             AssetType type) throws BaseException, AssetException,
            GeneralSecurityException;

}

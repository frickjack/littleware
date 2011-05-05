/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import java.util.*;
import java.security.GeneralSecurityException;
import java.rmi.RemoteException;
import java.rmi.Remote;
import javax.naming.LinkLoopException;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetPath;
import littleware.asset.AssetType;
import littleware.asset.IdWithClock;

import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.base.NoSuchThingException;
import littleware.security.AccessDeniedException;

/**
 * Asset-search interface.  Searches the local server database only.
 * Does not extends Remote so we have the option of
 * sending cilents serializable proxies, but every method
 * does throw RemoteException so this interface is
 * ready for a Remote mixin.
 */
public interface AssetSearchManager extends Remote {

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
    public AssetRef getAsset(UUID assetId) throws BaseException,
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
    public List<Asset> getAssets(Collection<UUID> idSet) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the Home assets this server has access to
     * for open-ended searches.
     *
     * @return mapping from home name to UUID.
     * @throws DataAccessException on database access/interaction failure
     * @throws AccessDeniedException if caller is not an administrator
     */
    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;


    public Map<String, UUID> getAssetIdsFrom(UUID fromId,
            AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    public Map<String, UUID> getAssetIdsFrom(UUID fromId) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

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
    public AssetRef getByName(String name, AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Convenience method just retrieves the asset referenced by the
     * last AssetPath returned by getAssetsAlongPath
     *
     * @param path to traverse
     * @return the asset at the end of the path
     * @throws GeneralSecurityException if caller does not have read-access
     *             to every asset along the path
     * @throws AssetPathTooLongException if traversal exceeds limit on number of assets
     * @throws LinkLoopException if a loop is detected during automatic link traversal
     */
    public AssetRef getAssetAtPath(AssetPath path) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Generate a path rooted so that there is no backtrack.
     *
     * @see AssetPath#hasRootBacktrack()
     * @return hasRootBacktrack () ? (path rooted without backtrack) : pathIn
     */
    public AssetPath normalizePath(AssetPath pathIn) throws BaseException, GeneralSecurityException,
            RemoteException;

    /**
     * Convert path rooted at path with non-null fromId property
     * to a path rooted at the furthest reachable ancestor
     *
     * @param pathIn path to convert
     * @return pathIn.getRoot().getFromId() != null ? new rooted bath : pathIn
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     */
    public AssetPath toRootedPath ( AssetPath pathIn
                                    ) throws BaseException, GeneralSecurityException,
        RemoteException;

    /** Shortcut to create rooted path for asset with particular id */
    public AssetPath toRootedPath( UUID uAsset ) throws BaseException, GeneralSecurityException,
        RemoteException;


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
    public List<Asset> getAssetHistory(UUID u_id, Date t_start, Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Get the asset linking FROM the given parent asset and
     * with the given name
     *
     * @param from result&apos;s FROM-asset id
     * @param name of result asset
     * @throws NoSuchThingException if requested asset does not exist
     */
    public AssetRef getAssetFrom(UUID from, String name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;

    /**
     * Method for a client to verify the transaction-counts
     * the client has in cache for a set of assets
     *
     * @param checkMap mapping from asset id to transaction count to verify
     * @return subset of v_check that is incorrect with correct mapping
     *              from id to transaction-count, or mapping from id
     *              to null if the specified id has been deleted from the asset repository
     */
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> checkMap) throws BaseException, RemoteException;

    /**
     * Return the assets modified by the 100 most recent transaction after lMin
     *
     * @param homeId to restrict to
     * @param minTransaction
     * @return list of (transaction,asset-id) info in transaction order
     */
    public List<IdWithClock> checkTransactionLog(UUID homeId, long minTransaction) throws BaseException, RemoteException;

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
    public Set<UUID> getAssetIdsTo(UUID toId,
            AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
}
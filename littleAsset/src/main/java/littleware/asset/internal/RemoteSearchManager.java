/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
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

import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.base.Maybe;
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
public interface RemoteSearchManager extends Remote {
    public static class AssetResult implements java.io.Serializable {
        private State state;
        private Option<Asset> asset;
        
        public static enum State { 
            NO_SUCH_ASSET, 
            USE_YOUR_CACHE, 
            ASSET_IN_RESULT,
            ACCESS_DENIED
        };
        
        private AssetResult( State state, Option<Asset> asset ) {
            this.state = state;
            this.asset = asset;
        }
        
        /** For Serializable contract */
        private AssetResult() {}
        
        public State getState() { return state; }
        public Option<Asset> getAsset() { return asset; }
        
        // ----
        private static AssetResult useCache = new AssetResult( State.USE_YOUR_CACHE, Maybe.empty( Asset.class ) );
        private static AssetResult noAsset = new AssetResult( State.NO_SUCH_ASSET, Maybe.empty( Asset.class ) );

        
        public static AssetResult useCache() {
            return useCache;
        }
        public static AssetResult noSuchAsset() {
            return noAsset;
        }
        
        
        public static AssetResult build( Asset asset ) {
            return new AssetResult( State.ASSET_IN_RESULT, Maybe.something(asset));
        }
    }

    /**
     * Get the asset with the specified id.
     *
     * @param assetId of asset to retrieve
     * @param cacheTimestamp of entry in client's cache - -1L if client does not have a valid cache entry
     * @return fully initialized asset or indicator to use cache result
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException some other failure condition
     */
    public AssetResult getAsset( UUID sessionId, UUID assetId, long cacheTimestamp ) throws BaseException,
            GeneralSecurityException, RemoteException;

    /**
     * Get as many of the assets in the given collection of ids as possible.
     *
     * @param id2CacheTStamp mapping of id to retrieve to timestamp in client cache or -1L if not cached
     * @return id to result map
     * @throws NoSuchThingException if requested asset does not exist in the db
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException if some other failure condition
     */
    public Map<UUID,AssetResult> getAssets( UUID sessionId, Map<UUID,Long> idToCacheTStamp ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
    

    /**
     * Get the Home assets this server has access to
     * for open-ended searches.
     *
     * @return mapping from home name to UUID.
     * @throws DataAccessException on database access/interaction failure
     * @throws AccessDeniedException if caller is not an administrator
     */
    public Map<String, UUID> getHomeAssetIds( UUID sessionId ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;


    public Map<String, UUID> getAssetIdsFrom( UUID sessionId, UUID fromId,
            AssetType type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;



    public Map<String, UUID> getAssetIdsFrom( UUID sessionId, UUID fromId
            ) throws BaseException, AssetException,
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
     * Get the links (assets with a_to as their TO-asset)
     * out of the given asset-id of the given type.
     * Caller must have READ-access to the a_to asset.
     *
     * @param toId asset - result&apos;s TO-asset
     * @param type to limit search to - may NOT be null
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

    /**
     * Position in JNDI or RMI directory to bind/lookup this service
     */
    public static final String  LOOKUP_PATH = "littleware/SearchManager";

}


package littleware.asset.internal;

import com.google.common.collect.ImmutableList;
import littleware.asset.AssetInfo;
import com.google.common.collect.ImmutableMap;
import java.util.*;
import java.security.GeneralSecurityException;
import littleware.asset.client.RemoteException;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;

import littleware.base.BaseException;
import littleware.base.DataAccessException;
import littleware.security.AccessDeniedException;


/**
 * Same as SearchManager, but replaces LittleContext with sessionId
 */
public interface RemoteSearchManager {
    
    
    //----------------------------------

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
    public ImmutableMap<UUID,AssetResult> getAssets( UUID sessionId, Map<UUID,Long> idToCacheTStamp ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;
    

    /**
     * Get the Home assets this server has access to.
     *
     * @param cacheTimestamp newest timestamp of entries in client's cached value - -1L if client does not have a valid cache entry
     * @param sizeInCache the number of entries in the value cached by the client if any 
     * @return mapping from home name to UUID.
     * @throws DataAccessException on database access/interaction failure
     * @throws AccessDeniedException if caller is not an administrator
     */
    public InfoMapResult getHomeAssetIds( UUID sessionId, long cacheTimestamp, int sizeInCache 
            ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException;


    public InfoMapResult getAssetIdsFrom( UUID sessionId, UUID fromId,
            AssetType type, long cacheTimestamp, int sizeInCache
            ) throws BaseException, AssetException, GeneralSecurityException, RemoteException;



    public InfoMapResult getAssetIdsFrom( UUID sessionId, UUID fromId,
            long cacheTimestamp, int sizeInCache
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
    public  AssetResult getByName( UUID sessionId, String name, 
            AssetType type, long cacheTimestamp ) throws BaseException, AssetException,
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
    public ImmutableList<Asset> getAssetHistory( UUID sessionId, UUID assetId,  Date start,  Date end)
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
    public AssetResult getAssetFrom( UUID sessionId, 
            UUID parentId,  
            String name, long cacheTimestamp ) throws BaseException, AssetException,
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
    public InfoMapResult getAssetIdsTo( UUID sessionId, UUID toId,
             AssetType type, long cacheTimestamp, int sizeInCache
            ) throws BaseException, AssetException,
                    GeneralSecurityException, RemoteException;

    /**
     * Position in JNDI or RMI directory to bind/lookup this service
     */
    public static final String  LOOKUP_PATH = "littleware/SearchManager";

}


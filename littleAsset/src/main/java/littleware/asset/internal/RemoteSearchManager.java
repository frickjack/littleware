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
    
    public static abstract class TStampResult<T> {
        private State          state;
        private T              data;
        
        public static enum State { 
            NO_DATA, 
            USE_YOUR_CACHE,
            DATA_IN_RESULT,
            ACCESS_DENIED
        };
        
        protected TStampResult( State state, T data ) {
            this.state = state;
            this.data = data;
        }

        /** For serializable */
        protected TStampResult(){}
        
        public State getState() { return state; }
        public T getData() { return data; }
    }
    
    
    public static class AssetResult extends TStampResult<Optional<Asset>> {
        
        
        private AssetResult( TStampResult.State state, Optional<Asset> optAsset ) {
            super( state, optAsset );
        }
        
        /** For Serializable contract */
        private AssetResult() {}
        

        public Optional<Asset> getAsset() { return getData(); }
        
        // ----
        private static final AssetResult useCache = new AssetResult( TStampResult.State.USE_YOUR_CACHE, Optional.empty() );
        private static final AssetResult noAsset = new AssetResult( TStampResult.State.NO_DATA, Optional.empty() );
        
        public static AssetResult useCache() {
            return useCache;
        }
        public static AssetResult noSuchAsset() {
            return noAsset;
        }
        
        
        public static AssetResult build( Asset asset ) {
            return new AssetResult( TStampResult.State.DATA_IN_RESULT, Optional.ofNullable(asset));
        }
    }
    
    //----------------------------------

    /**
     * Result for listChildren, etc returns asset info in a name-keyed map
     * (to simplify name-based asset-path resolution - which is very common).     
     */
    public static class InfoMapResult extends TStampResult<ImmutableMap<String,AssetInfo>> {
        private long newestTimestamp = -1L;
        
        private InfoMapResult( TStampResult.State state, ImmutableMap<String,AssetInfo> data ) {
            super( state, data );
            for ( AssetInfo info : data.values() ) {
                if ( info.getTimestamp() > newestTimestamp ) {
                    newestTimestamp = info.getTimestamp();
                }
            }
        }
        
        /** For Serializable contract */
        private InfoMapResult() {}
        
        /**
         * Shortcut for optInfo.values.map( _.getTimestamp ).max
         */
        public long getNewestTimestamp() { return newestTimestamp; }
        

        
        // ----
        private static final ImmutableMap<String,AssetInfo> emptyMap = ImmutableMap.of();
        private static final InfoMapResult useCache = new InfoMapResult( TStampResult.State.USE_YOUR_CACHE, emptyMap );
        private static final InfoMapResult noData = new InfoMapResult( TStampResult.State.NO_DATA, emptyMap );

        
        public static InfoMapResult useCache() {
            return useCache;
        }
        public static InfoMapResult noData() {
            return noData;
        }
        
        
        public static InfoMapResult build( ImmutableMap<String,AssetInfo> infoMap ) {
            return new InfoMapResult( TStampResult.State.DATA_IN_RESULT, infoMap );
        }
    }
    
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


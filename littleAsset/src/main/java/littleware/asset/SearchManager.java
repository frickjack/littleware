package littleware.asset;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetInfo;
import littleware.asset.AssetType;
import littleware.asset.internal.RemoteSearchManager;
import littleware.base.BaseException;
import littleware.base.DataAccessException;

/**
 * Server-side SearchManager implementation
 */
public interface SearchManager {
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

    //---------------------------------
    public static class PageResult {
        public final String glob;
        public final String last;

        public PageResult(String glob, String last) {
            this.glob = glob;
            this.last = last;
        }
    }
    
    //----------------------------------

            
    /**
     * Get the asset at the specified path
     *
     * @param context
     * @param path
     * @param clientCacheTStamp
     * @return AssetResult
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException some other failure condition
     */
    AssetResult getAsset(LittleContext context, 
                String path, 
                long clientCacheTStamp
                ) throws BaseException;
    
    /**
     * Shortcut for getAsset( ..., -1L )
     */
    Optional<Asset> getAsset( LittleContext context, String path
             ) throws BaseException;
    

    /**
     * Get assets below the given path
     *
     * @param context
     * @param glob
     * @return list of assets loaded in order - 2 entries
     *                with the same id may reference the same object,
     *                skips ids that do not exist
     * @throws NoSuchThingException if requested asset does not exist in the db
     * @throws AccessDeniedException if caller does not have permission to read
     *                 the specified asset
     * @throws DataAccessException on database access/interaction failure
     * @throws AssetException if some other failure condition
     */
    PageResult getAssets(
        LittleContext context, 
        String glob, int maxDepth,
        int start, int size
        ) throws BaseException, AssetException;
}

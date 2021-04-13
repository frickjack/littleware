package littleware.asset.internal;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.GeneralSecurityException;
import java.sql.*;

import littleware.asset.*;
import littleware.asset.AssetSpecializerRegistry;
import littleware.asset.LittleContext;
import littleware.asset.LittleTransaction;
import littleware.asset.SearchManager;
import littleware.asset.db.*;
import littleware.base.*;
import littleware.db.*;


/**
 * Simple implementation of Asset-search interface. 
 */
@SuppressWarnings("unchecked")
public class SimpleSearchManager implements SearchManager {

    private static final Logger log = Logger.getLogger(SimpleSearchManager.class.getName());
    private final DbCommandManager dbMgr;

    /**
     * Constructor stashes DataSource, DbManager, and CacheManager
     */
    @Inject
    public SimpleSearchManager(DbCommandManager dbMgr
            ) {
        this.dbMgr = dbMgr;
    }

    /**
     * Internal helper to convert an unspecialized/secured asset
     * to a result suitable to return to a client
     * 
     * @param optUnspecialized
     * @param clientTimestamp
     */
    private AssetResult buildResult( LittleContext ctx, Optional<Asset> optUnspecialized, 
            long clientTimestamp 
            ) throws BaseException, AssetException, GeneralSecurityException {
        if ( ! optUnspecialized.isPresent()) {
            return AssetResult.noSuchAsset();
        }

        final Asset unspecial = optUnspecialized.get();
        // sanity check
        if ( unspecial.getId() == null ) {
            throw new IllegalArgumentException( "Unspecialized asset with NULL id" );
        }
        
        if ( unspecial.getTimestamp() <= clientTimestamp ) {
            return AssetResult.useCache();
        }
        
        return AssetResult.build( unspecial );
    }
    
    

    @Override
    public AssetResult getAsset(LittleContext ctx, String path, long clientCacheTStamp) throws BaseException, AssetException {
        if (null == path) {
            throw new IllegalArgumentException("null path");
        }

        final LittleTransaction trans = ctx.getTransaction();

        try {
            final DbReader<Asset, UUID> reader = dbMgr.makeDbAssetLoader(trans);
            final Asset result = reader.loadObject(id);
            return this.buildResult( ctx, Optional.ofNullable(result), clientCacheTStamp );

        } catch (SQLException ex) {
            log.log(Level.INFO, "Caught unexpected: ", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        }

    }


    @Override
    public Optional<Asset> getAsset( LittleContext context, String path
             ) throws BaseException {
        throw new UnsupportedOperationException("not yet implemented");
    }
    

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
    public PageResult getAssets(
        LittleContext context, 
        String glob, int maxDepth,
        int start, int size
        ) throws BaseException, AssetException {
        throw new UnsupportedOperationException("not yet implemented");
    }

}

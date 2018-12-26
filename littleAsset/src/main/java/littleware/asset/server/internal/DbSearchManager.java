package littleware.asset.server.internal;

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
import littleware.asset.internal.RemoteSearchManager;
import static littleware.asset.internal.RemoteSearchManager.AssetResult;
import littleware.asset.internal.RemoteSearchManager.InfoMapResult;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.ServerSearchManager;
import littleware.asset.server.db.*;
import littleware.base.*;
import littleware.db.*;
import littleware.security.AccessDeniedException;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittleGroupMember;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;

/**
 * Simple implementation of Asset-search interface. 
 * TODO - refactor DbManager to better support AssetInfo methods
 */
@SuppressWarnings("unchecked")
public class DbSearchManager implements ServerSearchManager {

    private static final Logger log = Logger.getLogger(DbSearchManager.class.getName());
    private final DbCommandManager dbMgr;
    private final AssetSpecializerRegistry specialRegistry;
    private final Provider<AssetInfo.Builder> infoFactory;

    /**
     * Constructor stashes DataSource, DbManager, and CacheManager
     */
    @Inject
    public DbSearchManager(DbCommandManager dbMgr,
            AssetSpecializerRegistry specialRegistry,
            Provider<AssetInfo.Builder> infoFactory
            ) {
        this.dbMgr = dbMgr;
        this.specialRegistry = specialRegistry;
        this.infoFactory = infoFactory;
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
        
        final Asset special = this.secureAndSpecialize( ctx, unspecial);
        return AssetResult.build( special );
    }
    
    
    private InfoMapResult buildIMapResult( LittleContext ctx, Collection<UUID> assetIds, long clientTimestamp, int clientSize ) throws BaseException {
        if ( assetIds.isEmpty() ) {
            return InfoMapResult.noData();
        }
        final ImmutableMap.Builder<String,AssetInfo> mapBuilder = ImmutableMap.builder();
        ctx.getTransaction().startDbAccess();
        try {
            for( UUID id: assetIds ) {
                // getAssetOrNullInsecure updates transaction accessCache
                final Asset unspecial = this.getAssetOrNullInsecure(ctx, id);
                final AssetInfo info = infoFactory.get().copyFromAsset( unspecial ).build();
                mapBuilder.put( info.getName(), info );
            }
        } finally {
            ctx.getTransaction().endDbAccess();
        }
        final InfoMapResult dataResult = InfoMapResult.build( mapBuilder.build() );
        if ( (dataResult.getNewestTimestamp() == clientTimestamp) && (dataResult.getData().size() == clientSize)) {
            return InfoMapResult.useCache();
        }
        return dataResult;
    }
    
    
    @Override
    public  AssetResult getByName( LittleContext ctx, String name, 
            AssetType type, long cacheTimestamp
            ) throws BaseException, AssetException,
            GeneralSecurityException 
    {
        if (!type.isNameUnique()) {
            throw new InvalidAssetTypeException("getByName requires name-unique type: " + type);
        }

        // cache miss
        final Optional<Asset> loadedAssets;
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();
        try {
            try {
                final DbReader<Optional<Asset>, String> reader = dbMgr.makeDbAssetsByNameLoader(trans, name, type);

                loadedAssets = reader.loadObject(null);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Failed query", ex);
                throw new DataAccessException("Failed query: " + ex);
            }

            return this.buildResult(ctx, loadedAssets, cacheTimestamp);
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public ImmutableList<Asset> getAssetHistory(LittleContext ctx, UUID id, java.util.Date start, java.util.Date end)
            throws BaseException, AssetException,
            GeneralSecurityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public AssetResult getAssetFrom(LittleContext ctx, UUID parentId, String name, long cacheTimestamp ) throws BaseException, AssetException,
            GeneralSecurityException {
        Optional<Asset> optResult = Optional.empty();
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();
        try {
            try {
                final DbReader<Optional<Asset>, String> reader = dbMgr.makeDbAssetByParentLoader(trans, name, parentId );

                optResult = reader.loadObject(null);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Failed query", ex);
                throw new DataAccessException("Failed query: " + ex);
            }

            return buildResult( ctx, optResult, cacheTimestamp );
        } finally {
            trans.endDbAccess(accessCache);
        }
    }


    @Override
    public InfoMapResult getAssetIdsTo(LittleContext ctx, UUID toId,
            AssetType type, long cacheTimestamp, int sizeInCache ) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        trans.startDbAccess();
        try {
            // first - make sure the caller has read-access to the "to" asset
            getAsset( ctx, toId );
            final DbReader<Set<UUID>, String> reader = dbMgr.makeDbAssetIdsToLoader(trans, toId, Optional.ofNullable(type));
            return this.buildIMapResult(ctx, reader.loadObject(null), cacheTimestamp, sizeInCache);
        } catch (SQLException ex) {
            log.log(Level.INFO, "Failed call", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess();
        }
    }


    @Override
    public AssetResult getAsset(LittleContext ctx, UUID id, long clientCacheTStamp) throws BaseException, AssetException,
            GeneralSecurityException {
        if (null == id) {
            throw new IllegalArgumentException("null id");
        }

        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final Asset result = getAssetOrNullInsecure(ctx, id);
            return this.buildResult( ctx, Optional.ofNullable(result), clientCacheTStamp );
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    // cache ACLs to improve performance ...
    /**
     * Internal method - shared with SimpleAssetSearchManager - to specialize,
     * and verify access-permissions on the given newly-loaded asset.
     *
     * @param unspecial just loaded asset
     * @return specialized asset whose access permission for the active user has been verified
     */
     <T extends Asset> T secureAndSpecialize(LittleContext ctx, T unspecial) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();
        boolean mustCleanCache = false;  // little state flag - see below
        
        try {    
            { // check transaction cache first
                final Asset result = accessCache.get( unspecial.getId() );
                if ( null != result ) {
                    return (T) result;
                }
            }

            //
            // avoid mess where specialize calls something like getAssetIdsFrom that
            //    in turn calls getAsset as a security check ... ugh!
            // will update cache with specialized asset once it's available, or
            // clean the unspecialized asset out of the cache if specialization fails ...
            //
            accessCache.put( unspecial.getId(), unspecial);
            mustCleanCache = true;

            final T special = specialRegistry.getService(unspecial.getAssetType()).narrow(ctx, unspecial);
            // update cycle cache
            accessCache.put(special.getId(), special);
            if (special.getAssetType().equals(LittleUser.USER_TYPE) || special.getAssetType().equals(LittleGroup.GROUP_TYPE) || special.getAssetType().equals(LittleGroupMember.GROUP_MEMBER_TYPE) || ( // acl-entry may be protected by its own ACL
                    special.getAssetType().equals(LittleAclEntry.ACL_ENTRY) && (null != special.getAclId()) && special.getAclId().equals(((LittleAclEntry) special).getOwningAclId()) && accessCache.containsKey(special.getAclId()))) {
                /**
                 * No access limitation on USER, GROUP -
                 * chicken/egg problem since need these guys to implement security.
                 */
                mustCleanCache = false;
                return special;
            }

            final LittleUser caller = ctx.getCaller();

            if (null == caller) {
                throw new AccessDeniedException("Unauthenticated caller");
            }

            if (caller.getId().equals(special.getOwnerId()) || ctx.isAdmin()) {
                // Owner can read his own freakin' asset
                mustCleanCache = false;
                return special;
            }
            // Need to check ACL
            if (!ctx.checkPermission(LittlePermission.READ, special.getAclId())) {
                throw new AccessDeniedException("Caller " + caller.getName() + 
                        " does not have permission to access asset "
                        + special.getName() + "(" + special.getAssetType() + 
                        ", " + special.getId() + ")"
                        );
            }

            mustCleanCache = false;
            return special;
        } catch (NoSuchElementException e) {
            throw new DataAccessException("Failure to specialize " + unspecial.getAssetType() + " type asset: " + unspecial.getName()
                    + ", caught: " + e, e);
        } finally {
            if ( mustCleanCache ) {
                accessCache.remove( unspecial.getId() );
            }
            trans.endDbAccess(accessCache);
        }
    }

    /**
     * Internal cycle-cache acception retriever - does not do security check,
     * adds result to the thread TransactionManager.
     */
    protected Asset getAssetOrNullInsecure(LittleContext ctx, UUID id) throws BaseException {
        if (null == id) {
            return null;
        }

        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            Asset result = accessCache.get(id);

            if (null != result) {
                return result;
            }
            try {
                final DbReader<Asset, UUID> reader = dbMgr.makeDbAssetLoader(trans);
                result = reader.loadObject(id);
            } catch (SQLException ex) {
                log.log(Level.INFO, "Caught unexpected: ", ex);
                throw new DataAccessException("Caught unexpected: " + ex);
            }

            return result;
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public ImmutableMap<UUID, AssetResult> getAssets(LittleContext ctx, Collection<UUID> idSet) throws BaseException, AssetException,
            GeneralSecurityException {
        final ImmutableMap.Builder<UUID, Long> builder = ImmutableMap.builder();
        for (UUID id : idSet) {
            builder.put(id, -1L);
        }
        return getAssets(ctx, builder.build());
    }

    @Override
    public ImmutableMap<UUID, AssetResult> getAssets(LittleContext ctx, Map<UUID, Long> id2Timestamp) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final ImmutableMap.Builder<UUID, AssetResult> resultBuilder = ImmutableMap.builder();
            for (Map.Entry<UUID, Long> entry : id2Timestamp.entrySet()) {
                try {
                    resultBuilder.put(entry.getKey(), getAsset(ctx, entry.getKey(), entry.getValue()));
                } catch (GeneralSecurityException ex) {
                    log.log(Level.FINE, "Skipping access denied asset", ex);
                }
            }
            return resultBuilder.build();
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public RemoteSearchManager.InfoMapResult getHomeAssetIds( LittleContext ctx,
            long cacheTimestamp, int sizeInCache ) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final DbReader<Map<String, UUID>, String> reader = dbMgr.makeDbHomeIdLoader(trans);
            return this.buildIMapResult(ctx, reader.loadObject(null).values(), cacheTimestamp, sizeInCache);
        } catch (SQLException ex) {
            log.log(Level.INFO, "Caught unexpected: ", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public RemoteSearchManager.InfoMapResult getAssetIdsFrom( 
            LittleContext ctx,
            UUID fromId, AssetType assetType,
            long cacheTimestamp, int sizeInCache ) throws BaseException, AssetException,
            GeneralSecurityException 
    {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            // first - verify that the caller has access to the parent asset
            getAsset( ctx, fromId );
            final DbReader<Map<String, UUID>, String> reader = dbMgr.makeDbAssetIdsFromLoader(trans, fromId, Optional.ofNullable((AssetType) assetType), Optional.empty() );
            return buildIMapResult( ctx, reader.loadObject(null).values(), cacheTimestamp, sizeInCache );
        } catch (SQLException ex) {
            // do not throw cause e - may not be serializable
            log.log(Level.INFO, "Failed call", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(accessCache);
        }
    }


    @Override
    public RemoteSearchManager.InfoMapResult getAssetIdsFrom( 
            LittleContext ctx,
            UUID fromId, long cacheTimestamp, int sizeInCache
            ) throws BaseException, AssetException,
            GeneralSecurityException {

        return getAssetIdsFrom(ctx, fromId, null, cacheTimestamp, sizeInCache );
    }

    @Override
    public Optional<Asset> getAsset(LittleContext context, UUID assetId) throws BaseException, GeneralSecurityException {
        return getAsset( context, assetId, -1L ).getAsset();
    }

    @Override
    public ImmutableMap<String, AssetInfo> getHomeAssetIds(LittleContext context) throws BaseException, AssetException, GeneralSecurityException {
        return getHomeAssetIds( context, -1L, 0 ).getData();
    }

    @Override
    public ImmutableMap<String, AssetInfo> getAssetIdsFrom(LittleContext context, UUID fromId, AssetType type) throws BaseException, AssetException, GeneralSecurityException {
        return getAssetIdsFrom( context, fromId, type, -1L, 0 ).getData();
    }

    @Override
    public ImmutableMap<String, AssetInfo> getAssetIdsFrom(LittleContext context, UUID fromId) throws BaseException, AssetException, GeneralSecurityException {
        return getAssetIdsFrom( context, fromId, -1L, 0 ).getData();
    }

    @Override
    public Optional<Asset> getByName(LittleContext context, String name, AssetType type) throws BaseException, AssetException, GeneralSecurityException {
        return getByName( context, name, type, -1L ).getData();
    }

    @Override
    public Optional<Asset> getAssetFrom(LittleContext context, UUID parentId, String name) throws BaseException, AssetException, GeneralSecurityException {
        return getAssetFrom( context, parentId, name, -1L ).getData();
    }

    @Override
    public ImmutableMap<String, AssetInfo> getAssetIdsTo(LittleContext context, UUID toId, AssetType type) throws BaseException, AssetException, GeneralSecurityException {
        return getAssetIdsTo( context, toId, type, -1L, 0 ).getData();
    }
}

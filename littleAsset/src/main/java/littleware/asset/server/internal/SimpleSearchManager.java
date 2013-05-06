/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.internal;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.GeneralSecurityException;
import java.sql.*;

import littleware.asset.*;
import static littleware.asset.internal.RemoteSearchManager.AssetResult;
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
import littleware.security.auth.LittleSession;

/**
 * Simple implementation of Asset-search interface.  
 */
public class SimpleSearchManager implements ServerSearchManager {

    private static final Logger log = Logger.getLogger(SimpleSearchManager.class.getName());
    private final DbAssetManager dbMgr;
    private final AssetSpecializerRegistry specialRegistry;

    /**
     * Constructor stashes DataSource, DbManager, and CacheManager
     */
    @Inject
    public SimpleSearchManager(DbAssetManager dbMgr,
            AssetSpecializerRegistry specialRegistry
            ) {
        this.dbMgr = dbMgr;
        this.specialRegistry = specialRegistry;
    }

    @Override
    public Option<Asset> getByName(LittleContext ctx, String name, AssetType type) throws BaseException, AssetException,
            GeneralSecurityException {
        if (!type.isNameUnique()) {
            throw new InvalidAssetTypeException("getByName requires name-unique type: " + type);
        }

        // cache miss
        final Option<Asset> loadedAssets;
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();
        try {
            try {
                final DbReader<Option<Asset>, String> reader = dbMgr.makeDbAssetsByNameLoader(trans, name, type);

                loadedAssets = reader.loadObject(null);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Failed query", ex);
                throw new DataAccessException("Failed query: " + ex);
            }

            if (loadedAssets.isEmpty()) {
                return Maybe.empty();
            }
            final Asset asset = loadedAssets.iterator().next();
            accessCache.put(asset.getId(), asset);
            return Maybe.something(secureAndSpecialize(ctx, asset));
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public List<Asset> getAssetHistory(LittleContext ctx, UUID id, java.util.Date start, java.util.Date end)
            throws BaseException, AssetException,
            GeneralSecurityException {
        throw new UnsupportedOperationException();
    }

    @Override
    public Option<Asset> getAssetFrom(LittleContext ctx, UUID parentId, String name) throws BaseException, AssetException,
            GeneralSecurityException {
        Option<Asset> result = Maybe.empty();
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();
        try {
            try {
                final DbReader<Option<Asset>, String> reader = dbMgr.makeDbAssetByParentLoader(trans, name, parentId );

                result = reader.loadObject(null);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Failed query", ex);
                throw new DataAccessException("Failed query: " + ex);
            }

            if (result.isEmpty()) {
                return Maybe.empty();
            }
            final Asset asset = result.iterator().next();
            accessCache.put(asset.getId(), asset);
            return Maybe.something(secureAndSpecialize(ctx, asset));
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    /** Need to code this guy up */
    @Override
    public Map<UUID, Long> checkTransactionCount(LittleContext ctx, Map<UUID, Long> checkMap) throws BaseException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();
        final Map<UUID, Long> result = new HashMap<UUID, Long>();

        try {
            for (Map.Entry<UUID, Long> entry_x : checkMap.entrySet()) {
                Asset a_check = getAssetOrNullInsecure(ctx, entry_x.getKey());
                if (null != a_check) {
                    // asset exists
                    if (a_check.getTimestamp()
                            > entry_x.getValue()) {
                        // client is out of date
                        result.put(a_check.getId(),
                                a_check.getTimestamp());
                        log.log(Level.FINE, "Transaction count missync for: {0}", a_check);
                    } else {
                        log.log(Level.FINE, "Transaction count ok for: {0}", a_check);
                    }
                } else { // asset does not exist
                    result.put(entry_x.getKey(), null);
                }
            }
        } finally {
            trans.endDbAccess(accessCache);
        }
        return result;
    }

    @Override
    public Set<UUID> getAssetIdsTo(LittleContext ctx, UUID toId,
            AssetType type) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        trans.startDbAccess();
        try {
            final DbReader<Set<UUID>, String> reader = dbMgr.makeDbAssetIdsToLoader(trans, toId, Maybe.something(type));
            return reader.loadObject(null);
        } catch (SQLException ex) {
            log.log(Level.INFO, "Failed call", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess();
        }
    }

    @Override
    public List<IdWithClock> checkTransactionLog(LittleContext ctx, UUID homeId, long minTransaction) throws BaseException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final DbReader<List<IdWithClock>, Long> sql_reader = dbMgr.makeLogLoader(trans, homeId);
            return sql_reader.loadObject(minTransaction);
        } catch (SQLException ex) {
            log.log(Level.INFO, "Failed call", ex);
            throw new DataAccessException("Data access error: " + ex);
        } finally {
            trans.endDbAccess(accessCache);
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
            {
                final Asset result = accessCache.get(id);

                if (null != result) {
                    return AssetResult.build(result);
                }
            }
            
            final Asset result = getAssetOrNullInsecure(ctx, id);
            /*
            {  // Fetch the raw asset from the database
                Asset lookup = null;

                if ((dbMgr instanceof littleware.asset.server.db.aws.AwsDbAssetManager) ) {
                        //&& (clientCacheTStamp > 0)) {
                    // Try to avoid doing a consistent database read if possible.
                    // Check database with a possibly inconsisten read if we're running on AWS
                    // when the client gives us its in-cache timestamp.
                    // This is a really hacky way to do this ..., but it's easy for now
                    try {
                        lookup = ((littleware.asset.server.db.aws.DbAssetLoader) dbMgr.makeDbAssetLoader(trans)).withConsistentRead(false).loadObject(id);
                        log.log(Level.FINE, "Checking AWS with inconsistent read ..., hit: {0}", null != lookup );
                    } catch (SQLException ex) {
                        log.log(Level.INFO, "Ignoring unexpected exception on cache-check: ", ex);
                    }
                    if ((null != lookup) ) { // && lookup.getAssetType().isTStampCache()) {
                        accessCache.put(lookup.getId(), lookup);
                    } else {
                        lookup = getAssetOrNullInsecure(ctx, id);
                    }
                } else {
                    lookup = getAssetOrNullInsecure(ctx, id);
                }
                result = lookup;
            }
             * 
             */

            if (null == result) {
                return AssetResult.noSuchAsset();
            }
            // No need to secure if referencing client cache
            if ((result.getTimestamp() <= clientCacheTStamp)
                    && result.getAssetType().isTStampCache()) {
                return AssetResult.useCache();
            }

            // Specialize the asset
            littleware.base.Whatever.get().check("Got a valid id", result.getId() != null);
            final Asset secure = secureAndSpecialize(ctx, result);
            return AssetResult.build(secure);
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    // cache ACLs to improve performance ...
    /**
     * Internal method - shared with SimpleAssetSearchManager - to specialize,
     * and verify access-permissions on the given newly-loaded asset.
     *
     * @param loaded just loaded asset
     * @return specialized asset whose access permission for the active user has been verified
     */
     <T extends Asset> T secureAndSpecialize(LittleContext ctx, T loaded) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final T result = specialRegistry.getService(loaded.getAssetType()).narrow(ctx, loaded);
            // update cycle cache
            accessCache.put(result.getId(), result);
            if (result.getAssetType().equals(LittleUser.USER_TYPE) || result.getAssetType().equals(LittleGroup.GROUP_TYPE) || result.getAssetType().equals(LittleGroupMember.GROUP_MEMBER_TYPE) || ( // acl-entry may be protected by its own ACL
                    result.getAssetType().equals(LittleAclEntry.ACL_ENTRY) && (null != result.getAclId()) && result.getAclId().equals(((LittleAclEntry) result).getOwningAclId()) && accessCache.containsKey(result.getAclId()))) {
                /**
                 * No access limitation on USER, GROUP -
                 * chicken/egg problem since need these guys to implement security.
                 */
                return result;
            }

            final LittleUser caller = ctx.getCaller();

            if (null == caller) {
                /** ... I don't think this is necessary any more ... ?
                if (result.getAssetType().equals(LittleSession.SESSION_TYPE)) {
                    
                     * Loophole to let unauthenticated session get session
                     * info to simplify session setup
                   
                    return result;
                }
                 *   */
                throw new AccessDeniedException("Unauthenticated caller");
            }

            if (caller.getId().equals(result.getOwnerId()) || ctx.isAdmin()) {
                // Owner can read his own freakin' asset
                return result;
            }
            // Need to check ACL
            if (!ctx.checkPermission(LittlePermission.READ, result.getAclId())) {
                throw new AccessDeniedException("Caller " + caller.getName() + 
                        " does not have permission to access asset "
                        + result.getName() + "(" + result.getAssetType() + 
                        ", " + result.getId() + ")"
                        );
            }

            return result;
        } catch (NoSuchThingException e) {
            throw new DataAccessException("Failure to specialize " + loaded.getAssetType() + " type asset: " + loaded.getName()
                    + ", caught: " + e, e);
        } finally {
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

            accessCache.put(id, result);

            return result;
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public Map<UUID, AssetResult> getAssets(LittleContext ctx, Collection<UUID> idSet) throws BaseException, AssetException,
            GeneralSecurityException {
        final ImmutableMap.Builder<UUID, Long> builder = ImmutableMap.builder();
        for (UUID id : idSet) {
            builder.put(id, -1L);
        }
        return getAssets(ctx, builder.build());
    }

    @Override
    public Map<UUID, AssetResult> getAssets(LittleContext ctx, Map<UUID, Long> id2Timestamp) throws BaseException, AssetException,
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
    public Map<String, UUID> getHomeAssetIds(LittleContext ctx) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final DbReader<Map<String, UUID>, String> reader = dbMgr.makeDbHomeIdLoader(trans);
            return reader.loadObject(null);
        } catch (SQLException ex) {
            log.log(Level.INFO, "Caught unexpected: ", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(LittleContext ctx, UUID sourceId,
            AssetType assetType) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final DbReader<Map<String, UUID>, String> reader = dbMgr.makeDbAssetIdsFromLoader(trans, sourceId, Maybe.something((AssetType) assetType), Maybe.NONE);
            return reader.loadObject(null);
        } catch (SQLException ex) {
            // do not throw cause e - may not be serializable
            log.log(Level.INFO, "Failed call", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    public Map<String, UUID> getAssetIdsFrom(LittleContext ctx, UUID parentId, AssetType assetType, int i_state) throws BaseException, AssetException, GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> accessCache = trans.startDbAccess();

        try {
            final DbReader<Map<String, UUID>, String> sql_reader = dbMgr.makeDbAssetIdsFromLoader(trans, parentId, Maybe.something((AssetType) assetType), Maybe.something(i_state));
            return sql_reader.loadObject(null);
        } catch (SQLException ex) {
            // do not throw cause - may not be serializable
            log.log(Level.INFO, "Failed call", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(accessCache);
        }
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(LittleContext ctx, UUID parentId) throws BaseException, AssetException, GeneralSecurityException {
        return getAssetIdsFrom(ctx, parentId, null);
    }
}

/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.internal;

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.GeneralSecurityException;
import java.sql.*;

import littleware.asset.*;
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
    private final AssetPathFactory pathFactory;
    private final AssetSpecializerRegistry specialRegistry;

    /**
     * Constructor stashes DataSource, DbManager, and CacheManager
     */
    @Inject
    public SimpleSearchManager(DbAssetManager dbMgr,
            AssetSpecializerRegistry specialRegistry,
            AssetPathFactory pathFactory ) {
        this.dbMgr = dbMgr;
        this.pathFactory = pathFactory;
        this.specialRegistry = specialRegistry;
    }

    @Override
    public Option<Asset> getByName( LittleContext ctx, String name, AssetType type) throws BaseException, AssetException,
            GeneralSecurityException {
        if (!type.isNameUnique()) {
            throw new InvalidAssetTypeException("getByName requires name-unique type: " + type);
        }

        // cache miss
        final Set<Asset> v_load;
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();
        try {
            try {
                DbReader<Set<Asset>, String> db_reader = dbMgr.makeDbAssetsByNameLoader(name, type);

                v_load = db_reader.loadObject(null);
            } catch (SQLException ex) {
                log.log(Level.SEVERE, "Failed query", ex);
                throw new DataAccessException("Failed query: " + ex);
            }

            if (v_load.isEmpty()) {
                return Maybe.empty("No asset " + name + "/:type:" + type);
            }
            final Asset a_load = v_load.iterator().next();
            v_cycle_cache.put(a_load.getId(), a_load);
            return Maybe.something( secureAndSpecialize(ctx,a_load));
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public List<Asset> getAssetHistory(LittleContext ctx, UUID u_id, java.util.Date t_start, java.util.Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException {
        throw new UnsupportedOperationException();
    }


    @Override
    public Option<Asset> getAssetFrom( LittleContext ctx, UUID parentId, String name) throws BaseException, AssetException,
            GeneralSecurityException {
        UUID id = getAssetIdsFrom(ctx, parentId, null).get(name);
        if (null == id) {
            return Maybe.empty("Asset " + parentId + " has no child named " + name);
        }
        return getAsset(ctx, id);
    }

    /** Need to code this guy up */
    @Override
    public Map<UUID, Long> checkTransactionCount( LittleContext ctx, Map<UUID, Long> checkMap) throws BaseException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cache = trans.startDbAccess();
        final Map<UUID, Long> v_result = new HashMap<UUID, Long>();

        try {
            for (Map.Entry<UUID, Long> entry_x : checkMap.entrySet()) {
                Asset a_check = getAssetOrNullInsecure(ctx, entry_x.getKey());
                if (null != a_check) {
                    // asset exists
                    if (a_check.getTimestamp() >
                            entry_x.getValue()) {
                        // client is out of date
                        v_result.put(a_check.getId(),
                                a_check.getTimestamp());
                        log.log(Level.FINE, "Transaction count missync for: {0}", a_check);
                    } else {
                        log.log(Level.FINE, "Transaction count ok for: {0}", a_check);
                    }
                } else { // asset does not exist
                    v_result.put(entry_x.getKey(), null);
                }
            }
        } finally {
            trans.endDbAccess(v_cache);
        }
        return v_result;
    }

    @Override
    public Set<UUID> getAssetIdsTo( LittleContext ctx, UUID toId,
            AssetType type ) throws BaseException, AssetException,
            GeneralSecurityException {
        try {
            final DbReader<Set<UUID>, String> sql_reader = dbMgr.makeDbAssetIdsToLoader(toId, type );
            return sql_reader.loadObject(null);
        } catch (SQLException ex) {
            log.log( Level.INFO, "Failed call", ex );
            throw new DataAccessException("Caught unexpected: " + ex);
        }
    }

    @Override
    public List<IdWithClock> checkTransactionLog( LittleContext ctx, UUID homeId, long minTransaction) throws BaseException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cache = trans.startDbAccess();

        try {
            final DbReader<List<IdWithClock>, Long> sql_reader = dbMgr.makeLogLoader( homeId );
            return sql_reader.loadObject( minTransaction );
        } catch ( SQLException ex ) {
            log.log( Level.INFO, "Failed call", ex );
            throw new DataAccessException( "Data access error: " + ex );
        } finally {
            trans.endDbAccess(v_cache);
        }
    }


    @Override
    public Option<Asset> getAsset( LittleContext ctx, UUID id) throws BaseException, AssetException,
            GeneralSecurityException {
        if (null == id) {
            return Maybe.empty("Null id passed to getAsset");
        }

        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            Asset a_result = v_cycle_cache.get(id);

            if (null != a_result) {
                // no need to secure - already secure in cache
                return Maybe.something(a_result);
            }

            a_result = getAssetOrNullInsecure(ctx,id);
            if (null == a_result) {
                return Maybe.empty("No asset with id: " + id);
            }

            // Specialize the asset
            littleware.base.Whatever.get().check("Got a valid id", a_result.getId() != null);
            final Asset aSecure = secureAndSpecialize(ctx,a_result);
            return Maybe.something(aSecure);
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    // cache ACLs to improve performance ...
    /**
     * Internal method - shared with SimpleAssetSearchManager - to specialize,
     * and verify access-permissions on the given newly-loaded asset.
     *
     * @param a_loaded just loaded asset
     * @return specialized asset whose access permission for the active user has been verified
     */
    <T extends Asset> T secureAndSpecialize( LittleContext ctx, T a_loaded) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            final T a_result = specialRegistry.getService(a_loaded.getAssetType()).narrow(ctx, a_loaded);
            // update cycle cache
            v_cycle_cache.put(a_result.getId(), a_result);
            if (a_result.getAssetType().equals(LittleUser.USER_TYPE) || a_result.getAssetType().equals(LittleGroup.GROUP_TYPE) || a_result.getAssetType().equals(LittleGroupMember.GROUP_MEMBER_TYPE) || ( // acl-entry may be protected by its own ACL
                    a_result.getAssetType().equals(LittleAclEntry.ACL_ENTRY) && (null != a_result.getAclId()) && a_result.getAclId().equals( ((LittleAclEntry) a_result).getOwningAclId()) && v_cycle_cache.containsKey(a_result.getAclId()))) {
                /**
                 * No access limitation on USER, GROUP -
                 * chicken/egg problem since need these guys to implement security.
                 */
                return a_result;
            }

            final LittleUser caller = ctx.getCaller();

            if (null == caller) {

                if (a_result.getAssetType().equals(LittleSession.SESSION_TYPE)) {
                    /**
                     * Loophole to let unauthenticated session get session
                     * info to simplify session setup
                     */
                    return a_result;
                }

                throw new AccessDeniedException("Unauthenticated caller");
            }

            if (caller.getId().equals(a_result.getOwnerId()) || ctx.isAdmin() ) {
                // Owner can read his own freakin' asset
                return a_result;
            }
            // Need to check ACL
            if (!ctx.checkPermission( LittlePermission.READ, a_result.getAclId())) {
                throw new AccessDeniedException("Caller " + caller.getName() + " does not have permission to access asset " +
                        a_result.getName() + "(" + a_result.getAssetType() + ", " + a_result.getId() + ")");
            }

            return a_result;
        } catch (NoSuchThingException e) {
            throw new DataAccessException("Failure to specialize " + a_loaded.getAssetType() + " type asset: " + a_loaded.getName() +
                    ", caught: " + e, e);
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    /**
     * Internal cycle-cache acception retriever - does not do security check,
     * adds result to the thread TransactionManager.
     */
    protected Asset getAssetOrNullInsecure( LittleContext ctx, UUID u_id) throws BaseException {
        if (null == u_id) {
            return null;
        }

        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            Asset a_result = v_cycle_cache.get(u_id);

            if (null != a_result) {
                return a_result;
            }
            try {
                DbReader<Asset, UUID> sql_reader = dbMgr.makeDbAssetLoader();
                a_result = sql_reader.loadObject(u_id);
            } catch (SQLException ex) {
                log.log(Level.INFO, "Caught unexpected: ", ex);
                throw new DataAccessException("Caught unexpected: " + ex);
            }

            v_cycle_cache.put(u_id, a_result);

            return a_result;
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public List<Asset> getAssets( LittleContext ctx, Collection<UUID> v_id) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            final List<Asset> v_result = new ArrayList<Asset>();
            for (UUID u_id : v_id) {
                final Option<Asset> maybe = getAsset(ctx, u_id);
                if (maybe.isSet()) {
                    v_result.add(maybe.get());
                }
            }
            return v_result;
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public Map<String, UUID> getHomeAssetIds( LittleContext ctx ) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            final DbReader<Map<String, UUID>, String> sql_reader = dbMgr.makeDbHomeIdLoader();
            return sql_reader.loadObject(null);
        } catch (SQLException ex) {
            log.log(Level.INFO, "Caught unexpected: ", ex);
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom( LittleContext ctx, UUID u_source,
            AssetType n_type) throws BaseException, AssetException,
            GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            final DbReader<Map<String, UUID>, String> sql_reader = dbMgr.makeDbAssetIdsFromLoader(u_source, Maybe.emptyIfNull((AssetType) n_type), Maybe.empty(Integer.class));
            return sql_reader.loadObject(null);
        } catch (SQLException ex) {
            // do not throw cause e - may not be serializable
            log.log( Level.INFO, "Failed call", ex );
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }


    public Map<String, UUID> getAssetIdsFrom( LittleContext ctx, UUID parentId, AssetType assetType, int i_state) throws BaseException, AssetException, GeneralSecurityException {
        final LittleTransaction trans = ctx.getTransaction();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            final DbReader<Map<String, UUID>, String> sql_reader = dbMgr.makeDbAssetIdsFromLoader(parentId, Maybe.something((AssetType) assetType), Maybe.something(i_state));
            return sql_reader.loadObject(null);
        } catch (SQLException ex) {
            // do not throw cause - may not be serializable
            log.log( Level.INFO, "Failed call", ex );
            throw new DataAccessException("Caught unexpected: " + ex);
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom( LittleContext ctx, UUID parentId ) throws BaseException, AssetException, GeneralSecurityException {
        return getAssetIdsFrom( ctx, parentId , null);
    }

}


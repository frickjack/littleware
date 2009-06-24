/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server;

import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.GeneralSecurityException;
import java.security.acl.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.db.*;
import littleware.asset.server.db.*;
import littleware.security.*;

/**
 * AssetRetriever implementation retrieves assets
 * from this applications local database via
 * the published littleware.asset.CacheManager and
 * littleware.asset.db.DbManager.
 * We want to allow backend app-clusters to retrieve assets from
 * each other eventually, and not require authentication/access-control
 * between those trusted servers.
 */
public class LocalAssetRetriever implements AssetRetriever {

    private static final Logger olog_generic = Logger.getLogger("littleware.asset.server.LocalAssetRetriever");
    private final DbAssetManager om_db;
    private final CacheManager   om_cache;
    private final AssetSpecializerRegistry  oregistry_special;
    private final Provider<LittleTransaction>        oprovideTrans;

    /**
     * Constructor stashes DbManager, and CacheManager
     */
    public LocalAssetRetriever(DbAssetManager m_db,
            CacheManager m_cache,
            AssetSpecializerRegistry registry_special,
            Provider<LittleTransaction>  provideTrans
            ) {
        om_db = m_db;
        om_cache = m_cache;
        oregistry_special = registry_special;
        oprovideTrans = provideTrans;
    }


    @Override
    public Maybe<Asset> getAsset(UUID u_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == u_id) {
            return Maybe.empty( "Null id passed to getAsset");
        }

        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            Asset a_result = v_cycle_cache.get(u_id);

            if (null != a_result) {
                // no need to secure - already secure in cache
                return Maybe.something(a_result);
            }

            a_result = getAssetOrNullInsecure(u_id);
            if (null == a_result) {
                return Maybe.empty( "No asset with id: " + u_id );
            }

            // Specialize the asset
            littleware.base.Whatever.check("Got a valid id", a_result.getObjectId() != null);
            return Maybe.something( secureAndSpecialize(a_result) );
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    /**
     * Internal method - shared with SimpleAssetSearchManager - to specialize,
     * and verify access-permissions on the given newly-loaded asset.
     *
     * @param a_loaded just loaded asset
     * @return specialized asset whose access permission for the active user has been verified
     */
    <T extends Asset> T secureAndSpecialize(T a_loaded) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            final T a_result = oregistry_special.getService( a_loaded.getAssetType() ).narrow(a_loaded, this);

            if (a_result.getAssetType().equals(SecurityAssetType.USER) || a_result.getAssetType().equals(SecurityAssetType.GROUP) || a_result.getAssetType().equals(SecurityAssetType.GROUP_MEMBER) || ( // acl-entry may be protected by its own ACL
                    a_result.getAssetType().equals(SecurityAssetType.ACL_ENTRY) && (null != a_result.getAclId()) && a_result.getAclId().equals(a_result.getFromId()) && v_cycle_cache.containsKey(a_result.getAclId()))) {
                /**
                 * No access limitation on USER, GROUP - 
                 * chicken/egg problem since need these guys to implement security.
                 */
                return a_result;
            }

            final LittleUser p_caller = SecurityAssetType.getAuthenticatedUserOrNull();

            if (null == p_caller) {

                if (a_result.getAssetType().equals(SecurityAssetType.SESSION)) {
                    /**
                     * Loophole to let unauthenticated session get session 
                     * info to simplify session setup
                     */
                    return a_result;
                }

                throw new AccessDeniedException("Unauthenticated caller");
            }

            final Owner owner_asset = a_result.getOwner(this);
            if ((null != owner_asset) && (owner_asset.isOwner(p_caller))) {
                // Owner can read his own freakin' asset
                return a_result;
            }
            // Need to check ACL
            final LittleAcl acl_asset = a_result.getAcl(this);
            if (null == acl_asset) {
                throw new AccessDeniedException("Caller " + p_caller.getName() +
                        " does not have permission to access null-acl asset " +
                        a_result.getObjectId());
            }
            if (!acl_asset.checkPermission(p_caller, LittlePermission.READ)) {
                throw new AccessDeniedException("Caller " + p_caller.getName() + " does not have permission to access asset " +
                        a_result.getName() + "(" + a_result.getObjectId() + ")"
                        );
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
    protected Asset getAssetOrNullInsecure(UUID u_id) throws BaseException, RemoteException {
        if (null == u_id) {
            return null;
        }

        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            Asset a_result = v_cycle_cache.get(u_id);

            if (null != a_result) {
                return a_result;
            }

            try {
                final Maybe<Asset> maybeResult = om_cache.getAsset(u_id);
                if ( maybeResult.isSet() ) {
                    a_result = maybeResult.get();
                }
            } catch (CacheMissException e) {
            } catch (GeneralSecurityException e) {
                throw new AssertionFailedException("Cache should not do security check");
            }

            if (null == a_result) { // cache miss
                try {
                    DbReader<Asset, UUID> sql_reader = om_db.makeDbAssetLoader();
                    a_result = sql_reader.loadObject(u_id);
                } catch (SQLException e) {
                    olog_generic.log(Level.INFO, "Caught unexpected: " + e);
                    throw new DataAccessException("Caught unexpected: " + e);
                }

                om_cache.put(u_id, a_result);
            }
            v_cycle_cache.put(u_id, a_result);

            return a_result;
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public Set<Asset> getAssets(Collection<UUID> v_id) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            Set<Asset> v_result = new HashSet<Asset>();
            for (UUID u_id : v_id) {
                final Maybe<Asset> maybe = getAsset(u_id);
                if ( maybe.isSet() ) {
                    v_result.add( maybe.get() );
                }
            }
            return v_result;
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public Map<String, UUID> getHomeAssetIds() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        Map<String, UUID> v_result = null;

        try {
            return om_cache.getHomeAssetIds();
        } catch (CacheMissException e) {
        }

        // cache miss
        try {
            DbReader<Map<String, UUID>, String> sql_reader = om_db.makeDbHomeIdLoader();
            v_result = sql_reader.loadObject(null);
        } catch (SQLException e) {
            olog_generic.log(Level.INFO, "Caught unexpected: " + e);
            throw new DataAccessException("Caught unexpected: " + e);
        }

        om_cache.setHomeAssetIds(v_result);

        return v_result;
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_source,
            AssetType<? extends Asset> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        Map<String, UUID> v_result = null;

        try {
            return om_cache.getAssetIdsFrom(u_source, n_type);
        } catch (CacheMissException e) {
        }

        // cache miss
        try {
            DbReader<Map<String, UUID>, String> sql_reader = om_db.makeDbAssetIdsFromLoader(u_source, n_type);
            v_result = sql_reader.loadObject(null);
        } catch (SQLException e) {
            throw new DataAccessException("Caught unexpected: " + e, e);
        }

        om_cache.setAssetIdsFrom(u_source, n_type, v_result);
        return v_result;
    }

    public String getSourceName() {
        return "local";
    }
}



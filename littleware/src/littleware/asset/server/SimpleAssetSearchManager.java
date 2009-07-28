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

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.GeneralSecurityException;
import java.sql.*;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.*;
import littleware.db.*;

/**
 * Simple implementation of Asset-search interface.  
 */
public class SimpleAssetSearchManager extends LocalAssetRetriever implements AssetSearchManager {

    private static final Logger olog_generic = Logger.getLogger(SimpleAssetSearchManager.class.getName());
    private final DbAssetManager om_db;
    private final Provider<LittleTransaction> oprovideTrans;
    private final AssetPathFactory opathFactory = new SimpleAssetPathFactory(this);

    /**
     * Constructor stashes DataSource, DbManager, and CacheManager
     */
    @Inject
    public SimpleAssetSearchManager(DbAssetManager m_db,
            AssetSpecializerRegistry registry_special,
            Provider<LittleTransaction> provideTrans,
            PermissionCache  cachePermission ) {
        super(m_db, registry_special, provideTrans, cachePermission );
        om_db = m_db;
        oprovideTrans = provideTrans;
    }

    @Override
    public <T extends Asset> Maybe<T> getByName(String s_name, AssetType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (!n_type.isNameUnique()) {
            throw new InvalidAssetTypeException("getByName requires name-unique type: " + n_type);
        }

        // cache miss
        final Set<Asset> v_load;
        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();
        try {
            try {
                DbReader<Set<Asset>, String> db_reader = om_db.makeDbAssetsByNameLoader(s_name, n_type);

                v_load = db_reader.loadObject(null);
            } catch (SQLException e) {
                olog_generic.log(Level.SEVERE, "Caught unexpected: " + e);
                throw new DataAccessException("Unexpected caught: " + e, e);
            }

            if (v_load.isEmpty()) {
                return Maybe.empty("No asset " + s_name + "/:type:" + n_type);
            }
            final Asset a_load = v_load.iterator().next();
            v_cycle_cache.put(a_load.getObjectId(), a_load);
            return Maybe.something((T) secureAndSpecialize(a_load));
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, java.util.Date t_start, java.util.Date t_end)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException();
    }

    public SortedMap<AssetPath, Maybe<Asset>> getAssetsAlongPath(AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        // setup a cycle cache
        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cycle_cache = trans.startDbAccess();

        try {
            if (path_asset.hasRootBacktrack()) {
                return getAssetsAlongPath(opathFactory.normalizePath(path_asset));
            }

            SortedMap<AssetPath, Maybe<Asset>> mapResult = null;
            String s_path = path_asset.toString();
            Maybe<Asset> maybeResult = null;

            if (path_asset.hasParent()) {
                // else get parent
                // recursion!
                mapResult = getAssetsAlongPath(path_asset.getParent());

                if (mapResult.size() > 20) {
                    throw new TraverseTooLongException("Path traversal (" + path_asset +
                            ") exceeds 20 assets at " + s_path);
                }

                final Maybe<Asset> maybeParent = mapResult.get(mapResult.lastKey());
                String s_name = s_path.substring(s_path.lastIndexOf("/") + 1);

                if (!maybeParent.isSet()) {
                    maybeResult = maybeParent;
                } else if (s_name.equals("@")) {
                    if (null == maybeParent.get().getToId()) {
                        maybeResult = Maybe.empty("Link parent has null to-id: " + s_path);
                    } else {
                        maybeResult = getAsset(maybeParent.get().getToId());
                    }
                } else {
                    maybeResult = getAssetFrom(maybeParent.get().getObjectId(), s_name);
                }

            } else {
                mapResult = new TreeMap();
                maybeResult = path_asset.getRoot(this);
            }


            for (int i_link_count = 0;
                    maybeResult.isSet() && maybeResult.get().getAssetType().equals(AssetType.LINK) && (maybeResult.get().getToId() != null);
                    ++i_link_count) {
                if (i_link_count > 5) {
                    throw new LinkLimitException("Traversal exceeded 5 link limit at " + s_path);
                }
                maybeResult = getAsset(maybeResult.get().getToId());
            }

            mapResult.put(path_asset, maybeResult);
            return mapResult;
        } finally {
            trans.endDbAccess(v_cycle_cache);
        }
    }

    @Override
    public Maybe<Asset> getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        final SortedMap<AssetPath, Maybe<Asset>> v_path = getAssetsAlongPath(path_asset);
        return v_path.get(v_path.lastKey());
    }

    @Override
    public Maybe<Asset> getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        UUID u_id = getAssetIdsFrom(u_from, null).get(s_name);
        if (null == u_id) {
            return Maybe.empty("Asset " + u_from + " has no child named " + s_name);
        }
        return getAsset(u_id);
    }

    /** Need to code this guy up */
    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException, RemoteException {
        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cache = trans.startDbAccess();
        final Map<UUID, Long> v_result = new HashMap<UUID, Long>();

        try {
            for (Map.Entry<UUID, Long> entry_x : v_check.entrySet()) {
                Asset a_check = getAssetOrNullInsecure(entry_x.getKey());
                if (null != a_check) {
                    // asset exists
                    if (a_check.getTransactionCount() >
                            entry_x.getValue()) {
                        // client is out of date
                        v_result.put(a_check.getObjectId(),
                                a_check.getTransactionCount());
                        olog_generic.log(Level.FINE, "Transaction count missync for: " + a_check);
                    } else {
                        olog_generic.log(Level.FINE, "Transaction count ok for: " + a_check);
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
    public Set<UUID> getAssetIdsTo(UUID u_to,
            AssetType<? extends Asset> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        try {
            final DbReader<Set<UUID>, String> sql_reader = om_db.makeDbAssetIdsToLoader(u_to, n_type);
            return sql_reader.loadObject(null);
        } catch (SQLException e) {
            throw new DataAccessException("Caught unexpected: " + e);
        }
    }

    @Override
    public List<IdWithClock> checkTransactionLog( UUID homeId, long minTransaction) throws BaseException, RemoteException {
        final LittleTransaction trans = oprovideTrans.get();
        final Map<UUID, Asset> v_cache = trans.startDbAccess();
        final List<IdWithClock> result = new ArrayList<IdWithClock>();

        try {
            final DbReader<List<IdWithClock>, Long> sql_reader = om_db.makeLogLoader( homeId );
            return sql_reader.loadObject( minTransaction );
        } catch ( SQLException ex ) {
            throw new DataAccessException( "Data access error: " + ex );
        } finally {
            trans.endDbAccess(v_cache);
        }
    }
}


/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.internal;

import littleware.base.cache.Cache;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;
import java.security.GeneralSecurityException;

import littleware.asset.*;
import littleware.asset.server.CacheManager;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.*;
import littleware.base.*;
import littleware.base.cache.InMemoryCacheBuilder;
import littleware.security.*;
import littleware.db.*;

/**
 * Simple implementation of CacheManager maintains
 * an internal UUID to Asset map and local-only
 * Apache derby database.
 * This class is implemented as a singleton, 
 * and is syncrhonized.
 * If TransactionManager.isInTransaction, then miss on
 * all cache lookups, and defer save ops by passing them
 * to the TransactionManager.deferTillOutOfTransaction() method, since they might
 * finally be rolled back in the end.
 */
public class SimpleCacheManager implements CacheManager {

    private static final Logger log = Logger.getLogger( SimpleCacheManager.class.getName() );

    public final static int OI_MAXSIZE = 100000;
    public final static int OI_MAXSECS = 1000000; // no max age
    /**
     * Need to track null cache-entries separately,
     * since littleware.base.cache.Cache assumes non-null values.
     */
    private final Set<UUID> ov_null_entries = Collections.synchronizedSet(new HashSet<UUID>());
    private final Cache<UUID, Asset> ocache_asset;
    /** DbManager action factory - access via getDbManager () method */
    private final DbCacheManager om_db;
    private final Provider<LittleTransaction> oprovideTrans;


    /** 
     * Inject dependencies - setup the singleton
     *
     * @param m_dbcache db-manager for cache
     * @exception SingletonException after the 1st time this gets called
     */
    @Inject
    public SimpleCacheManager( DbCacheManager m_dbcache, 
            Provider<LittleTransaction> provideTrans,
            InMemoryCacheBuilder cacheBuilder
            ) {
        om_db = m_dbcache;
        oprovideTrans = provideTrans;
         ocache_asset = cacheBuilder.maxAgeSecs( OI_MAXSECS ).maxSize(OI_MAXSIZE).build();
    }

    @Override
    public Cache.Policy getPolicy() {
        return ocache_asset.getPolicy();
    }

    @Override
    public int getMaxSize() {
        return ocache_asset.getMaxSize();
    }

    @Override
    public int getMaxEntryAgeSecs() {
        return ocache_asset.getMaxEntryAgeSecs();
    }

    /** 
     * Actually clone()s a copy of a_value, and puts that in the cache.
     * Defers data-save till end of transaction if LittleTransaction.isInTransaction.
     */
    @Override
    public synchronized Asset put(final UUID u_key, final Asset a_value) {
        final LittleTransaction trans_maindb = oprovideTrans.get();
        if (trans_maindb.isDbUpdating()) {
            // Defer saving new data till out of transaction
            trans_maindb.deferTillTransactionEnd(
                    new Runnable() {

                @Override
                        public void run() {
                            put(u_key, a_value);
                        }
                    });
            return null;
        }
        try {
            if (null == a_value) {
                ov_null_entries.add(u_key);
                return ocache_asset.remove(u_key);
            } else {
                Whatever.get().check("Key must go with value in asset-cache", a_value.getId().equals(u_key));

                JdbcDbWriter<Asset> db_writer = om_db.makeDbAssetSaver();
                db_writer.saveObject( a_value);

                ov_null_entries.remove(u_key);
                return ocache_asset.put(u_key, a_value);
            }
        } catch (SQLException e) {
            throw new AssertionFailedException("Failure updating cache, caught: " + e, e);
        }
    }

    /** Actually returns a clone of the value if it's not null and ! LittleTransaction.isInTransaction */
    @Override
    public Asset get(UUID u_key) {
        if (oprovideTrans.get().isDbUpdating()) {
            return null;
        }
        Asset a_result = ocache_asset.get(u_key);

        if (null == a_result) {
            return null;
        }
        return a_result;
    }

    /**
     * Defers operation if LittleTransaction.isInTransaction
     */
    @Override
    public synchronized Asset remove(final UUID u_key) {
        final LittleTransaction trans_maindb = oprovideTrans.get();
        if (trans_maindb.isDbUpdating()) {
            trans_maindb.deferTillTransactionEnd(
                    new Runnable() {

                @Override
                        public void run() {
                            remove(u_key);
                        }
                    });
            return null;
        }
        try {
            JdbcDbWriter<UUID> db_writer = om_db.makeDbEraser();
            db_writer.saveObject( u_key);
        } catch (SQLException e) {
            throw new AssertionFailedException("Failure updating cache, caught: " + e, e);
        }

        return ocache_asset.remove(u_key);
    }

    @Override
    public synchronized void clear() {
        try {
            JdbcDbWriter<UUID> db_writer = om_db.makeDbEraser();
            db_writer.saveObject( null);
        } catch (SQLException e) {
            throw new AssertionFailedException("Failure updating cache, caught: " + e, e);
        }

        ocache_asset.clear();
        ov_null_entries.clear();
    }

    @Override
    public int size() {
        return ocache_asset.size();
    }

    @Override
    public boolean isEmpty() {
        return ocache_asset.isEmpty();
    }

    @Override
    public Map<UUID, Asset> cacheContents() {
        return ocache_asset.cacheContents();
    }

    @Override
    public Maybe<Asset> getAsset(UUID u_id) throws DataAccessException, AssetException, NoSuchThingException, GeneralSecurityException {
        return Maybe.emptyIfNull( getAssetOrNull(u_id) );
    }

    /** Ignore cycle-cache - somebody else should maintain and check that */
    public Maybe<Asset> getAsset(UUID u_id, Map<UUID, Asset> v_cycle_cache) throws DataAccessException, AssetException, NoSuchThingException, GeneralSecurityException {
        return getAsset(u_id);
    }

    /**
     * Return null if there is a cache entry for this u_id key with a null value.
     * Throw CacheMissException if there is no entry for this
     * has a no entry for the u_id key or if LittleTransaction.isInTransaction.
     *
     * @param u_id to look for
     * @return the asset or null if the u_id is in the cache
     * @exception CacheMissException if the u_id is not in the cache
     */
    public Asset getAssetOrNull(UUID u_id) throws DataAccessException, AssetException, GeneralSecurityException {
        if (oprovideTrans.get().isDbUpdating()) {
            throw new CacheMissException("In transaction");
        }
        if (ov_null_entries.contains(u_id)) {
            return null;
        }
        Asset a_result = this.get(u_id);   // does a clone()
        if (null != a_result) {
            return a_result;
        }
        throw new CacheMissException("No entry in cache for: " + u_id);
    }

    /** Ignore cycle-cache - somebody else should maintain and check that */
    public Asset getAssetOrNull(UUID u_id, Map<UUID, Asset> v_cycle_cache) throws DataAccessException, AssetException, GeneralSecurityException {
        return getAssetOrNull(u_id);
    }

    /**
     * Just loops over id's calling getAssetOrNull().
     * Throws a CacheMissException if any of the requested ID's are not
     * in cache.
     */
    @Override
    public List<Asset> getAssets(Collection<UUID> v_id) throws DataAccessException, AssetException, GeneralSecurityException {
        final List<Asset> v_result = new ArrayList<Asset>();
        Set<UUID> v_done = new HashSet<UUID>();

        for (UUID u_id : v_id) {
            if (!v_done.contains(u_id)) {
                Asset a_asset = getAssetOrNull(u_id);
                if (null != a_asset) {
                    v_result.add(a_asset);
                }
            }
        }
        return v_result;
    }

    /** Also cache miss if LittleTransaction.isInTransaction */
    @Override
    public synchronized Map<String, UUID> getHomeAssetIds() throws DataAccessException, AssetException, GeneralSecurityException {
        if (oprovideTrans.get().isDbUpdating()) {
            throw new CacheMissException("In transaction");
        }
        try {
            JdbcDbReader<Map<String, UUID>, String> db_reader = om_db.makeDbHomeIdsLoader();
            if (null == db_reader) {
                throw new CacheMissException();
            }
            return db_reader.loadObject( "" );
        } catch (SQLException e) {
            throw new DataAccessException("frickjack: " + e, e);
        }
    }

    /** NOOP if LittleTransaction.isInTransaction */
    @Override
    public synchronized void setHomeAssetIds(Map<String, UUID> v_home_ids) {
        if (oprovideTrans.get().isDbUpdating()) {
            return;
        }
        try {
            JdbcDbWriter<Map<String, UUID>> db_writer = om_db.makeDbHomeIdsSaver();
            db_writer.saveObject( v_home_ids);
        } catch (SQLException e) {
            Whatever.get().check("Data access failure: " + e, false);
        }
    }

    @Override
    public synchronized Map<String, UUID> getAssetIdsFrom(UUID u_source,
            AssetType n_type) throws DataAccessException, AssetException, GeneralSecurityException {
        if (oprovideTrans.get().isDbUpdating()) {
            throw new CacheMissException("In transaction");
        }
        try {
            JdbcDbReader<Map<String, UUID>, String> db_reader = om_db.makeDbAssetIdsFromLoader(u_source, n_type);
            if (null == db_reader) {
                throw new CacheMissException();
            }
            return db_reader.loadObject( "" );
        } catch (SQLException e) {
            throw new DataAccessException("Data access failure", e);
        }
    }

    @Override
    public synchronized void setAssetIdsFrom(UUID u_source,
            AssetType n_type,
            Map<String, UUID> v_data) {
        if (oprovideTrans.get().isDbUpdating()) {
            return;
        }
        try {
            JdbcDbWriter<Map<String, UUID>> db_writer = om_db.makeDbAssetIdsFromSaver(u_source, n_type);
            db_writer.saveObject( v_data);
        } catch (SQLException e) {
            Whatever.get().check("Data access failure: " + e, false);
        }
    }

    @Override
    public synchronized Set<UUID> getAssetIdsTo(UUID u_to,
            AssetType n_type) throws DataAccessException, AssetException, GeneralSecurityException {
        if (oprovideTrans.get().isDbUpdating()) {
            throw new CacheMissException("In transaction");
        }
        try {
            JdbcDbReader<Set<UUID>, String> db_reader = om_db.makeDbAssetIdsToLoader(u_to, n_type);
            if (null == db_reader) {
                throw new CacheMissException();
            }
            return db_reader.loadObject( "" );
        } catch (SQLException e) {
            throw new DataAccessException("Data access failure", e);
        }
    }

    @Override
    public synchronized void setAssetIdsTo(UUID u_to,
            AssetType n_type,
            Set<UUID> v_data) {
        if (oprovideTrans.get().isDbUpdating()) {
            return;
        }
        try {
            JdbcDbWriter<Set<UUID>> db_writer = om_db.makeDbAssetIdsToSaver(u_to, n_type);
            db_writer.saveObject( v_data);
        } catch (SQLException e) {
            Whatever.get().check("Data access failure: " + e, false);
        }
    }


    @Override
    public Maybe<Asset> getByName(String s_name, AssetType n_type) throws DataAccessException,
            AssetException, NoSuchThingException, AccessDeniedException, GeneralSecurityException {
        if (oprovideTrans.get().isDbUpdating()) {
            throw new CacheMissException("In transaction");
        }
        try {
            JdbcDbReader<Set<UUID>, String> db_reader = om_db.makeDbAssetsByNameLoader(s_name, n_type, null);
            if (null == db_reader) {
                throw new CacheMissException();
            }

            Set<UUID> v_data = db_reader.loadObject( "" );
            if (v_data.isEmpty()) {
                return null;
            }
            return Maybe.emptyIfNull( getAssetOrNull(v_data.iterator().next()) );
        } catch (SQLException e) {
            throw new DataAccessException("frickjack: " + e, e);
        }

    }

    @Override
    public List<Asset> getAssetHistory(UUID u_id, java.util.Date t_start, java.util.Date t_end)
            throws NoSuchThingException, AccessDeniedException, GeneralSecurityException,
            DataAccessException, AssetException {
        throw new CacheMissException();
    }

    @Override
    public void setAssetsByName(String s_name, AssetType n_type, UUID u_home, Set<Asset> v_data) {
        if (oprovideTrans.get().isDbUpdating()) {
            return;
        }
        try {
            JdbcDbWriter<Set<Asset>> db_writer = om_db.makeDbAssetsByNameSaver(s_name, n_type, u_home);
            db_writer.saveObject( v_data);
        } catch (SQLException e) {
            log.log(Level.WARNING, "Cache update caught unexpected: " + e +
                    ", " + BaseException.getStackTrace(e));
            throw new AssertionFailedException("Data access failure: " + e, e);
        }
    }

    /** Not yet implemented */
    @Override
    public Maybe<Asset> getAssetFrom(UUID u_from, String s_name) throws BaseException, AssetException,
            GeneralSecurityException {
        throw new CacheMissException();
    }



    /** Not yet implemented */
    @Override
    public Maybe<Asset> getAssetAtPath(AssetPath path_asset) throws BaseException, AssetException,
            GeneralSecurityException {
        throw new CacheMissException();
    }

    /**
     * Not implemented in CacheManager
     */
    @Override
    public Map<UUID, Long> checkTransactionCount(Map<UUID, Long> v_check) throws BaseException {
        throw new CacheMissException();
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from, AssetType n_type, int i_state) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public Map<String, UUID> getAssetIdsFrom(UUID u_from) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        return getAssetIdsFrom( u_from, null );
    }

    @Override
    public List<IdWithClock> checkTransactionLog(UUID homeId, long minTransaction) throws BaseException, RemoteException {
        throw new CacheMissException("Not supported yet.");
    }
}


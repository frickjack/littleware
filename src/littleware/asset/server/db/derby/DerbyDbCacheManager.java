package littleware.asset.server.db.derby;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.*;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.UUIDFactory;
import littleware.db.*;

/**
 * Derby implementation of DbCacheManager interface.
 * Behaves like a singleton.  Works with the data-handlers in
 * this package to maintain a valid query-log.
 */
public class DerbyDbCacheManager implements DbCacheManager {

    private static final Set<String> mv_query_log = Collections.synchronizedSet(new HashSet<String>());
    private final static String MS_TYPE_TREE_INSERT =
            "INSERT INTO littleware.x_asset_type_tree ( s_ancestor_id, s_descendent_id ) VALUES ( ?, ? )";
    private static final Set<AssetType> mv_saved_types = new HashSet<AssetType>();

    /**
     * Internal to package utility
     * saves AssetType tree info to the derby 
     *               littleware.x_asset_type_tree table
     * as necessary.
     *
     * @param n_type to save ancestore-tree for if necessary
     * @param con_derby to access the db with
     */
    static void saveAssetTypeIfNecessary(final AssetType n_type,
            final Connection con_derby) throws SQLException {
        AssetType n_super = n_type.getSuperType();

        if ((null != n_super) && (!mv_saved_types.contains(n_type))) {
            PreparedStatement stmt_derby = null;

            try {
                // Note - only have to insert type into type-tree table once
                // A type's ancestors do not change.
                stmt_derby = con_derby.prepareStatement(MS_TYPE_TREE_INSERT);

                for (AssetType n_check = n_type;
                        n_super != null;
                        n_super = n_super.getSuperType()) {
                    stmt_derby.setString(1, UUIDFactory.makeCleanString(n_super.getObjectId()));
                    stmt_derby.setString(2, UUIDFactory.makeCleanString(n_check.getObjectId()));
                    stmt_derby.executeUpdate();
                    n_check = n_super;
                }
                mv_saved_types.add(n_type);
            } finally {
                Janitor.cleanupSession(stmt_derby);
            }
        }
    }

    /**
     * Add the specified query to the query-log to indicate that
     * the data for the query has been saved to the cache.
     * Each data-saver invokes this method as a callback after successfully updating
     * the derby cache.
     */
    static void addQueryToLog(String s_querylog_key) {
        mv_query_log.add(s_querylog_key);
    }

    /**
     * Zero out the QueryLog - should only be called by DbEraser as a callback
     * after clearing all data out of the derby query database.
     */
    static void clearQueryLog() {
        mv_query_log.clear();
    }

    public JdbcDbReader<Map<String, UUID>, String> makeDbHomeIdsLoader() {
        if (!mv_query_log.contains(DbHomeIdsSaver.getQueryLogKey())) {
            return null;
        }
        return new DbHomeIdsLoader();
    }

    public JdbcDbReader<Map<String, UUID>, String> makeDbAssetIdsFromLoader(UUID u_from, AssetType n_type) {
        String s_query_key = DbAssetIdsFromSaver.getQueryLogKey(u_from, n_type);

        if (!mv_query_log.contains(s_query_key)) {
            // We may have done a previous lookup with a wildcard type,
            // but we may not know the TYPE of those assets - UGH!
            return null;
        }
        return new DbAssetIdsFromLoader(u_from, n_type);
    }

    public JdbcDbReader<Set<UUID>, String> makeDbAssetIdsToLoader(UUID u_to, AssetType n_type) {
        String s_query_key = DbAssetIdsToSaver.getQueryLogKey(u_to, n_type);

        if (!mv_query_log.contains(s_query_key)) {
            return null;
        }
        return new DbAssetIdsToLoader(u_to, n_type);
    }

    public JdbcDbWriter<Set<UUID>> makeDbAssetIdsToSaver(UUID u_to, AssetType n_type) {
        return new DbAssetIdsToSaver(u_to, n_type);
    }

    public JdbcDbWriter<Map<String, UUID>> makeDbHomeIdsSaver() {
        return new DbHomeIdsSaver();
    }

    public JdbcDbWriter<Map<String, UUID>> makeDbAssetIdsFromSaver(UUID u_from, AssetType n_type) {
        return new DbAssetIdsFromSaver(u_from, n_type);
    }

    public JdbcDbWriter<Asset> makeDbAssetSaver() {
        return new DbAssetSaver();
    }

    public JdbcDbWriter<UUID> makeDbEraser() {
        return new DbEraser();
    }

    public JdbcDbWriter<Set<Asset>> makeDbAssetsByNameSaver(String s_name, AssetType n_type, UUID u_home) {
        return new DbAssetsByNameSaver(s_name, n_type, u_home);
    }

    public JdbcDbReader<Set<UUID>, String> makeDbAssetsByNameLoader(String s_name, AssetType n_type,
            UUID u_home) {
        String s_query_key = DbAssetsByNameSaver.getQueryLogKey(s_name, n_type, u_home);

        if (!mv_query_log.contains(s_query_key)) {
            return null;
        }
        return new DbAssetsByNameLoader(s_name, n_type, u_home);
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


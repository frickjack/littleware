package littleware.asset.server.db.derby;

import java.util.*;
import java.sql.*;

import littleware.base.*;
import littleware.db.*;
import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.UUIDFactory;

/**
 * DbWriter implementation for creating new assets,
 * and updating existing assets.
 */
public class DbAssetSaver extends AbstractDerbyWriter<Asset> {

    private static final String MS_TRANSACTION_QUERY =
            "SELECT l_last_transaction FROM littleware.asset_cache WHERE s_id=?";
    private static final String MS_UPDATE_QUERY =
            "UPDATE littleware.asset_cache SET " +
            "							  s_name       = ?," +
            "							  s_id_home    = ?," +
            "							  l_last_transaction   = ?," +
            "							  s_pk_type    = ?," +
            "							  s_id_creator = ?," +
            "							  s_id_updater = ?," +
            "							  s_id_owner   = ?," +
            "							  f_value      = ?," +
            "							  s_id_acl     = ?," +
            "							  t_created    = ?," +
            "							  t_updated        = ?," +
            "							  s_id_from    = ?," +
            "							  s_id_to      = ?," +
            "							  t_start      = ?," +
            "							  t_end  = ? " +
            "  WHERE s_id = ?";
    private static final String MS_INSERT_QUERY =
            "INSERT INTO littleware.asset_cache (" +
            "							  s_name      ," +
            "							  s_id_home   ," +
            "							  l_last_transaction  ," +
            "							  s_pk_type   ," +
            "							  s_id_creator," +
            "							  s_id_updater," +
            "							  s_id_owner  ," +
            "							  f_value     ," +
            "							  s_id_acl    ," +
            "							  t_created   ," +
            "							  t_updated       ," +
            "							  s_id_from   ," +
            "							  s_id_to     ," +
            "							  t_start     ," +
            "							  t_end,   " +
            "							  s_id    " +
            "							  ) VALUES (          " +
            "	    ?,?,?,?,?   ,?,?,?,?,?   ,?,?,?,?,?   ,?  )";

    /**
     * Constructor registers query with super-class
     */
    public DbAssetSaver() {
        super(MS_TRANSACTION_QUERY, false);
    }

    /**
     * Parameterize the given statement - both MS_QUERY_UPDATE and MS_QUERY_INSERT
     * get parameterized the same way
     *
     * @param sql_stmt to parameterize - should be one of MS_QUERY_INSERT/UPDATE
     * @param a_data to parameterize the statement with
     */
    private void setupUpdateStatement(PreparedStatement sql_stmt, Asset a_data) throws SQLException {
        Whatever.check("Asset must have non-null name", null != a_data.getName());
        sql_stmt.setString(1, a_data.getName());
        sql_stmt.setString(2, UUIDFactory.makeCleanString(a_data.getHomeId()));
        sql_stmt.setLong(3, a_data.getTransactionCount());
        sql_stmt.setString(4, UUIDFactory.makeCleanString(a_data.getAssetType().getObjectId()));
        sql_stmt.setString(5, UUIDFactory.makeCleanString(a_data.getCreatorId()));
        sql_stmt.setString(6, UUIDFactory.makeCleanString(a_data.getLastUpdaterId()));
        sql_stmt.setString(7, UUIDFactory.makeCleanString(a_data.getOwnerId()));
        sql_stmt.setFloat(8, a_data.getValue());
        sql_stmt.setString(9, UUIDFactory.makeCleanString(a_data.getAclId()));
        sql_stmt.setTimestamp(10, new Timestamp(a_data.getCreateDate().getTime()));
        sql_stmt.setTimestamp(11, new Timestamp(a_data.getLastUpdateDate().getTime()));
        sql_stmt.setString(12, UUIDFactory.makeCleanString(a_data.getFromId()));
        sql_stmt.setString(13, UUIDFactory.makeCleanString(a_data.getToId()));
        if (null != a_data.getStartDate()) {
            sql_stmt.setTimestamp(14, new Timestamp(a_data.getStartDate().getTime()));
        } else {
            sql_stmt.setTimestamp(14, null);
        }
        if (null != a_data.getEndDate()) {
            sql_stmt.setTimestamp(15, new Timestamp(a_data.getEndDate().getTime()));
        } else {
            sql_stmt.setTimestamp(15, null);
        }
        sql_stmt.setString(16, UUIDFactory.makeCleanString(a_data.getObjectId()));
    }

    /**
     * Save the Asset data to the derby cache database.
     * We assume the save is not necessary if data for the asset
     * is already in the cache, and the asset transaction-count has
     * not increased.
     */
    public boolean saveObject(PreparedStatement sql_stmt, Asset a_data) throws SQLException {
        String s_update_query = MS_UPDATE_QUERY;

        ResultSet sql_rset = null;
        try {
            sql_stmt.setString(1, UUIDFactory.makeCleanString(a_data.getObjectId()));
            sql_rset = sql_stmt.executeQuery();

            if (sql_rset.next()) {
                // Then we have some data in the cache
                s_update_query = MS_UPDATE_QUERY;

                if ((null != sql_rset.getObject(1)) && (a_data.getTransactionCount() <= sql_rset.getLong(1))) {
                    // Data in cache is already up to date
                    return false;
                }
            } else {
                // No data for this asset in the cache yet
                s_update_query = MS_INSERT_QUERY;
                // Check if we need to update the asset-type-tree                
            }
            DerbyDbCacheManager.saveAssetTypeIfNecessary(a_data.getAssetType(), sql_stmt.getConnection());
        } finally {
            Janitor.cleanupSession(sql_rset);
        }

        PreparedStatement sql_update = null;
        try {
            // Setup a supplementary statement to do UPDATE/INSERT as necessary
            sql_update = sql_stmt.getConnection().prepareStatement(s_update_query);
            setupUpdateStatement(sql_update, a_data);
            sql_update.executeUpdate();
            return false;
        } finally {
            // Don't leave that supplementary statement open!
            Janitor.cleanupSession(sql_update);
        }
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


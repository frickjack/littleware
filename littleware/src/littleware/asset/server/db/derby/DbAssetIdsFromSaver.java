package littleware.asset.server.db.derby;

import java.sql.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.AssertionFailedException;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;
import littleware.db.*;
import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.UUIDFactory;

/**
 * DbWriter implementation for saving ids-from data to the cache
 */
public class DbAssetIdsFromSaver extends AbstractDerbyWriter<Map<String, UUID>> {

    private final static Logger olog_generic = Logger.getLogger("littleware.asset.server.db.derby.DbAssetIdsFromSaver");
    private static final String MS_QUERY_FORMAT = "assetidsfrom:%1$s,%2$s";
    private final UUID ou_from;
    private final AssetType on_type;

    /**
     * Return the string that identifies a from-id lookup in the
     * query-log. 
     */
    public static String getQueryLogKey(UUID u_from, AssetType n_type) {
        if (n_type != null) {
            return String.format(MS_QUERY_FORMAT, u_from.toString(), n_type.getObjectId());
        } else {
            return String.format(MS_QUERY_FORMAT, u_from.toString(), "null");
        }
    }

    /**
     * Constructor registers query with super-class
     * 
     * @param u_from id of parent result-assets link from
     * @param n_type of return-assets - null to leave unspecified
     * @param sql_factory db connection-source
     */
    public DbAssetIdsFromSaver(UUID u_from, AssetType n_type) {
        super("UPDATE littleware.asset_cache SET s_name=?, s_id_from=? " +
                ((null == n_type) ? "" : ",s_pk_type=? ") + " WHERE s_id=?",
                false);

        ou_from = u_from;
        on_type = n_type;
    }

    /**
     * Save the fromid data to the derby cache database, and
     * update DbDerbyCacheManager's query log.
     */
    public boolean saveObject(PreparedStatement sql_stmt, Map<String, UUID> v_data) throws SQLException {
        Map<String, UUID> v_missing_assets = new HashMap<String, UUID>();
        String s_id_from = UUIDFactory.makeCleanString(ou_from);

        sql_stmt.setString(2, s_id_from);

        if (null != on_type) {
            DerbyDbCacheManager.saveAssetTypeIfNecessary(on_type, sql_stmt.getConnection());
            sql_stmt.setString(3, UUIDFactory.makeCleanString(on_type.getObjectId()));
        }

        PreparedStatement stmt_check1st =
                sql_stmt.getConnection().prepareStatement("SELECT s_name, s_pk_type FROM littleware.asset_cache WHERE s_id=?");
        ResultSet rset_check1st = null;

        try {
            for (Map.Entry<String, UUID> entry_x : v_data.entrySet()) {
                final String s_id = UUIDFactory.makeCleanString(entry_x.getValue());

                stmt_check1st.setString(1, s_id);
                rset_check1st = stmt_check1st.executeQuery();

                if (rset_check1st.next()) {
                    String s_name = rset_check1st.getString(1);
                    String s_type_id = rset_check1st.getString(2);
                    UUID u_type = ((null == s_type_id) ? null : UUIDFactory.parseUUID(s_type_id));

                    if ((!Whatever.equalsSafe(s_name, entry_x.getKey())) || ((null != on_type) && ((null == u_type) || on_type.isA(AssetType.getMember(u_type))))) {
                        // Need to update name or type data
                        sql_stmt.setString(1, entry_x.getKey());
                        if (null != on_type) {
                            sql_stmt.setString(4, s_id);
                        } else {
                            sql_stmt.setString(3, s_id);
                        }
                        if (0 == sql_stmt.executeUpdate()) {
                            // this id must not be in the cache yet - need to add it
                            olog_generic.log(Level.WARNING, "ASSERTION FAILED");
                            v_missing_assets.put(entry_x.getKey(), entry_x.getValue());
                        }
                    } // else db has good data in it
                } else { // need to insert data
                    v_missing_assets.put(entry_x.getKey(), entry_x.getValue());
                }
                rset_check1st.close();
            }
        } catch (NoSuchTypeException e) {
            throw new AssertionFailedException("Invalid asset type collected", e);
        } finally {
            Janitor.cleanupSession(rset_check1st, stmt_check1st);
        }

        PreparedStatement sql_insert = null;
        try {
            // Setup a supplementary statement to do needed INSERTS
            sql_insert = sql_stmt.getConnection().prepareStatement("INSERT INTO littleware.asset_cache (s_id, s_name, s_id_from, s_pk_type) VALUES ( ?, ?, ?, ? )");

            sql_insert.setString(3, s_id_from);
            if (null != on_type) {
                sql_insert.setString(4, UUIDFactory.makeCleanString(on_type.getObjectId()));
            } else {
                sql_insert.setNull(4, Types.VARCHAR);
            }
            for (Map.Entry<String, UUID> entry_x : v_missing_assets.entrySet()) {
                sql_insert.setString(1, UUIDFactory.makeCleanString(entry_x.getValue()));
                sql_insert.setString(2, entry_x.getKey());
                sql_insert.executeUpdate();
            }
            DerbyDbCacheManager.addQueryToLog(getQueryLogKey(ou_from, on_type));
        } finally {
            // Don't leave that supplementary statement open!
            Janitor.cleanupSession(sql_insert);
        }
        return false;
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


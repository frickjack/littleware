package littleware.asset.server.db.postgres;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.sql.*;

import littleware.asset.*;
import littleware.asset.server.AbstractDbReader;
import littleware.asset.server.TransactionManager;
import littleware.base.*;

/**
 * Data loader for home asset name-id map
 */
public class DbAssetsByNameLoader extends AbstractDbReader<Set<Asset>, String> {

    private static final Logger olog_generic = Logger.getLogger("littleware.asset.server.db.postgres.DbAssetsByNameLoader");
    private final int oi_client_id;
    private String os_name = null;
    private UUID ou_home = null;
    private AssetType on_type = null;
    private final TransactionManager omgr_trans;

    /**
     * Constructor registers query with super-class,
     * and stashes the local client id.
     */
    public DbAssetsByNameLoader(String s_name, AssetType n_type, UUID u_home, int i_client_id, TransactionManager mgr_trans ) {
        super("SELECT * FROM littleware.getAssetsByName( ?, ?, ?, ? )", false, mgr_trans );
        oi_client_id = i_client_id;
        os_name = s_name;
        on_type = n_type;
        ou_home = u_home;
        omgr_trans = mgr_trans;
    }

    /**
     * Implementation sets argument sql_stmt.setObject ( 1, x_arg ),
     * then calls through to super.executeStatement ( sql_stmt, null )
     *
     * @param s_arg is ignored
     * @return ResultSet from execution of query or callable statement
     */
    public ResultSet executeStatement(PreparedStatement sql_stmt, String s_arg) throws SQLException {
        olog_generic.log(Level.FINE, "Parameterizing getAssetsByName with: " +
                os_name + ", " + on_type + " (" + on_type.getObjectId() +
                "), " + ou_home);
        sql_stmt.setString(1, os_name);
        sql_stmt.setString(2, UUIDFactory.makeCleanString(on_type.getObjectId()));
        sql_stmt.setString(3, UUIDFactory.makeCleanString(ou_home));
        sql_stmt.setInt(4, oi_client_id);

        return super.executeStatement(sql_stmt, null);
    }

    /**
     * Pull an Asset from a canonical littleware.asset ResultSet
     *
     * @param sql_rset to pull data from
     * @return name to id map
     * @exception SQLException on failure to extract data
     */
    public Set<Asset> loadObject(ResultSet sql_rset) throws SQLException {
        DbAssetLoader db_loader = new DbAssetLoader(oi_client_id, omgr_trans );
        Set<Asset> v_result = new HashSet<Asset>();

        while (sql_rset.next()) {
            Asset a_new = db_loader.loadObjectReady(sql_rset);
            olog_generic.log(Level.FINE, "Adding asset to result: " +
                    a_new.getName() + " (" + a_new.getObjectId() + ")");
            Whatever.check("Asset has valid name", a_new.getName() != null);
            v_result.add(a_new);
        }
        return v_result;
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


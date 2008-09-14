package littleware.asset.server.db.postgres;


import java.sql.*;

import littleware.asset.Asset;
import littleware.asset.server.AbstractDbWriter;
import littleware.base.UUIDFactory;
import littleware.db.*;


/**
 * Little handler for clearing the cache-sync data for a given client src -
 * should be done at client startup time.
 */
public class DbCacheSyncClearer 
             extends AbstractDbWriter<String> {
    private final int oi_client_id;
    
    
	/**
	 * Constructor registers query with super-class,
     * and stashes the local client id.
     *
     * @param i_client_id
	 */
	public DbCacheSyncClearer ( int i_client_id ) {
		super ( "{ ? = call littleware.clearCache( ? ) }", true );
        oi_client_id = i_client_id;
	}
	
	/**
	 * Clear the db cache-sync data for the client.
	 *
	 * @param sql_stmt connection to execute a save operation against - possible obtained from
	 *                 this.prepareStatement ( sql_conn, a_delete ),
	 *                 possibly null for some aggregate-based saves.
	 * @param s_ignore is ignored
	 * @return result of sql_stmt.execute()
	 * @exception SQLException pass through exceptions thrown by sql_stmt access
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, String s_ignore ) throws SQLException {
		CallableStatement sql_call = (CallableStatement) sql_stmt;
		sql_call.registerOutParameter ( 1, Types.INTEGER );
		sql_call.setInt ( 2, oi_client_id );
		return sql_call.execute ();
 	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


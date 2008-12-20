/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.postgres;


import java.sql.*;

import littleware.asset.server.AbstractDbWriter;
import littleware.asset.server.TransactionManager;


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
	public DbCacheSyncClearer ( int i_client_id, TransactionManager mgr_trans ) {
		super ( "{ ? = call littleware.clearCache( ? ) }", true, mgr_trans );
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

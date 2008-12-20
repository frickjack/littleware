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

import littleware.asset.Asset;
import littleware.asset.server.AbstractDbWriter;
import littleware.asset.server.TransactionManager;
import littleware.base.UUIDFactory;

/**
 * Little handler for deleting assets out of the repository
 */
public class DbAssetDeleter extends AbstractDbWriter<Asset> {
    private final int oi_client_id;
    
	/**
	 * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbAssetDeleter ( int i_client_id, TransactionManager mgr_trans ) {
		super ( "{ ? = call littleware.deleteAsset( ?, ?, ? ) }", true, mgr_trans );
        oi_client_id = i_client_id;
	}
	
	/**
	* Save the specified object by setting parameters against and executing
	 * the supplied prepared statement.
	 *
	 * @param sql_stmt connection to execute a save operation against - possible obtained from
	 *                 this.prepareStatement ( sql_conn, a_delete ),
	 *                 possibly null for some aggregate-based saves.
	 * @param a_delete to save
	 * @return result of sql_stmt.execute()
	 * @exception SQLException pass through exceptions thrown by sql_stmt access
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, Asset a_delete ) throws SQLException {
		CallableStatement sql_call = (CallableStatement) sql_stmt;
		sql_call.registerOutParameter ( 1, Types.BIGINT );
		sql_call.setString ( 2, UUIDFactory.makeCleanString ( a_delete.getObjectId () ) );
		sql_call.setString ( 3, a_delete.getLastUpdate () );
        sql_call.setInt ( 4, oi_client_id );
		return sql_call.execute ();
 	}
	
}


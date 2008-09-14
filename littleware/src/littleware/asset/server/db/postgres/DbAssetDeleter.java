package littleware.asset.server.db.postgres;


import java.sql.*;

import littleware.asset.Asset;
import littleware.asset.server.AbstractDbWriter;
import littleware.base.UUIDFactory;
import littleware.db.*;

/**
 * Little handler for deleting assets out of the repository
 */
public class DbAssetDeleter extends AbstractDbWriter<Asset> {
    private final int oi_client_id;
    
	/**
	 * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbAssetDeleter ( int i_client_id ) {
		super ( "{ ? = call littleware.deleteAsset( ?, ?, ? ) }", true );
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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


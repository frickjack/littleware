package littleware.security.auth.server.db.postgres;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.UUID;
import java.sql.*;

import littleware.asset.server.AbstractDbWriter;
import littleware.db.*;
import littleware.base.UUIDFactory;


/**
 * Handler to update a user password
 */
public class DbPasswordSaver extends AbstractDbWriter<String> {
	private static Logger olog_generic = Logger.getLogger ( "littleware.security.auth.server.db.DbPasswordSaver" );
	
	private UUID ou_user = null;
	
	private final static String  os_query =
		"{ ? = call littleware.savePassword( ?, ? ) }";
	
	/**
	 * Stash the id of the user that this object will update
	 *
	 * @param u_user id to stash
	 */
	public DbPasswordSaver ( UUID u_user ) {
		super ( os_query, true );
		ou_user = u_user;
	}
	
	/**
	 * Update the password associated with this handler&apos;s user
	 *
	 * @param sql_stmt connection to execute a save operation against - possible obtained from
	 *                 this.prepareStatement ( sql_conn, a_object ),
	 *                 possibly null for some aggregate-based saves.
	 * @param s_password to save
	 * @return result of sql_stmt.execute()
	 * @exception SQLException pass through exceptions thrown by sql_stmt access
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, String s_password ) throws SQLException {
		if ( (null == s_password) 
			 || (s_password.length () < 6)
			 ) {
			throw new SQLException ( "password must be at least 6 characters long" );
		}
		
		CallableStatement sql_call = (CallableStatement) sql_stmt;
		olog_generic.log ( Level.FINE, "Registering output type: " + java.sql.Types.BOOLEAN );
		sql_call.registerOutParameter ( 1, java.sql.Types.BIT );
		sql_call.setString ( 2, UUIDFactory.makeCleanString ( ou_user ) );		
		sql_call.setString ( 3, s_password );
				
		return sql_call.execute ();
 	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


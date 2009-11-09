/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server.db.postgres;

import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.UUID;
import java.sql.*;

import littleware.asset.server.AbstractDbWriter;
import littleware.asset.server.JdbcTransaction;
import littleware.base.UUIDFactory;


/**
 * Handler to update a user password
 */
public class DbPasswordSaver extends AbstractDbWriter<String> {
	private static final Logger olog_generic = Logger.getLogger ( DbPasswordSaver.class.getName() );
	
	private UUID ou_user = null;
	
	private final static String  os_query =
		"{ ? = call littleware.savePassword( ?, ? ) }";
	
	/**
	 * Stash the id of the user that this object will update
	 *
	 * @param u_user id to stash
	 */
	public DbPasswordSaver ( UUID u_user, Provider<JdbcTransaction> provideTrans ) {
		super ( os_query, true, provideTrans );
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
    @Override
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


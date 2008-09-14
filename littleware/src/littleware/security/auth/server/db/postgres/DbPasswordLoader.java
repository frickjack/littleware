package littleware.security.auth.server.db.postgres;

import java.sql.*;
import java.util.UUID;

import littleware.asset.server.AbstractDbReader;
import littleware.db.*;
import littleware.base.UUIDFactory;

/**
 * Postgres implementation of password-checker
 */
public class DbPasswordLoader extends AbstractDbReader<Boolean,String> {
	private static final String os_query = "SELECT * FROM littleware.checkPassword ( ?, ? )";
	
	private UUID         ou_user = null;
	
	
	/** 
	 * Stash the id of the user this handler should update
	 * 
	 * @param u_user id of user to check password
	 */
	public DbPasswordLoader ( UUID u_user ) {
		super ( os_query, false );
		ou_user = u_user;
	}
		
	/**
	 * Implementation sets argument sql_stmt.setObject ( 1, x_arg ),
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @param s_password to check
	 * @return ResultSet from execution of query or callable statement
	 */
	public ResultSet executeStatement( PreparedStatement sql_stmt, String s_password ) throws SQLException {
		sql_stmt.setString ( 1, UUIDFactory.makeCleanString ( ou_user ) );
		sql_stmt.setString ( 2, s_password );
		return super.executeStatement ( sql_stmt, null );
	}
	
	/**
	 * Collect result of user password check
	 * 
	 * @return true if password check succeeds, false otherwise
	 */
	public Boolean loadObject ( ResultSet sql_rset ) throws SQLException {
		if ( ! sql_rset.next () ) {
			return Boolean.FALSE;
		}
		return sql_rset.getBoolean ( 1 );
	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


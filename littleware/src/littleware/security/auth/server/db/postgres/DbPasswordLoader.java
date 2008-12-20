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


package littleware.security.auth.server.db.postgres;

import java.sql.*;
import java.util.UUID;

import littleware.asset.server.AbstractDbReader;
import littleware.asset.server.TransactionManager;
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
	public DbPasswordLoader ( UUID u_user, TransactionManager mgr_trans ) {
		super ( os_query, false, mgr_trans );
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


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

import java.util.logging.Logger;
import java.util.*;
import java.sql.*;

import littleware.asset.server.AbstractDbReader;
import littleware.asset.*;
import littleware.asset.server.TransactionManager;
import littleware.base.*;
import littleware.db.*;


/**
 * Data loader for home asset name-id map
 */
public class DbChildIdLoader extends AbstractDbReader<Map<String,UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( DbChildIdLoader.class.getName() );
    private final int           oi_client_id;
	
	private UUID      ou_from = null;
	private AssetType on_type = null;
    private final TransactionManager  omgr_trans;


	/**
     * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbChildIdLoader ( UUID u_from, AssetType n_type, int i_client_id, TransactionManager mgr_trans ) {
		super ( "SELECT * FROM littleware.getChildIdDictionary( ?, ?, ? )", false, mgr_trans );
		ou_from = u_from;
		on_type = n_type;
        oi_client_id = i_client_id;
        omgr_trans = mgr_trans;
	}
	
	/**
	 * Implementation sets argument sql_stmt.setObject ( 1, x_arg ),
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @param s_arg is ignored
	 * @return ResultSet from execution of query or callable statement
	 */
    @Override
	public ResultSet executeStatement( PreparedStatement sql_stmt, String s_arg ) throws SQLException {		
		sql_stmt.setString ( 1, UUIDFactory.makeCleanString ( ou_from ) );
		if ( null != on_type ) {
			sql_stmt.setString ( 2, UUIDFactory.makeCleanString ( on_type.getObjectId () ) );
		} else {
			sql_stmt.setNull ( 2, Types.VARCHAR );
		}
        sql_stmt.setInt ( 3, oi_client_id );
		return super.executeStatement ( sql_stmt, null );
	}
	
	
	/**
	 * Pull an Asset from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
	 * @return name to id map
	 * @exception SQLException on failure to extract data
	 */
	public Map<String,UUID> loadObject( ResultSet sql_rset ) throws SQLException {
		JdbcDbReader<Map<String,UUID>,String> db_home_reader = new DbHomeIdLoader ( oi_client_id, omgr_trans );
		return db_home_reader.loadObject ( sql_rset );
	}
}


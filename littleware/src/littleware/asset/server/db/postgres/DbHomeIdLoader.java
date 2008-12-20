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
import littleware.asset.server.TransactionManager;
import littleware.base.*;


/**
 * Data loader for home asset name-id map
 */
public class DbHomeIdLoader extends AbstractDbReader<Map<String,UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( DbHomeIdLoader.class.getName() );
    private final int           oi_client_id;
	
	
	/**
	 * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbHomeIdLoader ( int i_client_id, TransactionManager mgr_trans ) {
		super ( "SELECT * FROM littleware.getHomeIdDictionary(?)", false, mgr_trans );
        oi_client_id = i_client_id;
	}
	
		
    @Override
    public ResultSet executeStatement( PreparedStatement sql_stmt, String s_arg ) throws SQLException {
		sql_stmt.setInt ( 1, oi_client_id );
		return super.executeStatement ( sql_stmt, null );
	}
    
	/**
	 * Pull an Asset from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
	 * @return Asset or null if no data
	 * @exception SQLException on failure to extract data
	 */
	public Map<String,UUID> loadObject( ResultSet sql_rset ) throws SQLException {
		Map<String,UUID> v_result = new HashMap<String,UUID> ();
		
		while ( sql_rset.next () ) {
			v_result.put ( sql_rset.getString ( "s_name" ),
						   UUIDFactory.parseUUID ( sql_rset.getString ( "s_id" ) )
						   );
		}
		
		return v_result;
	}
}

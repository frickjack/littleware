/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.db.postgres;

import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.*;
import java.sql.*;

import littleware.asset.*;
import littleware.asset.server.AbstractDbReader;
import littleware.asset.server.JdbcTransaction;
import littleware.base.*;


/**
 * Data loader for to-id based search
 */
public class DbAssetIdsToLoader extends AbstractDbReader<Set<UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( DbAssetIdsToLoader.class.getName() );
    private final int           oi_client_id;
	
	private final UUID      ou_to;
	private final AssetType on_type;
	
	/**
     * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbAssetIdsToLoader ( UUID u_to, AssetType n_type, int i_client_id, Provider<JdbcTransaction> provideTrans ) {
		super ( "SELECT * FROM littleware.getAssetIdsTo( ?, ?, ? )", false, provideTrans );
		ou_to = u_to;
		on_type = n_type;
        oi_client_id = i_client_id;
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
		sql_stmt.setString ( 1, UUIDFactory.makeCleanString ( ou_to ) );
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
    @Override
	public Set<UUID> loadObject( ResultSet sql_rset ) throws SQLException {
        Set<UUID> v_result = new HashSet<UUID> ();
        
		while ( sql_rset.next () ) {
			v_result.add ( 
						   UUIDFactory.parseUUID ( sql_rset.getString ( "s_id" ) )
						   );
		}
        return v_result;
	}
}

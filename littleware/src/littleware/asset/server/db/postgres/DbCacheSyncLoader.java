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


/**
 * Data loader for cache-synchronization data.
 * The database keeps track of which clients load and update
 * which assets, then allows a client to ask the database
 * for an update on assets the client load that have since been changed by other
 * clients.
 */
public class DbCacheSyncLoader 
        extends AbstractDbReader<Map<UUID,Asset>,String> {
	private static final Logger olog_generic = Logger.getLogger ( DbCacheSyncLoader.class.getName() );
	
	private final int       oi_src;
    private final TransactionManager  omgr_trans;
	
	/**
     * Constructor registers query with super-class
	 */
	public DbCacheSyncLoader ( int i_src, TransactionManager mgr_trans ) {
		super ( "SELECT * FROM littleware.synchCache( ? )", false, mgr_trans );
		oi_src = i_src;
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
		sql_stmt.setInt( 1, oi_src );
        
		return super.executeStatement ( sql_stmt, null );
	}   
	
	/**
     * Return mapping from UUID of changed assets to changed assets,
     * UUID to null if asset has been deleted.
     */
	public Map<UUID,Asset> loadObject( ResultSet sql_rset ) throws SQLException {
		DbAssetLoader db_loader = new DbAssetLoader ( oi_src, omgr_trans );
		Map<UUID,Asset>    v_result = new HashMap<UUID,Asset> ();
		
		while ( sql_rset.next () ) {
            UUID   u_id = UUIDFactory.parseUUID ( sql_rset.getString ( "s_id" ) );
            String s_name = sql_rset.getString ( "s_name" );
            
            if ( null == s_name ) {
                v_result.put ( u_id, null );
            } else {
                v_result.put ( u_id, db_loader.loadObjectReady ( sql_rset ) );
            }
		}
		return v_result;
	}
}


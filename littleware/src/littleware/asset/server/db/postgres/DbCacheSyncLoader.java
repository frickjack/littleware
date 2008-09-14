package littleware.asset.server.db.postgres;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.sql.*;

import littleware.asset.server.AbstractDbReader;
import littleware.asset.*;
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
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.postgres.DbCacheSyncLoader" );
	
	private final int       oi_src;
	
	/**
     * Constructor registers query with super-class
	 */
	public DbCacheSyncLoader ( int i_src ) {
		super ( "SELECT * FROM littleware.synchCache( ? )", false );
		oi_src = i_src;
	}
	
	/**
     * Implementation sets argument sql_stmt.setObject ( 1, x_arg ),
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @param s_arg is ignored
	 * @return ResultSet from execution of query or callable statement
	 */
	public ResultSet executeStatement( PreparedStatement sql_stmt, String s_arg ) throws SQLException {
		sql_stmt.setInt( 1, oi_src );
        
		return super.executeStatement ( sql_stmt, null );
	}   
	
	/**
     * Return mapping from UUID of changed assets to changed assets,
     * UUID to null if asset has been deleted.
     */
	public Map<UUID,Asset> loadObject( ResultSet sql_rset ) throws SQLException {
		DbAssetLoader db_loader = new DbAssetLoader ( oi_src );
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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


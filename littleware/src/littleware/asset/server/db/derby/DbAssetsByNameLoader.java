package littleware.asset.server.db.derby;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.sql.*;

import javax.sql.DataSource;
import littleware.db.*;
import littleware.asset.*;
import littleware.base.*;



/**
 * Data loader for asset-search by name.
 * Assumes the DbCacheManager has already verified
 * that the query's data is in the cache.
 * Returns set of UUID - assumes the CacheManager
 * can pull the assets themselves out of its internal
 * asset-object cache.
 */
class DbAssetsByNameLoader extends AbstractDerbyReader<Set<UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.derby.DbAssetsByNameLoader" );
	
	private final String    os_name;
	private final AssetType on_type;
	private final UUID      ou_home;
	
	/**
	 * Constructor registers query with super-class
	 */
	public DbAssetsByNameLoader ( DataSource dataSource, String s_name, AssetType n_type, UUID u_home ) {
        super ( dataSource,
                "SELECT s_id FROM littleware.asset_cache WHERE " +
                "(s_pk_type=? " +
                "  OR s_pk_type IN (SELECT s_descendent_id FROM littleware.x_asset_type_tree WHERE s_ancestor_id=?)) " +
                " AND s_name=?" +
                ((null == u_home) ? "" : " AND s_id_home=?"),
                false
                );
		os_name = s_name;
		on_type = n_type;
		ou_home = u_home;
	}
	
	/**
	 * Implementation sets argument 
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @return ResultSet from execution of query or callable statement
	 */
	public ResultSet executeStatement( PreparedStatement sql_stmt, String s_ignore ) throws SQLException {
        String s_type_id = UUIDFactory.makeCleanString ( on_type.getObjectId () );
		sql_stmt.setString ( 1, s_type_id );
		sql_stmt.setString ( 2, s_type_id );        
		sql_stmt.setString ( 3, os_name );
		if ( null != ou_home ) {
			sql_stmt.setString( 4, UUIDFactory.makeCleanString ( ou_home ) );
		}
		
		return super.executeStatement ( sql_stmt, null );
	}
	
	
	
	/**
	 * Pull data from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
	 * @exception SQLException on failure to extract data
	 */
	public Set<UUID> loadObject( ResultSet sql_rset ) throws SQLException {
		Set<UUID> v_result = new HashSet<UUID> ();
		
		while ( sql_rset.next () ) {
			v_result.add ( UUIDFactory.parseUUID( sql_rset.getString ( 1 ) ) );
		}
		
		return v_result;
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


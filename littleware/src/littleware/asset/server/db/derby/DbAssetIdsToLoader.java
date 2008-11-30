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
* Data loader for home asset name-id map.
 * Assumes the DbCacheManager has already verified
 * that the query's data is in the cache.
 */
public class DbAssetIdsToLoader extends AbstractDerbyReader<Set<UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.derby.DbAssetIdsToLoader" );
    
	private final UUID       ou_to;
	private final AssetType  on_type;
	
	private final static String ms_query = "SELECT s_id FROM littleware.asset_cache WHERE s_id_to=? " +
        "AND (s_pk_type=? OR s_pk_type IN (SELECT s_descendent_id " +
        "FROM littleware.x_asset_type_tree WHERE s_ancestor_id=?))";
    
	/**
     * Constructor registers query with super-class, and stashes
	 * query arguments.
	 */
	public DbAssetIdsToLoader ( DataSource dataSource, UUID u_to, AssetType n_type ) {
        super( dataSource, ms_query, false );

		ou_to = u_to;
		on_type = n_type;
	}
	
	/**
     * Implementation sets argument 
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @return ResultSet from execution of query or callable statement
	 */
	public ResultSet executeStatement( PreparedStatement sql_stmt, String s_ignore ) throws SQLException {
		sql_stmt.setString ( 1, UUIDFactory.makeCleanString ( ou_to ) );
        String s_pk_type = UUIDFactory.makeCleanString ( on_type.getObjectId () );
        sql_stmt.setString ( 2, s_pk_type );
        sql_stmt.setString ( 3, s_pk_type );        
		
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
            v_result.add ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id" ) ) );
        }

        return v_result;
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.asset.server.db.derby;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;

import littleware.db.*;
import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.UUIDFactory;

/**
 * DbWriter implementation for adding home-id data to the Derby cache.
 */
public class DbHomeIdsSaver extends AbstractDerbyWriter<Map<String,UUID>> {
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.derby.DbHomeIdsSaver" );
	private static final String          MS_QUERY_HOMEIDS = "homeids";

	/**
	 * Return the string that identifies a homeids lookup in the
	 * query-log. 
	 */
	public static String getQueryLogKey () { return MS_QUERY_HOMEIDS; }
	
	/**
	 * Constructor registers query with super-class
	 */
	public DbHomeIdsSaver () {
		super ( "UPDATE littleware.asset_cache SET s_name=?, s_pk_type='" + 
				UUIDFactory.makeCleanString ( AssetType.HOME.getObjectId () ) +
				"' WHERE s_id=?", 
				false
				);
	}
	
	/**
	 * Save the homeid data to the derby cache database, and
	 * update DbDerbyCacheManager's query log.
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, Map<String,UUID> v_data ) throws SQLException {
		Map<String,UUID>  v_missing_assets = new HashMap<String,UUID> ();
		
		for ( Map.Entry<String,UUID> entry_x : v_data.entrySet () ) {
			sql_stmt.setString( 1, entry_x.getKey () );
			sql_stmt.setString ( 2, UUIDFactory.makeCleanString ( entry_x.getValue () ) );
			if ( 0 == sql_stmt.executeUpdate () ) {
				// this id must not be in the cache yet - need to add it
				v_missing_assets.put ( entry_x.getKey (), entry_x.getValue () );
			}
		}
		PreparedStatement sql_insert = null;
		try {
			// Setup a supplementary statement to do needed INSERTS
			sql_insert = sql_stmt.getConnection ().prepareStatement 
				( "INSERT INTO littleware.asset_cache (s_id, s_name, s_pk_type) VALUES ( ?, ?, '" +
				  UUIDFactory.makeCleanString ( AssetType.HOME.getObjectId () ) + "')" 
				  );
			for ( Map.Entry<String,UUID> entry_x : v_missing_assets.entrySet () ) {
				sql_insert.setString ( 1, UUIDFactory.makeCleanString( entry_x.getValue () ) );
				sql_insert.setString ( 2, entry_x.getKey () );
				sql_insert.executeUpdate ();
			}
			DerbyDbCacheManager.addQueryToLog ( getQueryLogKey () );
			olog_generic.log ( Level.INFO, "Just added query " + getQueryLogKey () + 
							   " to query cache" 
							   );
		} finally {
			// Don't leave that supplementary statement open!
			Janitor.cleanupSession ( sql_insert );
		}
		return false;
 	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


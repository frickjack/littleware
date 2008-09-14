package littleware.asset.server.db.derby;

import java.util.*;
import java.sql.*;

import littleware.db.*;
import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.UUIDFactory;

/**
 * DbWriter implementation clearing assets out of the cache.
 */
public class DbEraser extends AbstractDerbyWriter<UUID> {
	/**
	 * Constructor registers query with super-class
	 */
	public DbEraser () {
		super ( "DELETE FROM littleware.asset_cache WHERE s_id=?",
				false 
				);
	}
	
	/**
	 * Remove the specified asset from the derby-cache,
	 * or just clear the whole derby database if u_id is null.
	 *
	 * @param u_id of deleted asset, or null to zero out the database
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, UUID u_id ) throws SQLException {
		if ( null != u_id ) {
			sql_stmt.setString( 1, UUIDFactory.makeCleanString ( u_id ) );
			sql_stmt.executeUpdate ();
			return false;
		} 
		// Erase everything
		PreparedStatement sql_stmt_clear = sql_stmt.getConnection ().prepareStatement (
						     "DELETE FROM littleware.asset_cache"
																					   );
		
		try {
			sql_stmt_clear.executeUpdate ();
			DerbyDbCacheManager.clearQueryLog ();
			return false;
		} finally {
			Janitor.cleanupSession ( sql_stmt_clear );
		}		
 	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


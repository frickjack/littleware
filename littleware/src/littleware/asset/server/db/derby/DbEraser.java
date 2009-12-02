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

package littleware.asset.server.db.derby;

import java.util.*;
import java.sql.*;

import javax.sql.DataSource;
import littleware.db.*;
import littleware.base.UUIDFactory;

/**
 * DbWriter implementation clearing assets out of the cache.
 */
public class DbEraser extends AbstractDerbyWriter<UUID> {
	/**
	 * Constructor registers query with super-class
	 */
	public DbEraser ( DataSource dataSource ) {
		super ( dataSource, "DELETE FROM littleware.asset_cache WHERE s_id=?",
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
            // clear asset-type data too
            sql_stmt_clear.close();
            sql_stmt_clear = sql_stmt.getConnection().prepareStatement( "DELETE FROM littleware.x_asset_type_tree" );
            sql_stmt_clear.executeUpdate();
			DerbyDbCacheManager.clearQueryLog ();
			return false;
		} finally {
			Janitor.cleanupSession ( sql_stmt_clear );
		}		
 	}
	
}

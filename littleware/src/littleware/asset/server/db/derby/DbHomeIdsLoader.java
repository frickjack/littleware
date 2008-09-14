package littleware.asset.server.db.derby;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.sql.*;

import littleware.db.*;
import littleware.asset.*;
import littleware.base.*;



/**
 * Data loader for home asset name-id map.
 * Assumes the DbCacheManager has already verified
 * that the query's data is in the cache.
 */
public class DbHomeIdsLoader extends AbstractDerbyReader<Map<String,UUID>,String> {
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.derby.DbHomeIdsLoader" );

	
	/**
	* Constructor registers query with super-class
	 */
	public DbHomeIdsLoader () {
		super ( "SELECT s_id, s_name FROM littleware.asset_cache WHERE s_pk_type='" +
				UUIDFactory.makeCleanString ( AssetType.HOME.getObjectId () ) + "'", false );
	}
	
	
	/**
	 * Pull data from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


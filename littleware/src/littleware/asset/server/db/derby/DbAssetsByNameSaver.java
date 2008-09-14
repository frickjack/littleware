package littleware.asset.server.db.derby;

import java.util.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.db.*;
import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.base.UUIDFactory;

/**
 * DbWriter implementation for creating new assets,
 * and updating existing assets.
 */
public class DbAssetsByNameSaver extends AbstractDerbyWriter<Set<Asset>> {	
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.derby.DbAssetIdsFromLoader" );
	private static final String          MS_QUERY = "byname:";
	
	private String  os_my_query = null;
	
	/**
	 * Return the string that identifies a by-name lookup in the
	 * query-log. 
	 */
	public static String getQueryLogKey ( String s_name, AssetType n_type, UUID u_home ) { 
		String s_result = MS_QUERY; 
		if ( null == u_home ) {
			s_result += "null,";
		} else {
			s_result += u_home + ",";
		}
		s_result += n_type + ",";
		s_result += s_name;
		
		return s_result;
	}
	
	/**
	 * Constructor registers a bogus query with the super-class -
	 * this class actually just hands off db updates to DbAssetSaver internally.
	 */
	public DbAssetsByNameSaver ( String s_name, AssetType n_type, UUID u_home ) {
		super ( "UPDATE littleware.asset_cache SET s_name=? WHERE s_id=?", 
				false 
				);
		os_my_query = getQueryLogKey ( s_name, n_type, u_home );
	}
	
	/**
	 * Save the homeid data to the derby cache database, and
	 * update DbDerbyCacheManager's query log.
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, Set<Asset> v_data ) throws SQLException {
		DbAssetSaver  db_saver = new DbAssetSaver ();
		
		for ( Asset a_save : v_data ) {
			db_saver.saveObject ( sql_stmt.getConnection (), a_save );
		}
		DerbyDbCacheManager.addQueryToLog ( os_my_query );
		return false;
 	}
}
	
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


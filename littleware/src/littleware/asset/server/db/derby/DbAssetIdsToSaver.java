package littleware.asset.server.db.derby;

import java.util.*;
import java.sql.*;

import javax.sql.DataSource;
import littleware.asset.*;
import littleware.base.AssertionFailedException;
import littleware.base.UUIDFactory;
import littleware.db.*;

/**
 * DbWriter implementation for saving ids-to data to the cache
 */
public class DbAssetIdsToSaver extends AbstractDerbyWriter<Set<UUID>> {
	private static final String          MS_QUERY_FORMAT = "assetidsto:%1$s,%2$s";
	private final UUID       ou_to;
	private final AssetType  on_type;
	
	/**
     * Return the string that identifies a homeids lookup in the
	 * query-log. 
	 */
	public static String getQueryLogKey ( UUID u_to, AssetType n_type ) { 
        return String.format ( MS_QUERY_FORMAT, u_to.toString (), n_type.getObjectId () );
	}
    
	/**
     * Constructor registers query with super-class
	 * 
	 * @param u_to id of parent result-assets link from
	 * @param n_type of return-assets, null indicates any type
	 */
	public DbAssetIdsToSaver ( DataSource dataSource, UUID u_to, AssetType n_type ) {
        super ( dataSource,
                "UPDATE littleware.asset_cache SET s_id_to=?, s_pk_type=? WHERE s_id=?",
                false
                );        
		ou_to = u_to;
		on_type = n_type;
	}
	
	/**
     * Save the to-id data to the derby cache database, and
	 * update DbDerbyCacheManager's query log.
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, Set<UUID> v_data ) throws SQLException {
		List<UUID>  v_missing_assets = new ArrayList<UUID> ();
		String  s_id_to = UUIDFactory.makeCleanString ( ou_to );
		
        DerbyDbCacheManager.saveAssetTypeIfNecessary ( on_type, sql_stmt.getConnection () );
        
		sql_stmt.setString ( 1, s_id_to );
        sql_stmt.setString ( 2, UUIDFactory.makeCleanString ( on_type.getObjectId () ) );
        
        List<UUID>    v_copy = new ArrayList<UUID> ( v_data.size () );
        
        PreparedStatement stmt_check1st = 
            sql_stmt.getConnection ().prepareStatement ( "SELECT s_pk_type FROM littleware.asset_cache WHERE s_id=?" );
        ResultSet         rset_check1st = null;
        
        try {
            for ( UUID u_entry : v_data ) {
                String s_id = UUIDFactory.makeCleanString ( u_entry );
                stmt_check1st.setString ( 1, s_id );
                rset_check1st = stmt_check1st.executeQuery ();
                
                if ( rset_check1st.next () ) {
                    String s_type_id = rset_check1st.getString ( 1 );
                    UUID   u_type = ((null == s_type_id) ? null : UUIDFactory.parseUUID ( s_type_id ));
                    
                    if ( (u_type == null)
                         || (! AssetType.getMember ( u_type ).isA ( on_type ))
                         ) 
                    {
                        // then update type, on_type is more specific than what we have stored
                        sql_stmt.setString ( 3, s_id );
                        sql_stmt.addBatch ();                        
                        v_copy.add( u_entry );
                    } // else - update not necessary for this asset
                } else {
                    // need to insert some data
                    v_missing_assets.add ( u_entry );
                }
                rset_check1st.close ();
            }
        } catch ( NoSuchTypeException e ) {
            throw new AssertionFailedException ( "Invalid type id in db", e );
        } finally {
            Janitor.cleanupSession ( rset_check1st, stmt_check1st );
        }
        
        int[] v_result = sql_stmt.executeBatch ();
        int   i_count = 0;
        
        for ( int i_result : v_result ) {
            if ( i_result == 0 ) {
                v_missing_assets.add ( v_copy.get ( i_count ) );
            }
            ++i_count;
        }
        
        if ( ! v_missing_assets.isEmpty () ) {        
            // Need to insert some new entries
            PreparedStatement sql_insert = null;
            try {
                // Setup a supplementary statement to do needed INSERTS
                sql_insert = sql_stmt.getConnection ().prepareStatement 
                    ( "INSERT INTO littleware.asset_cache (s_id,s_id_to, s_pk_type) VALUES ( ?, ?, ? )" );
                
                sql_insert.setString ( 2, s_id_to );
                sql_insert.setString ( 3, UUIDFactory.makeCleanString ( on_type.getObjectId () ) );
                for ( UUID u_entry : v_missing_assets ) {
                    sql_insert.setString ( 1, UUIDFactory.makeCleanString( u_entry ) );
                    sql_insert.addBatch ();
                }
                sql_insert.executeBatch ();
                
            } finally {
                // Don't leave that supplementary statement open!
                Janitor.cleanupSession ( sql_insert );
            }
        }
        DerbyDbCacheManager.addQueryToLog ( getQueryLogKey ( ou_to, on_type ) );

		return false;
 	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


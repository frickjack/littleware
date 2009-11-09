package littleware.asset.server.db.derby;

import java.util.logging.Logger;
import java.util.*;
import java.sql.*;

import javax.sql.DataSource;
import littleware.asset.*;
import littleware.base.*;



/**
 * Data loader for home asset name-id map.
 * Assumes the DbCacheManager has already verified
 * that the query's data is in the cache.
 */
public class DbAssetIdsFromLoader extends AbstractDerbyReader<Map<String,UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.derby.DbAssetIdsFromLoader" );

	private final UUID       ou_from;
	private final AssetType  on_type;
	
    private static final String ms_query_with_type =
        "SELECT s_id, s_name FROM littleware.asset_cache WHERE s_id_from=? " +
        "AND (s_pk_type=? OR s_pk_type IN (SELECT s_descendent_id " +
        "FROM littleware.x_asset_type_tree WHERE s_ancestor_id=?))";
    private static final String ms_query_no_type = "SELECT s_id, s_name FROM littleware.asset_cache WHERE s_id_from=?";

    private final DataSource odataSource;


    /**
     * Constructor registers query with super-class, and stashes
	 * query arguments.
	 */
    public DbAssetIdsFromLoader ( DataSource dataSource, UUID u_from, AssetType n_type ) {
        super ( dataSource,
                ((null == n_type) ? ms_query_no_type : ms_query_with_type),
                 false
                 );
        ou_from = u_from;
		on_type = n_type;
        odataSource = dataSource;
	}
    
	
	/**
	 * Implementation sets argument 
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @return ResultSet from execution of query or callable statement
	 */
    @Override
	public ResultSet executeStatement( PreparedStatement sql_stmt, String s_ignore ) throws SQLException {
		sql_stmt.setString ( 1, UUIDFactory.makeCleanString ( ou_from ) );
		if ( null != on_type ) {
            String s_pk_type = UUIDFactory.makeCleanString ( on_type.getObjectId () );
			sql_stmt.setString ( 2, s_pk_type );
            sql_stmt.setString ( 3, s_pk_type );
		}
		
		return super.executeStatement ( sql_stmt, null );
	}
	
	/**
	 * Pull data from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
	 * @exception SQLException on failure to extract data
	 */
	public Map<String,UUID> loadObject( ResultSet sql_rset ) throws SQLException {
		DbHomeIdsLoader db_loader = new DbHomeIdsLoader ( odataSource );
		return db_loader.loadObject ( sql_rset );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


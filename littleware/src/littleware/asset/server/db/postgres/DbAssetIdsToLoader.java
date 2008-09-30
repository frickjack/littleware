package littleware.asset.server.db.postgres;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.sql.*;

import littleware.asset.*;
import littleware.asset.server.AbstractDbReader;
import littleware.base.*;
import littleware.db.*;


/**
 * Data loader for to-id based search
 */
public class DbAssetIdsToLoader extends AbstractDbReader<Set<UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.postgres.DbAssetIdsToLoader" );
    private final int           oi_client_id;
	
	private UUID      ou_to = null;
	private AssetType on_type = null;
	
	/**
        * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbAssetIdsToLoader ( UUID u_to, AssetType n_type, int i_client_id ) {
		super ( "SELECT * FROM littleware.getAssetIdsTo( ?, ?, ? )", false );
		ou_to = u_to;
		on_type = n_type;
        oi_client_id = i_client_id;
	}
	
	/**
     * Implementation sets argument sql_stmt.setObject ( 1, x_arg ),
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @param s_arg is ignored
	 * @return ResultSet from execution of query or callable statement
	 */
	public ResultSet executeStatement( PreparedStatement sql_stmt, String s_arg ) throws SQLException {		
		sql_stmt.setString ( 1, UUIDFactory.makeCleanString ( ou_to ) );
		if ( null != on_type ) {
			sql_stmt.setString ( 2, UUIDFactory.makeCleanString ( on_type.getObjectId () ) );
		} else {
			sql_stmt.setNull ( 2, Types.VARCHAR );
		}
        sql_stmt.setInt ( 3, oi_client_id );
		return super.executeStatement ( sql_stmt, null );
	}
	
	
	/**
     * Pull an Asset from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
	 * @return name to id map
	 * @exception SQLException on failure to extract data
	 */
	public Set<UUID> loadObject( ResultSet sql_rset ) throws SQLException {
        Set<UUID> v_result = new HashSet<UUID> ();
        
		while ( sql_rset.next () ) {
			v_result.add ( 
						   UUIDFactory.parseUUID ( sql_rset.getString ( "s_id" ) )
						   );
		}
        return v_result;
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


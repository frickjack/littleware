package littleware.asset.server.db.postgres;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.*;
import java.sql.*;

import littleware.asset.server.AbstractDbReader;
import littleware.asset.*;
import littleware.base.*;
import littleware.db.*;


/**
 * Data loader for home asset name-id map
 */
public class DbHomeIdLoader extends AbstractDbReader<Map<String,UUID>,String> {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.postgres.DbHomeIdLoader" );
    private final int           oi_client_id;
	
	
	/**
	 * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbHomeIdLoader ( int i_client_id ) {
		super ( "SELECT * FROM littleware.getHomeIdDictionary(?)", false );
        oi_client_id = i_client_id;
	}
	
		
    public ResultSet executeStatement( PreparedStatement sql_stmt, String s_arg ) throws SQLException {
		sql_stmt.setInt ( 1, oi_client_id );
		return super.executeStatement ( sql_stmt, null );
	}
    
	/**
	 * Pull an Asset from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
	 * @return Asset or null if no data
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


package littleware.asset.server.db.postgres;

import java.sql.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;

import littleware.asset.*;
import littleware.asset.server.AbstractDbWriter;
import littleware.base.*;
import littleware.db.*;

/**
 * DbWriter implementation for creating and updating an asset.
 */
public class DbAssetSaver extends AbstractDbWriter<Asset> {
	private final int  oi_client_id;
    
	/**
	 * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbAssetSaver ( int i_client_id ) {
		super ( "{ ? = call littleware.saveAsset( ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ? ) }", true );
        oi_client_id = i_client_id;
	}
	
	/**
	 * Save the specified object by setting parameters against and executing
	 * the supplied prepared statement.
	 *
	 * @param sql_stmt connection to execute a save operation against - possible obtained from
	 *                 this.prepareStatement ( sql_conn, a_object ),
	 *                 possibly null for some aggregate-based saves.
	 * @param a_object to save
	 * @return result of sql_stmt.execute()
	 * @exception SQLException pass through exceptions thrown by sql_stmt access
	 */
	public boolean saveObject ( PreparedStatement sql_stmt, Asset a_object ) throws SQLException {
		CallableStatement sql_call = (CallableStatement) sql_stmt;
		sql_call.registerOutParameter ( 1, Types.BIGINT );
		sql_call.setString ( 2, a_object.getName () );
		sql_call.setString ( 3, UUIDFactory.makeCleanString ( a_object.getAssetType ().getObjectId () ) );
		sql_call.setString ( 4, UUIDFactory.makeCleanString ( a_object.getFromId () ) );
		sql_call.setString ( 5, UUIDFactory.makeCleanString ( a_object.getToId () ) );
		sql_call.setString ( 6, UUIDFactory.makeCleanString ( a_object.getCreatorId () ) );
		sql_call.setString ( 7, UUIDFactory.makeCleanString ( a_object.getAclId () ) );
		sql_call.setString ( 8, a_object.getData () );
		//sql_call.setFloat ( 9, a_object.getValue () );
		sql_call.setBigDecimal ( 9, new java.math.BigDecimal ( (double) a_object.getValue ().floatValue () ) );
		sql_call.setString ( 10, a_object.getComment () );
		sql_call.setString ( 11, a_object.getLastUpdate () );
		sql_call.setString ( 12, UUIDFactory.makeCleanString ( a_object.getHomeId () ) );
		
		java.util.Date t_start = a_object.getStartDate ();
		java.util.Date t_end   = a_object.getEndDate ();
		/*....
		sql_call.setTimestamp ( 13, (null == t_start) ? null : new Timestamp( t_start.getTime () ) );
		sql_call.setTimestamp ( 14, (null == t_end) ? null : new Timestamp( t_end.getTime () ) );
		...*/
		DateFormat format_date = new SimpleDateFormat ( "yyyy-MM-dd HH:mm:ss" );

		sql_call.setString ( 13, (null == t_start) ? null : format_date.format ( t_start ) );
		sql_call.setString ( 14, (null == t_start) ? null : format_date.format ( t_end ) );

		sql_call.setString ( 15, UUIDFactory.makeCleanString ( a_object.getObjectId () ) );
		sql_call.setString ( 16, UUIDFactory.makeCleanString ( a_object.getOwnerId () ) );
        
        long l_transaction = a_object.getTransactionCount ();
        if ( l_transaction > 0 ) {
            sql_call.setLong( 17, l_transaction );
        } else {
            sql_call.setNull ( 17, Types.BIGINT );
        } 
        sql_call.setInt ( 18, oi_client_id );
        
		boolean b_result = sql_call.execute ();
        a_object.setTransactionCount ( sql_call.getLong( 1 ) );
        return b_result;
 	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


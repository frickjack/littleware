/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.server.db.postgres;

import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.UUID;
import java.sql.*;

import littleware.asset.server.AbstractDbReader;
import littleware.asset.*;
import littleware.asset.server.JdbcTransaction;
import littleware.base.*;


/**
 * Data loader for an asset identified by an id
 */
public class DbAssetLoader extends AbstractDbReader<Asset,UUID> {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.server.db.postgres.DbAssetLoader" );
	private final int oi_client_id;
	
	
	/**
	 * Constructor registers query with super-class,
     * and stashes the local client id.
	 */
	public DbAssetLoader ( int i_client_id, Provider<JdbcTransaction> provideTrans ) {
		//super ( "{ ? = call littleware.getAsset( ? ) }", true );
		super( "SELECT * FROM littleware.getAsset( ?, ? )", // AS " +
			   //"( s_id                VARCHAR, s_name              VARCHAR, s_id_home           VARCHAR, l_last_transaction  BIGINT,  s_pk_type           VARCHAR, s_id_creator        VARCHAR, s_id_updater        VARCHAR, s_id_owner          VARCHAR, f_value             NUMERIC, s_id_acl            VARCHAR, s_comment           VARCHAR, s_last_change       VARCHAR, s_data              VARCHAR, s_id_from           VARCHAR, s_id_to             VARCHAR, t_created           TIMESTAMP, t_updated           TIMESTAMP, t_last_accessed     TIMESTAMP, t_start             TIMESTAMP, t_end  TIMESTAMP) ", 
			   false, provideTrans );
        oi_client_id = i_client_id;
	}
	
	/**
	 * Implementation sets argument sql_stmt.setObject ( 1, x_arg ),
	 * then calls through to super.executeStatement ( sql_stmt, null )
	 *
	 * @return ResultSet from execution of query or callable statement
	 */
    @Override
	public ResultSet executeStatement( PreparedStatement sql_stmt, UUID u_arg ) throws SQLException {
		sql_stmt.setString ( 1, u_arg.toString ().replaceAll ( "-", "" ).toUpperCase () );
		sql_stmt.setInt ( 2, oi_client_id );
		return super.executeStatement ( sql_stmt, null );
	}
	

    /**
     * Protected method - shared in package to allow simple
	 * loading of asset sets.  This method begins loading
	 * data out of the supplied result-set immediately,
	 * and assumes the caller has already done a
	 *      sql_rset.next() check
	 */
    Asset loadObjectReady ( ResultSet sql_rset ) throws SQLException {
		AssetType   n_type = null;
		Asset       a_new = null;
		
		try {
		    n_type = AssetType.getMember(  UUIDFactory.parseUUID( sql_rset.getString( "s_pk_type" ) ) );
			olog_generic.log ( Level.FINE, "Allocating object of type: " + n_type );
			a_new = n_type.create ();
		} catch ( SQLException e ) {
			throw e;
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught unexpected instantiating new asset of type " + n_type +
							   ", caught: " + e );
			throw new SQLException ( "Failure to allocate asset of type " + n_type + ", caught: " + e );
		}
		
		a_new.setObjectId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id" ) ) );
		a_new.setName ( sql_rset.getString ( "s_name" ) );
		olog_generic.log ( Level.FINE, "Just set asset name to: " + sql_rset.getString ( "s_name" ) );
		
		a_new.setHomeId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id_home" ) ) );
		a_new.setTransactionCount ( sql_rset.getLong ( "l_last_transaction" ) );
		a_new.setAssetType ( n_type );
		a_new.setCreatorId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id_creator" ) ) );
		a_new.setLastUpdaterId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id_updater" ) ) );
		a_new.setOwnerId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id_owner" ) ) );
		a_new.setValue ( sql_rset.getFloat ( "f_value" ) );
		a_new.setAclId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id_acl" ) ) );
		a_new.setComment ( sql_rset.getString ( "s_comment" ) );
		a_new.setLastUpdate ( sql_rset.getString ( "s_last_change" ) );
		try {
			a_new.setData ( sql_rset.getString ( "s_data" ) );
		} catch ( BaseException e ) {
			throw new SQLException ( "Invalid data in db, caught: " + e );
		}
		a_new.setFromId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id_from" ) ) );
		a_new.setToId ( UUIDFactory.parseUUID ( sql_rset.getString ( "s_id_to" ) ) );
		a_new.setCreateDate ( sql_rset.getTimestamp ( "t_created" ) );
		a_new.setLastUpdateDate ( sql_rset.getTimestamp ( "t_updated" ) );
		a_new.setLastAccessDate ( sql_rset.getTimestamp ( "t_last_accessed" ) );
		a_new.setStartDate ( sql_rset.getTimestamp ( "t_start" ) );
		a_new.setEndDate ( sql_rset.getTimestamp ( "t_end" ) );
		
		return a_new;
	}		
		
	/**
	 * Not actually used internally, but implements a poll of
	 * an Asset from a canonical littleware.asset ResultSet
	 *
	 * @param sql_rset to pull data from
	 * @return Asset or null if no data
	 * @exception SQLException on failure to extract data
	 */
    @Override
	public Asset loadObject( ResultSet sql_rset ) throws SQLException {
		if ( ! sql_rset.next () ) {
			return null;
		}
		return loadObjectReady ( sql_rset );
	}
}


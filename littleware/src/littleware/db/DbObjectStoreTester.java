package littleware.db.test;

import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ResourceBundle;
import javax.sql.DataSource;
import java.sql.*;
import java.security.GuardedObject;

import junit.framework.*;

import littleware.base.*;
import littleware.db.*;

/**
 * Little test case for DbOjbectStore implementation
 */
public class DbObjectStoreTester extends TestCase {
	private static final Logger         ox_logger = Logger.getLogger ( "littleware.db.test.DbObjectStoreTester" );
	
	/**
	 * Constructor stashes object-store to test against.
	 * 
	 * @param s_name of test method to run
	 */
	public DbObjectStoreTester ( String s_name ) {
		super ( s_name );
	}

	/** No setup necessary */
	public void setUp () {}
	/** No tearDown necessary */
	public void tearDown () {}
	
	/**
	 * Just try to read in the littleware.principal_type table
	 * via the DbEnumReader
	 */
	public void testSimpleLoad () {
		Connection        sql_conn = null;
		PreparedStatement sql_stmt = null;
		ResultSet         sql_rset = null;
		
		try {
			SqlResourceBundle  x_bundle = SqlResourceBundle.getBundle ();
			DataSource      sql_data_source = (DataSource)
				((GuardedObject) x_bundle.getObject ( SqlResourceBundle.Content.LittlewareConnectionFactory )).getObject ();
			/*...
			DbReader<List<EnumType>,String>  x_reader = new DbEnumReader ( "littleware.principal_type" );
			List<EnumType> v_result = x_reader.loadObject ( sql_data_source, null );
			assertTrue ( "Non-empty principal-type enum-list", ! v_result.isEmpty () );
			*/
			/*
		} catch ( LittleSqlException e ) {
			ox_logger.log ( Level.INFO, "Caught unexepcted: " + e );
			assertTrue ( "Caught unexpected: " + e, false );
		} catch ( SQLException e ) {
			ox_logger.log ( Level.INFO, "Caught unexepcted: " + e );
			assertTrue ( "Caught unexpected: " + e, false );
			*/
		} finally {
			Janitor.cleanupSession ( sql_rset, sql_stmt, sql_conn );
		}
			
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


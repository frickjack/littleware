package littleware.db.test;


import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;

import junit.framework.*;

import littleware.db.*;


/**
 * Little test case for connection factory
 */
public class ConnectionFactoryTester extends TestCase {
	
	private static Logger		ox_logger = Logger.getLogger ( "littleware.db.test" );
	
	private ConnectionFactory   ox_factory;
	private String              os_test_query;
	
	/**
	 * Constructor takes a connection factory and a test query to run
	 * against a checked out connection.  The supplied query should just return 'Hello'.
	 * 
	 * @param s_name of test method to run
	 * @param x_factory to test against
	 * @param s_test_query that returns 'Hello' result
	 */
	public ConnectionFactoryTester ( String s_name,
									 ConnectionFactory x_factory, String s_test_query ) {
		super( s_name );
		ox_factory = x_factory;
		os_test_query = s_test_query;
	}
	
	/** No setup necessary */
	public void setUp () {}
	/** No tearDown necessary */
	public void tearDown () {}
	
	/**
	 * Run the test query
	 */
	public void testQuery () {
		Connection x_conn = null;
		Statement  x_stmt = null;
		ResultSet  x_rset = null;
		try { 
			x_conn = ox_factory.getConnection ();
			x_stmt = x_conn.createStatement ();
			x_rset = x_stmt.executeQuery ( os_test_query );
			
			assertTrue ( "Resultset not empty from test query: " + os_test_query,
						 x_rset.next () );
			assertTrue ( "Resultset.getString(1) == Hello",
						 "Hello".equals ( x_rset.getString ( 1 ) )
						);
			ox_logger.log ( Level.INFO, "Test query worked, got: " + x_rset.getString ( 1 ) );
		} catch ( LittleSqlException e ) {
			ox_logger.log ( Level.SEVERE, "Caught unexpected: " + e );
			assertTrue ( "Caught unexpected: " + e, false );
		} catch ( SQLException e ) {
			ox_logger.log ( Level.SEVERE, "Caught unexpected: " + e );
			assertTrue ( "Caught unexpected: " + e, false );
		} finally {
			Janitor.cleanupSession ( x_rset, x_stmt, x_conn, ox_factory );
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


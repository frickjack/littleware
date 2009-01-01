/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.db.test;


import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;

import javax.sql.DataSource;
import junit.framework.*;

import littleware.db.*;


/**
 * Little test case for connection factory
 */
public class ConnectionFactoryTester extends TestCase {
	
	private static final Logger		ox_logger = Logger.getLogger ( ConnectionFactoryTester.class.getName() );
	
	private DataSource          odsource;
	private String              os_test_query;
	
	/**
	 * Constructor takes a connection factory and a test query to run
	 * against a checked out connection.  The supplied query should just return 'Hello'.
	 * 
	 * @param s_name of test method to run
	 * @param dsource to test against
	 * @param s_test_query that returns 'Hello' result
	 */
	public ConnectionFactoryTester ( String s_name,
									 DataSource dsource, String s_test_query ) {
		super( s_name );
		odsource = dsource;
		os_test_query = s_test_query;
	}
	
	/** No setup necessary */
    @Override
	public void setUp () {}
	/** No tearDown necessary */
    @Override
	public void tearDown () {}
	
	/**
	 * Run the test query
	 */
	public void testQuery () {
		Connection x_conn = null;
		Statement  x_stmt = null;
		ResultSet  x_rset = null;
		try { 
			x_conn = odsource.getConnection ();
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
			Janitor.cleanupSession ( x_rset, x_stmt, x_conn );
		}
	}
}


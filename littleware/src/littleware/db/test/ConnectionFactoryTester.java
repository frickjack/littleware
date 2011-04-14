/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.db.test;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.sql.*;

import javax.sql.DataSource;


import littleware.db.*;
import littleware.test.LittleTest;

/**
 * Little test case for connection factory
 */
public class ConnectionFactoryTester extends LittleTest {

    private static final Logger log = Logger.getLogger(ConnectionFactoryTester.class.getName());
    private static final String TEST_QUERY = "SELECT 'hello' FROM asset";
    
    private final DataSource dsource;
    private final DataSourceHandler proxyHandler;
    

    /**
     * Constructor takes a connection factory and a test query to run
     * against a checked out connection.  The supplied query should just return 'Hello'.
     *
     * @param dsource to test against
     * @param proxyHandler to test against
     */
    @Inject
    public ConnectionFactoryTester(
            @Named( "datasource.littleware" ) DataSource dsource,
            @Named( "datasource.littleware" ) DataSourceHandler proxyHandler
            ) {
        setName( "testQuery" );
        this.dsource = dsource;
        this.proxyHandler = proxyHandler;
    }

    /**
     * Run a test query - note: only works against some databases ...
     */
    public void testQuery() {
        Connection conn = null;
        Statement stmt = null;
        ResultSet rset = null;
        try {
            conn = dsource.getConnection();
            stmt = conn.createStatement();
            rset = stmt.executeQuery(TEST_QUERY);

            assertTrue("Resultset not empty from test query: " + TEST_QUERY,
                    rset.next());
            assertTrue("Resultset.getString(1) == Hello",
                    "Hello".equalsIgnoreCase(rset.getString(1))
                    );
            log.log(Level.INFO, "Test query worked, got: {0}", rset.getString(1));
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Failed test", ex );
            assertTrue("Caught unexpected: " + ex, false);
        } finally {
            Janitor.cleanupSession(rset, stmt, conn);
        }
    }

    /**
     * Test that our injected DataSource is actually a dynamic-proxy
     * around our injected DataSourceHandler
     */
    public void testProxy() {
        // Ok - test the proxy stuff
        assertTrue( "DataSource != proxyHandler.getDataSource b/c it's a proxy!",
                proxyHandler.getDataSource() != dsource
                );
        final DataSource remember = proxyHandler.getDataSource();
        final String     rememberUrl = proxyHandler.getJdbcUrl();
        try {
            proxyHandler.setDataSource(null,"bla");
            // should get null-pointer exception via proxy DataSource now
            try {
                dsource.getConnection();
                fail( "Injected data source should be a proxy with our proxyHandler" );
            } catch( Exception ex ) {
            }
        } finally {
            proxyHandler.setDataSource( remember, rememberUrl );
        }
    }
}


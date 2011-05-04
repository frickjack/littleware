/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.test;


import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.server.db.test.DbAssetManagerTester;
import littleware.security.LittleUser;
import littleware.test.TestFactory;

/**
 * Test suite for littleware.asset package
 */
public class JenkinsTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger(JenkinsTestSuite.class.getName());

    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public JenkinsTestSuite(
            Provider<TransactionTester> provideTransTester,
            Provider<DbAssetManagerTester> provideDbTester,
            Provider<LittleUser> provideCaller,
            Provider<QuotaUtilTester> provideQuotaTester
            ) {
        super(JenkinsTestSuite.class.getName());
        boolean runTest = true;

        if (runTest) {
            this.addTest( provideDbTester.get() );
            this.addTest( provideDbTester.get().putName( "testCreateUpdateDelete" ) );
        }
        if ( false ) {
            // this test polutes the asset-type table, so only run it when necessar
            this.addTest( provideDbTester.get().putName( "testAssetTypeCheck" ) );
        }

        if (runTest) {
            this.addTest( provideTransTester.get() );
        }
        if ( false ) {
            // this test only applies for JdbcLittleTransaction db implementation
            this.addTest(provideTransTester.get().putName("testSavepoint") );
        }
        if ( runTest ) {
            this.addTest( provideQuotaTester.get() );
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    public static Test suite() {
        try {
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            return (new TestFactory()).build( serverBoot, JenkinsTestSuite.class );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }

}


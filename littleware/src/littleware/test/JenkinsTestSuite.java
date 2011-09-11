/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestSuite;
import littleware.bootstrap.AppBootstrap;

/**
 * Test suite constructor that pulls together tests from
 * every littleware.*.test package that can run automatically
 * behind a server (no swing, no user interaction)
 */
public class JenkinsTestSuite extends TestSuite {

    private static final Logger log = Logger.getLogger(JenkinsTestSuite.class.getName());

    @Inject
    public JenkinsTestSuite(
            littleware.base.test.JenkinsTestSuite baseSuite,
            littleware.lgo.test.PackageTestSuite lgoSuite,
            Provider<littleware.bootstrap.test.BootstrapTester> bootstrapProvider
        ) {
        super(JenkinsTestSuite.class.getName());
        // disable server tests
        final boolean bRun = true;

        log.log(Level.INFO, "Trying to setup littleware.test test suite");
        try {
            if (bRun) {
                this.addTest( bootstrapProvider.get() );
                this.addTest( bootstrapProvider.get().putName( "testSessionSemantics" ) );
            }
            if (bRun) {
                log.log(Level.INFO, "Trying to setup littleware.base test suite");
                this.addTest(baseSuite);
            }
            if ( bRun ) {
                log.log( Level.INFO, "Trying to setup lgo test suite" );
                this.addTest( lgoSuite );
            }

            if (false) {
                // Move this test out to littleAsset sub-project - littleAsset sets up a databse connection
                log.log(Level.INFO, "Trying to setup littleware.db test suite");
                //this.addTest( dbSuite );
            }

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Failed to setup test suite" , ex );
            throw ex;
        }
        log.log(Level.INFO, "JenkinsTestSuite.suite () returning ok ...");
    }

    /**
     * Just call through to ServerTestLauncher.suite() - should only
     * invoke when this is the master SeverTestLauncher TestSuite.
     */
    public static JenkinsTestSuite suite() {
        log.log(Level.WARNING, "Guice 2.0 has an AOP bug that may throw an exception booting in test-runner class-loader");
        try {
            return (new TestFactory()).build(
                    AppBootstrap.appProvider.get().profile(AppBootstrap.AppProfile.CliApp).build(),
                    JenkinsTestSuite.class
                    );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }
}

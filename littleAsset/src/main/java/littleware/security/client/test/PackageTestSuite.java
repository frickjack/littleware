/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.client.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;


import junit.framework.*;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.bootstrap.AppBootstrap;
import littleware.test.TestFactory;


/**
 * Just little utility class that packages up a test suite
 * for the littleware.security package.
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger log = Logger.getLogger(PackageTestSuite.class.getName());

    /**
     * Inject the managers to test against
     */
    @Inject
    public PackageTestSuite(
            Provider<AclManagerTester> provideAclTester,
            Provider<AccountManagerTester> provideAccountTester,
            ClientLoginTester testClientLogin
            ) {
        super(PackageTestSuite.class.getName());

        log.log(Level.INFO, "Trying to setup littleware.security test suite");
        log.log(Level.INFO, "Registering littleware SimpleDbLoginConfiguration");
        boolean b_run = true;

        if (b_run) {
            this.addTest(provideAclTester.get().putName("testAcl"));
        }

        if (b_run) {
            this.addTest(provideAccountTester.get().putName("testGetPrincipals"));
            //this.addTest(provideAccountTester.get().putName("testPasswordUpdate"));
            this.addTest(provideAccountTester.get().putName("testGroupUpdate"));
        }
        if (b_run) {
            this.addTest(provideAclTester.get().putName("testAclLoad"));
            //this.addTest(provideAclTester.get().putName("testAclUpdate"));
        }
        if ( b_run ) {
            this.addTest( testClientLogin );
        }

        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    /**
     * Just call through to ServerTestLauncher.suite() - should only
     * invoke when this is the master SeverTestLauncher TestSuite.
     */
    public static Test suite() {
        try {
            log.log( Level.INFO, "Launching test suite ..."  );
            return (new TestFactory()).build(
                    ServerBootstrap.provider.get().profile( AppBootstrap.AppProfile.CliApp ).build(),
                    PackageTestSuite.class
                    );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }

    public static void main( String[] args ) {
        final String[] testArgs = {
            "-noloading", PackageTestSuite.class.getName()
        };
        junit.swingui.TestRunner.main(testArgs);
    }

}



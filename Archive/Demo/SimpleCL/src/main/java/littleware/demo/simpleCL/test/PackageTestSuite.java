/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.demo.simpleCL.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestSuite;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.bootstrap.server.ServerBootstrap;
import littleware.test.TestFactory;

/**
 * Test suite for simpleCL package
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger log = Logger.getLogger(PackageTestSuite.class.getName());

    @Inject
    public PackageTestSuite(Provider<SimpleCLTester> provideSimpleCL,
            final LittleBootstrap bootstrap) {
        super(PackageTestSuite.class.getName());
        this.addTest(provideSimpleCL.get());
    }

    public static TestSuite suite() {
        try {
            final ServerBootstrap bootServer = ServerBootstrap.provider.get().build();
            final ClientBootstrap.LoginSetup bootstrap = ClientBootstrap.clientProvider.get().build();
            //return (new TestFactory()).build(bootstrap, PackageTestSuite.class );
            return (new TestFactory()).build(bootServer, bootstrap, PackageTestSuite.class);
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test bootstrap failed", ex);
            throw ex;
        }
    }

    public static void main(String[] v_args) {
        String[] v_test_args = {"-noloading", PackageTestSuite.class.getName()};
        //Test suite = suite ();
        log.log(Level.INFO, "Trying to setup test gui for: " + PackageTestSuite.class.getName());
        junit.swingui.TestRunner.main(v_test_args);
    }
}

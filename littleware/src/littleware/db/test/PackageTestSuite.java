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
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;

import junit.framework.*;
import littleware.bootstrap.server.ServerBootstrap;
import littleware.test.TestFactory;


/**
 * Just little utility class that packages up a test suite
 * for the littleware.db package.
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    @Inject
    public PackageTestSuite( Provider<ConnectionFactoryTester> provideFactoryTester ) {
        super( PackageTestSuite.class.getName() );

        this.addTest( provideFactoryTester.get() );
        this.addTest( provideFactoryTester.get().putName("testProxy"));
    }


    public static Test suite() {
        log.log(Level.WARNING, "Guice 2.0 has an AOP bug that may throw an exception booting in test-runner class-loader");
        try {
            return (new TestFactory()).build(
                    ServerBootstrap.provider.get().profile(ServerBootstrap.ServerProfile.Standalone).build(),
                    PackageTestSuite.class
                    );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }

}



/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.bootstrap.server.ServerBootstrap;
import littleware.test.TestFactory;

/**
 * Test the apps.tracker packages
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    @Inject
    public PackageTestSuite(
            Provider<QueryManagerTester> provideQueryTester,
            Provider<ProductSetupTester> provideProductTester
            ) {
        super( PackageTestSuite.class.getName() );
        this.addTest( provideQueryTester.get() );
        this.addTest( provideProductTester.get() );
    }

    public static Test suite() {
        try {
            return (new TestFactory()).build( ServerBootstrap.provider.get().build(),
                ClientBootstrap.clientProvider.get().build(),
                PackageTestSuite.class
                );
        } catch ( RuntimeException ex ) {
            log.log( Level.SEVERE, "Test setup failed", ex );
            throw ex;
        }
    }

    public static void main( String[] ignore ) {
        final String[] args = new String[] { "-noloading", PackageTestSuite.class.getName() };
        junit.swingui.TestRunner.main( args );
    }
}

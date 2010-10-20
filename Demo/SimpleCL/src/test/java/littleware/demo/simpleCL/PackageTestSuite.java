/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.demo.simpleCL;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestSuite;
import littleware.apps.client.ClientBootstrap;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.LittleBootstrap;
import littleware.security.auth.SimpleNamePasswordCallbackHandler;
import littleware.security.auth.server.ServerBootstrap;
import littleware.test.TestFactory;

/**
 * Test suite for simpleCL package
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    @Inject
    public PackageTestSuite( Provider<SimpleCLTester> provideSimpleCL,
            final LittleBootstrap bootstrap
            ) {
        super( PackageTestSuite.class.getName() );
        this.addTest( provideSimpleCL.get() );
    }

    
    public static TestSuite suite() {
        final ServerBootstrap bootServer = new ServerBootstrap( true );
        final ClientBootstrap bootstrap = new ClientBootstrap(
                new ClientServiceGuice( new SimpleNamePasswordCallbackHandler( "littleware.test_user", "bla"))
                );
        //return (new TestFactory()).build(bootstrap, PackageTestSuite.class );
        return (new TestFactory()).build( bootServer, bootstrap, PackageTestSuite.class );
    }

    public static void main( String[] v_args ) {
	String[] v_test_args = {"-noloading", PackageTestSuite.class.getName() };
        //Test suite = suite ();
        log.log ( Level.INFO, "Trying to setup test gui for: " + PackageTestSuite.class.getName () );
	junit.swingui.TestRunner.main(v_test_args);
    }

}

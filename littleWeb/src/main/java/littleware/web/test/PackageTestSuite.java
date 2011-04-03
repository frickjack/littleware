/*
 * Copyright 2007-2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.test.AssetTestFactory;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AppModuleFactory;



/**
 * Just little utility class that packages up a test suite
 * for the littleware.web package.
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    @Inject
    public PackageTestSuite ( 
            Provider<BrowserTypeTester> provideBrowserTypeTester,
            Provider<ThumbServletTester> provideThumbServTester
            ) {
        super( PackageTestSuite.class.getName() );

        log.log(Level.INFO, "Trying to setup littleware.web test suite");

        // This should get the SimpleSessionManager up and listening on the default port
        boolean runTest = true;

        if (runTest) {
            this.addTest( provideBrowserTypeTester.get() );
        }
        if ( runTest ) {
            this.addTest( provideThumbServTester.get() );
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }


    public static Test suite() {
        try {
            //final ServerModuleFactory test = ServerModuleFactory.class.cast( Class.forName( "littleware.apps.filebucket.server.BucketServerModule$Factory" ).newInstance() );
            //log.log( Level.INFO, "Instantiated test factory!" );
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            final ClientBootstrap.ClientBuilder clientBuilder = ClientBootstrap.clientProvider.get(
                    ); //.addModuleFactory( new LgoServerModule.Factory() );
            for( AppModuleFactory scan : clientBuilder.getModuleSet() ) {
                log.log( Level.INFO, "Scanning client module set: {0}", scan.getClass().getName());
            }
            return (new AssetTestFactory()).build(serverBoot,
                    clientBuilder.build(),
                    PackageTestSuite.class
                    );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        } catch ( Throwable ex ) {
            log.log(Level.SEVERE, "Test setup failed, what the frick ?", ex);
            throw new AssertionFailedException( "Bootstrap failed", ex );
        }
    }

}

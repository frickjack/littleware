/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;
import littleware.asset.client.test.AssetTestFactory;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.base.AssertionFailedException;



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
            Provider<LoginTester> provideLoginTester,
            Provider<JwtTester> provideJwtTester,
            Provider<AssetTester> provideAssetTester
            ) {
        super( PackageTestSuite.class.getName() );

        log.log(Level.INFO, "Trying to setup littleware.web test suite");

        // This should get the SimpleSessionManager up and listening on the default port
        boolean runTest = true;

        if ( runTest ) {
            this.addTest( provideLoginTester.get() );
        }
        if ( runTest ) {
            this.addTest( provideJwtTester.get() );
        }        
        if ( runTest ) {
            this.addTest( provideAssetTester.get() );
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }


    public static Test suite() {
        try {
            //final ServerModuleFactory test = ServerModuleFactory.class.cast( Class.forName( "littleware.apps.filebucket.server.BucketServerModule$Factory" ).newInstance() );
            //log.log( Level.INFO, "Instantiated test factory!" );
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            return (new AssetTestFactory()).build(serverBoot,
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

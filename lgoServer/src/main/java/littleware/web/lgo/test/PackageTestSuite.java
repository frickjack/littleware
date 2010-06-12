/*
 * Copyright 2007-2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.lgo.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.bootstrap.client.ClientModuleFactory;
import littleware.bootstrap.server.ServerBootstrap;
import littleware.bootstrap.server.ServerModuleFactory;
import littleware.test.TestFactory;



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
    public PackageTestSuite ( Provider<LgoServerTester> provideLgoServerTester,
            Provider<LgoServletTester> provideLgoServletTester
            ) {
        super( PackageTestSuite.class.getName() );

        log.log(Level.INFO, "Trying to setup littleware.web test suite");

        // This should get the SimpleSessionManager up and listening on the default port
        boolean b_run = true;

        if (b_run) {
            this.addTest(provideLgoServerTester.get());
        }
        if (b_run) {
            this.addTest( provideLgoServletTester.get() );
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }


    public static Test suite() {
        try {
            final ServerModuleFactory test = ServerModuleFactory.class.cast( Class.forName( "littleware.apps.filebucket.server.BucketServerModule$Factory" ).newInstance() );
            log.log( Level.INFO, "Instantiated test factory!" );
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            final ClientBootstrap.ClientBuilder clientBuilder = ClientBootstrap.clientProvider.get(
                    ); //.addModuleFactory( new LgoServerModule.Factory() );
            for( ClientModuleFactory scan : clientBuilder.getModuleSet() ) {
                log.log( Level.INFO, "Scanning client module set: " + scan.getClass().getName() );
            }
            return (new TestFactory()).build(serverBoot,
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

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
import littleware.apps.client.ClientBootstrap;
import littleware.apps.filebucket.server.BucketServerActivator;
import littleware.apps.filebucket.server.BucketServerGuice;
import littleware.apps.tracker.client.TrackerClientGuice;
import littleware.apps.tracker.server.TrackerServerActivator;
import littleware.apps.tracker.server.TrackerServerGuice;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.GuiceOSGiBootstrap;
import littleware.security.auth.SimpleNamePasswordCallbackHandler;
import littleware.security.auth.server.ServerBootstrap;
import littleware.test.TestFactory;

/**
 * Test the apps.tracker packages
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    @Inject
    public PackageTestSuite( Provider<QueryManagerTester> provideQueryTester ) {
        super( PackageTestSuite.class.getName() );
        this.addTest( provideQueryTester.get() );
    }

    public static Test suite() {
        try {
            final GuiceOSGiBootstrap serverBoot = new ServerBootstrap( true );
            serverBoot.getGuiceModule().add( new TrackerServerGuice() );
            serverBoot.getOSGiActivator().add( TrackerServerActivator.class );
            serverBoot.getGuiceModule().add( new BucketServerGuice() );
            serverBoot.getOSGiActivator().add( BucketServerActivator.class );

            final GuiceOSGiBootstrap clientBoot = new ClientBootstrap( new ClientServiceGuice( new SimpleNamePasswordCallbackHandler( "littleware.test_user", "bla" )));
            clientBoot.getGuiceModule().add( new TrackerClientGuice() );
            return (new TestFactory()).build( serverBoot,
                clientBoot,
                PackageTestSuite.class
                );
        } catch ( RuntimeException ex ) {
            log.log( Level.SEVERE, "Test setup failed", ex );
            throw ex;
        }

    }
}

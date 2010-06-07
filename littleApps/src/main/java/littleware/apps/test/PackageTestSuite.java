/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;
import littleware.apps.client.ClientSyncModule;

import littleware.apps.filebucket.server.BucketServerModule;
import littleware.apps.filebucket.server.BucketServerGuice;
import littleware.apps.misc.test.ThumbManagerTester;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.GuiceOSGiBootstrap;
import littleware.security.auth.SimpleNamePasswordCallbackHandler;
import littleware.security.auth.server.ServerBootstrap;
import littleware.test.TestFactory;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger(PackageTestSuite.class.getName());

    /** Inject server-connected sessionHelper */
    @Inject
    public PackageTestSuite(
            HudsonTestSuite      hudsonSuite,
            Provider<SwingClientTester> provide_swing_test,
            //Provider<ThumbManagerTester> provide_thumb_test,
            Provider<JAssetFamilyTester> provideFamilyTest,
            Provider<SwingFeedbackTester> provideFeedbackTest,
            Provider<JDeleteAssetTester> provideDeleteTest
            )
    {
        super(PackageTestSuite.class.getName());

        boolean b_run = true;
        
        if ( b_run ) {
            this.addTest( hudsonSuite );
        }

        if ( b_run ) {
            this.addTest( provideDeleteTest.get() );
        }
        if (b_run) {
            this.addTest( provideFeedbackTest.get() );
        }

        if (b_run) {
            this.addTest(provide_swing_test.get().putName("testClientSession"));
        }

        /*..
        if (b_run) {
            this.addTest(provide_thumb_test.get());
        } ..*/

        if (b_run) {
            this.addTest(provide_swing_test.get().putName("testJAssetViews"));
        }
        if (b_run) {
            this.addTest(provide_swing_test.get().putName("testJAssetBrowser"));
        }

        if (b_run) {
            this.addTest(provideFamilyTest.get());
        }
        if (b_run) {
            this.addTest(provide_swing_test.get().putName("testJEditor"));
        }
        if (b_run) {
            this.addTest(provide_swing_test.get().putName("testWizardCreate"));
        }

        log.log(Level.INFO, "PackageTestSuite() ok ...");
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        try {
            final GuiceOSGiBootstrap serverBoot = new ServerBootstrap( true );
            serverBoot.getGuiceModule().add( new BucketServerGuice() );
            serverBoot.getOSGiActivator().add( BucketServerModule.class );
            return (new TestFactory()).build( serverBoot,
                new ClientSyncModule( new ClientServiceGuice( new SimpleNamePasswordCallbackHandler( "littleware.test_user", "bla" ))),
                PackageTestSuite.class
                );
        } catch ( RuntimeException ex ) {
            log.log( Level.SEVERE, "Test setup failed", ex );
            throw ex;
        }
    }

}

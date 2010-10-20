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
import littleware.bootstrap.client.AppModuleFactory;

import littleware.bootstrap.client.ClientBootstrap;
import littleware.bootstrap.server.ServerBootstrap;
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

    public static Test suite() {
        try {
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            final ClientBootstrap.ClientBuilder clientBuilder = ClientBootstrap.clientProvider.get();
            for( AppModuleFactory scan : clientBuilder.getModuleSet() ) {
                log.log( Level.INFO, "Scanning client module set: {0}", scan.getClass().getName());
            }
            return (new TestFactory()).build(serverBoot,
                    clientBuilder.build(),
                    PackageTestSuite.class
                    );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }

}

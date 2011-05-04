/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.client.test.AssetTestFactory;
import littleware.bootstrap.AppBootstrap.AppProfile;

/**
 * Limited test suite for Hudson-server to run.
 * Does not test Swing widgets.
 */
public class JenkinsTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger(JenkinsTestSuite.class.getName());

    /** Inject server-connected sessionHelper */
    @Inject
    public JenkinsTestSuite(
            Provider<AssetModelLibTester> provide_model_test,
            Provider<BucketTester> provide_bucket_test,
            littleware.apps.image.test.PackageTestSuite miscSuite,
            littleware.apps.lgo.test.HudsonTestSuite lgoSuite,
            littleware.asset.test.JenkinsTestSuite assetSuite,
            littleware.security.client.test.PackageTestSuite securitySuite
            ) {
        super(JenkinsTestSuite.class.getName());

        boolean runTest = true;

        if ( runTest ) {
            this.addTest( assetSuite );
        }
        if ( runTest ) {
            this.addTest( securitySuite );
        }
        if (runTest) {
            this.addTest(lgoSuite);
        }
        if (runTest) {
            this.addTest( miscSuite );
        }

        if (runTest) {
            this.addTest(provide_model_test.get());
        }
        if (runTest) {
            this.addTest(provide_model_test.get().putName("testSessionHookup"));
        }

        if (runTest) {
            this.addTest(provide_bucket_test.get());
        }

        log.log(Level.INFO, "HudsonTestSuite() ok ...");
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        try {
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            return (new AssetTestFactory()).build(serverBoot,
                    ClientBootstrap.clientProvider.get().profile(AppProfile.CliApp).build(),
                    JenkinsTestSuite.class
                    );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }
}

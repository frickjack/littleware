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
import littleware.apps.client.ClientSyncModule;
import littleware.apps.filebucket.server.BucketServerModule;
import littleware.apps.filebucket.server.BucketServerGuice;
import littleware.apps.image.test.ImageManagerTester;
import littleware.security.auth.ClientServiceGuice;
import littleware.security.auth.GuiceOSGiBootstrap;
import littleware.security.auth.SimpleNamePasswordCallbackHandler;
import littleware.security.auth.server.ServerBootstrap;
import littleware.test.TestFactory;

/**
 * Limited test suite for Hudson-server to run.
 * Does not test Swing widgets.
 */
public class HudsonTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger(HudsonTestSuite.class.getName());

    /** Inject server-connected sessionHelper */
    @Inject
    public HudsonTestSuite(
            Provider<AssetModelLibTester> provide_model_test,
            Provider<BucketTester> provide_bucket_test,
            littleware.apps.image.test.PackageTestSuite miscSuite,
            littleware.apps.lgo.test.HudsonTestSuite lgoSuite,
            littleware.web.test.PackageTestSuite webSuite) {
        super(HudsonTestSuite.class.getName());

        boolean b_run = true;

        if (b_run) {
            this.addTest(lgoSuite);
        }
        if (b_run) {
            this.addTest(webSuite);
        }

        if (b_run) {
            this.addTest( miscSuite );
        }

        if (b_run) {
            this.addTest(provide_model_test.get());
        }
        if (b_run) {
            this.addTest(provide_model_test.get().putName("testSessionHookup"));
        }

        if (b_run) {
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
            final GuiceOSGiBootstrap serverBoot = new ServerBootstrap(true);
            serverBoot.getGuiceModule().add(new BucketServerGuice());
            serverBoot.getOSGiActivator().add(BucketServerModule.class);
            return (new TestFactory()).build(serverBoot,
                    new ClientSyncModule(new ClientServiceGuice(new SimpleNamePasswordCallbackHandler("littleware.test_user", "bla"))),
                    HudsonTestSuite.class);
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }
}

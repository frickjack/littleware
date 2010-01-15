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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.apps.client.ClientBootstrap;
import littleware.apps.filebucket.server.BucketServerActivator;
import littleware.apps.filebucket.server.BucketServerGuice;
import littleware.apps.misc.test.ImageManagerTester;
import littleware.base.AssertionFailedException;
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
            Provider<ImageManagerTester> provide_image_test,
            littleware.apps.lgo.test.HudsonTestSuite suiteLgo
            )
    {
        super(HudsonTestSuite.class.getName());

        boolean b_run = true;

        if (b_run) {
            this.addTest(suiteLgo);
        }

        if (b_run) {
            this.addTest( provide_image_test.get() );
        }

        if (b_run) {
            this.addTest(provide_model_test.get());
        }
        if (b_run) {
            this.addTest(provide_model_test.get().putName("testSessionHookup"));
        }

        try {
            if (b_run) {
                TestCase test = provide_bucket_test.get();
                test.setName("testBucket");
                this.addTest(test);
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed to get started");
        }

        log.log(Level.INFO, "HudsonTestSuite() ok ...");
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        try {
            final GuiceOSGiBootstrap serverBoot = new ServerBootstrap( true );
            serverBoot.getGuiceModule().add( new BucketServerGuice() );
            serverBoot.getOSGiActivator().add( BucketServerActivator.class );
            return (new TestFactory()).build( serverBoot,
                new ClientBootstrap( new ClientServiceGuice( new SimpleNamePasswordCallbackHandler( "littleware.test_user", "bla" ))),
                HudsonTestSuite.class
                );
        } catch ( RuntimeException ex ) {
            log.log( Level.SEVERE, "Test setup failed", ex );
            throw ex;
        }
    }
}
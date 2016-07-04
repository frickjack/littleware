/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import littleware.asset.client.AssetSearchManagerTester;
import littleware.asset.client.AssetTreeToolTester;
import littleware.asset.client.AssetTestFactory;
import littleware.asset.client.AssetPathTester;
import littleware.asset.client.AssetManagerTester;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.base.AssertionFailedException;

/**
 * Test suite for littleware.asset package
 */
public class JenkinsTestSuite extends TestSuite {

    private static final Logger log = Logger.getLogger(JenkinsTestSuite.class.getName());

    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public JenkinsTestSuite(
            Provider<AssetPathTester> providePathTester,
            Provider<AssetTreeToolTester> provideTreeTester,
            Provider<AssetSearchManagerTester> provideSearchTest,
            Provider<AssetManagerTester> provideAstMgrTest,
            littleware.asset.test.JenkinsTestSuite assetTestSuite,
            littleware.security.client.test.PackageTestSuite securityTestSuite,
            Provider<RestClientTester> restTestFactory) {
        super(JenkinsTestSuite.class.getName());
        boolean runTest = true;

        if (runTest) {
            this.addTest(assetTestSuite);
            this.addTest(securityTestSuite);
        }
        if (runTest) {
            this.addTest(provideAstMgrTest.get());
        }
        if (runTest) {
            this.addTest(provideSearchTest.get().putName("testLoad"));
            this.addTest(provideSearchTest.get());
            this.addTest(provideSearchTest.get().putName("testTransactionLog"));
        }

        if (runTest) {
            this.addTest(provideTreeTester.get());
        }
        if (runTest) {
            this.addTest(providePathTester.get());
            this.addTest(providePathTester.get().putName("testBadLookup"));
        }
        if (runTest) {
            this.addTest(restTestFactory.get());
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    public static TestSuite suite() {
        try {
            return (new AssetTestFactory()).build(ServerBootstrap.provider.get().build(), JenkinsTestSuite.class);
        } catch (Throwable ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw new AssertionFailedException("Failed bootstrap", ex);
        }
    }
}

/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;
import littleware.asset.GenericAsset;



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
            Provider<AssetManagerTester> provideAMgrTest,
            littleware.asset.test.JenkinsTestSuite assetTestSuite
            ) {
        super(JenkinsTestSuite.class.getName());
        boolean runTest = true;

        this.addTest( assetTestSuite );
        if ( runTest ) {
            this.addTest( provideAMgrTest.get() );
        }
        if (runTest) {
            this.addTest( provideSearchTest.get().putName( "testLoad"));
            this.addTest( provideSearchTest.get() );
            this.addTest( provideSearchTest.get().putName( "testTransactionLog"));
        }

        if ( runTest ) {
            this.addTest( provideTreeTester.get() );
        }
        if (runTest) {
            this.addTest(providePathTester.get() );
            this.addTest(providePathTester.get().putName( "testBadLookup") );
        }

        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
}


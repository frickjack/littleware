/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.asset.pickle.PickleMaker;
import littleware.asset.pickle.XmlPicklerProvider;


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
            Provider<AssetRetrieverTester> provideRetrieverTest,
            Provider<AssetPathTester> providePathTester,
            Provider<AssetTreeToolTester> provideTreeTester,
            Provider<AssetSearchManagerTester> provideSearchTest,
            HumanPicklerProvider  humanPickler,
            XmlPicklerProvider    xmlPickler
            ) {
        super(JenkinsTestSuite.class.getName());
        boolean runTest = true;

        if ( runTest ) {
            this.addTest( provideTreeTester.get() );
        }
        if (runTest) {
            this.addTest(new PickleTester( humanPickler ));
            this.addTest(new PickleTester( xmlPickler ) );
        }

        if (runTest) {
            this.addTest(provideRetrieverTest.get());
            this.addTest(provideRetrieverTest.get().putName("testAssetType") );
        }
        if (runTest) {
            this.addTest( provideSearchTest.get() );
            this.addTest( provideSearchTest.get().putName( "testTransactionLog"));
        }
        if (runTest) {
            this.addTest(providePathTester.get() );
            this.addTest(providePathTester.get().putName( "testBadLookup") );
        }

        if (runTest) {
            this.addTest(new XmlAssetTester("testXmlAsset"));
        }

        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
}


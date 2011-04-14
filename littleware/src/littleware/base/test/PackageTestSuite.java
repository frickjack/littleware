/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.base.stat.test.SamplerTester;

/**
 * Specialization of JenkinsTestSuite includes swing UI tests
 * that require user interaction
 */
public class PackageTestSuite extends JenkinsTestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );
    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    @Inject
    public PackageTestSuite( Provider<WhateverTester> provideWhatever,
            Provider<FbIteratorTester> provideFbTester,
            Provider<PropLoaderTester> providePropTester,
            Provider<CacheTester> provideCacheTester,
            NullFeedbackTester nullFbTester,
            SamplerTester samplerTester,
            Provider<SwingTester> swingTesterProvider,
            Provider<SwingFeedbackTester> swingFeedbackTestProvider
            ) {
        super( provideWhatever, provideFbTester, providePropTester, provideCacheTester, nullFbTester, samplerTester );
        setName( getClass().getName() );
        boolean runTest = true;

        if (runTest) {
            // These tests require UI access - won't run under Hudson
            this.addTest( swingTesterProvider.get().putName("testJTextAppender"));
            this.addTest( swingTesterProvider.get().putName("testListModelIterator"));
            this.addTest( swingFeedbackTestProvider.get() );
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

}



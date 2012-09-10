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
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestSuite;
import littleware.base.UUIDFactory;
import littleware.base.stat.test.SamplerTester;

/**
 * Just little utility class that packages up a test suite
 * for the littleware.base package.
 */
public class JenkinsTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( JenkinsTestSuite.class.getName() );
    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    @Inject
    public JenkinsTestSuite( Provider<WhateverTester> provideWhatever,
            Provider<FbIteratorTester> provideFbTester,
            Provider<PropLoaderTester> providePropTester,
            Provider<CacheTester> provideCacheTester,
            NullFeedbackTester nullFbTester,
            SamplerTester samplerTester,
            ZipUtilTester zipTester
            ) {
        super( PackageTestSuite.class.getName() );

        boolean runTests = true;

        if ( runTests ) {
            this.addTest( nullFbTester );
        }
        if ( runTests ) {
            this.addTest( providePropTester.get() );
        }
        if ( runTests ) {
            this.addTest( provideWhatever.get() );
        }
        if ( runTests ) {
            this.addTest( new XmlResourceBundleTester( "testBasicXmlBundle" ) );
        }
        if (runTests) {
            this.addTest( provideCacheTester.get().putName("testGeneric") );
            this.addTest( provideCacheTester.get().putName("testAgeOut") );
            this.addTest( provideCacheTester.get().putName("testSizeLimit") );
        }
        if (runTests) {
            this.addTest(new UUIDFactoryTester("testFactory", UUIDFactory.getFactory()));
            this.addTest(new DynamicEnumTester("testEnum"));
            this.addTest(new XmlSpecialTester("testEncodeDecode"));
            this.addTest(samplerTester);
        }
        if ( runTests ) {
            this.addTest( provideFbTester.get() );
        }
        if ( runTests ) {
            this.addTest( zipTester );
        }
        log.log(Level.INFO, "JenkinsTestSuite.suite () returning ok ...");
    }

}



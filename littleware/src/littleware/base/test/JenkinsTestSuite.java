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

import junit.framework.*;

import littleware.base.*;
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
            SamplerTester samplerTester
            ) {
        super( PackageTestSuite.class.getName() );

        boolean b_run = true;

        if ( b_run ) {
            this.addTest( nullFbTester );
        }
        if ( b_run ) {
            this.addTest( providePropTester.get() );
        }
        if ( b_run ) {
            this.addTest( provideWhatever.get() );
        }
        if ( b_run ) {
            this.addTest( new XmlResourceBundleTester( "testBasicXmlBundle" ) );
        }
        if (b_run) {
            this.addTest( provideCacheTester.get().putName("testGeneric") );
            this.addTest( provideCacheTester.get().putName("testAgeOut") );
            this.addTest( provideCacheTester.get().putName("testSizeLimit") );
        }
        if (b_run) {
            this.addTest(new UUIDFactoryTester("testFactory", UUIDFactory.getFactory()));
            this.addTest(new DynamicEnumTester("testEnum"));
            this.addTest(new XmlSpecialTester("testEncodeDecode"));
            this.addTest(samplerTester);
        }
        if ( b_run ) {
            this.addTest( provideFbTester.get() );
        }
        log.log(Level.INFO, "JenkinsTestSuite.suite () returning ok ...");
    }

}



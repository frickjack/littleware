/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.*;

/**
 * Just little utility class that packages up a test suite
 * for the littleware.base package.
 */
public class PackageTestSuite extends TestSuite {
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
            NullFeedbackTester nullFbTester
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
            this.addTest(littleware.base.stat.test.PackageTestSuite.suite());
        }
        if ( b_run ) {
            this.addTest( provideFbTester.get() );
        }
        if (false) {
            // These tests require UI access - won't run under Hudson
            this.addTest(new SwingTester("testJTextAppender"));
            this.addTest(new SwingTester("testListModelIterator"));
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

}



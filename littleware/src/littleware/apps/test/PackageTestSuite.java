/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
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
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.apps.lgo.test.XmlLgoHelpTester;
import littleware.base.AssertionFailedException;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger olog = Logger.getLogger( PackageTestSuite.class.getName() );


    /** Inject server-connected sessionHelper */
    @Inject
    public PackageTestSuite( Provider<AddressBookTester> provide_address_test,
            Provider<SwingClientTester> provide_swing_test,
            Provider<BucketTester> provide_bucket_test,
            Provider<TrackerTester> provide_tracker_test
            ) {
        super( PackageTestSuite.class.getName() );

        /*..
        Injector     injector;
        try {
            injector = Guice.createInjector(
                new ClientServiceGuice( m_helper ),
                new StandardClientGuice(),
                new StandardSwingGuice(),
                new PropertiesGuice( PropertiesLoader.get().loadProperties() )
                );
        } catch ( IOException ex ) {
            olog.log( Level.SEVERE, "Failed to load littleware properties", ex );
            throw new AssertionFailedException( "Failed to load littleware properties", ex );
        }
         */
        final boolean b_run = true;

        if (b_run) {
            TestCase test = new XmlLgoHelpTester();
            this.addTest( test );
        }

        if (b_run) {
            TestCase test = provide_address_test.get ();
            test.setName( "testAddressBook" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = provide_swing_test.get();
            test.setName( "testJSessionManager" );
            this.addTest( test );
            test = provide_swing_test.get();
            test.setName( "testJSessionHelper" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = provide_swing_test.get();
            test.setName( "testJAssetViews" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = provide_swing_test.get();
            test.setName( "testJAssetBrowser" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = provide_swing_test.get();
            test.setName( "testGroupFolderTool" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = provide_swing_test.get();
            test.setName( "testAssetModelLibrary" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = provide_swing_test.get();
            test.setName( "testJEditor" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = provide_swing_test.get();
            test.setName( "testWizardCreate" );
            this.addTest( test );
        }

        try {
            if (b_run) {
                TestCase test = provide_bucket_test.get();
                test.setName( "testBucket" );
                this.addTest( test );
            }
            if (b_run) {
                TestCase test = provide_tracker_test.get();
                test.setName( "testTracker" );
                this.addTest( test );
            }
            if (b_run) {
                TestCase test = provide_tracker_test.get();
                test.setName( "testTrackerSwing" );
                this.addTest( test );
            }
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed to get started");
        }

        olog.log(Level.INFO, "PackageTestSuite() returning ok ...");
    }
}

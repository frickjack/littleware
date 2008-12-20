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

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.io.IOException;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.apps.client.*;
import littleware.apps.swingclient.*;
import littleware.base.AssertionFailedException;
import littleware.base.PropertiesGuice;
import littleware.base.PropertiesLoader;
import littleware.security.auth.*;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite extends TestSuite {

    private static Logger olog = Logger.getLogger("littleware.apps.test.PackageTestSuite");
    private static SessionHelper om_helper = null;

    /**
     * Stash a test login session, so each test does not
     * have to setup a separate session.
     * Tests can access this in setUp routine.
     */
    static SessionHelper getTestSessionHelper() {
        if (null == om_helper) {
            try {
                // Force RMI
                SessionUtil util = SessionUtil.get ();
                SessionManager m_session = util.getSessionManager( util.getRegistryHost (), util.getRegistryPort () );
                String s_test_user = "littleware.test_user";
                String s_test_password = "test123";

                om_helper = m_session.login(s_test_user, s_test_password,
                        "setup test session for SwingClientTester");
            } catch (RuntimeException e) {
                throw e;
            } catch (Exception e) {
                throw new AssertionFailedException("Failure setting up test session, caught: " + e, e);
            }
        }
        return om_helper;
    }

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    public static Test suite() {
        return new PackageTestSuite();
    }

    /**
     * Self-register all the JUnit tests to do a littleware.apps
     * regression test.
     */
    public PackageTestSuite() {
        super("littleware.apps.test.PackageTestSuite");

        final SessionHelper m_helper = PackageTestSuite.getTestSessionHelper();
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
        boolean b_run = true;

        // quick check
        IconLibrary icolib = injector.getInstance( IconLibrary.class );
        if ( ! icolib.getRoot().equals( "littleware/apps/icons" ) ) {
            throw new AssertionFailedException( "Bad icon root: " + icolib.getRoot() );
        }
    
        if (b_run) {
            TestCase test = injector.getInstance( AddressBookTester.class );
            test.setName( "testAddressBook" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = injector.getInstance( SwingClientTester.class );
            test.setName( "testJSessionManager" );
            this.addTest( test );
            test = injector.getInstance( SwingClientTester.class );
            test.setName( "testJSessionHelper" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = injector.getInstance( SwingClientTester.class );
            test.setName( "testJAssetViews" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = injector.getInstance( SwingClientTester.class );
            test.setName( "testJAssetBrowser" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = injector.getInstance( SwingClientTester.class );
            test.setName( "testGroupFolderTool" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = injector.getInstance( SwingClientTester.class );
            test.setName( "testAssetModelLibrary" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = injector.getInstance( SwingClientTester.class );
            test.setName( "testJEditor" );
            this.addTest( test );
        }
        if (b_run) {
            TestCase test = injector.getInstance( SwingClientTester.class );
            test.setName( "testWizardCreate" );
            this.addTest( test );
        }

        try {
            if (b_run) {
                TestCase test = injector.getInstance( BucketTester.class );
                test.setName( "testBucket" );
                this.addTest( test );
            }
            if (b_run) {
                TestCase test = injector.getInstance( TrackerTester.class );
                test.setName( "testTracker" );
                this.addTest( test );
            }
            if (b_run) {
                TestCase test = injector.getInstance( TrackerTester.class );
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

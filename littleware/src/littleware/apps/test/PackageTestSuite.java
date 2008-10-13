package littleware.apps.test;

import com.google.inject.Guice;
import com.google.inject.Injector;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.apps.client.*;
import littleware.apps.swingclient.*;
import littleware.base.AssertionFailedException;
import littleware.security.auth.*;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite extends TestSuite {

    private static Logger olog_generic = Logger.getLogger("littleware.apps.test.PackageTestSuite");
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

        try {
            StandardSwingGuice.getIconLibrary ().setRoot( "localhost/littleware/lib/icons" );
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            throw new AssertionFailedException("Failed to initialize IconLibrary", e);
        }

        final SessionHelper m_helper = PackageTestSuite.getTestSessionHelper();
        final Injector     injector = Guice.createInjector(
                new ClientServiceGuice( m_helper ),
                new StandardClientGuice(),
                new StandardSwingGuice() 
                );

        boolean b_run = true;

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

        olog_generic.log(Level.INFO, "PackageTestSuite() returning ok ...");
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.web.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.web.*;
import littleware.security.auth.server.SimpleSessionManager;
import littleware.security.auth.SessionManager;
import littleware.security.server.SecurityResourceBundle;
import littleware.web.pickle.*;


/**
 * Just little utility class that packages up a test suite
 * for the littleware.web package.
 */
public abstract class PackageTestSuite {	

    /**
	 * Setup a test suite to exercise this package -
	 * junit.swingui.TestRunner looks for this.
	 */
    public static Test suite () {
        TestSuite test_suite = new TestSuite ( "littleware.web.test.PackageTestSuite" );
		Logger   log_generic = Logger.getLogger ( "littleware.web.test" );
		log_generic.log ( Level.INFO, "Trying to setup littleware.web test suite" );
		
		// This should get the SimpleSessionManager up and listening on the default port
		SessionManager m_session = (SessionManager) SecurityResourceBundle.getBundle ().getObject ( SecurityResourceBundle.Content.SessionManager );
		boolean        b_run = true;

		if ( b_run ) {
			test_suite.addTest ( new BeanTester ( "testSessionBean" ) );
		}
        if ( b_run ) {
			test_suite.addTest ( new BeanTester ( "testDefaultsBean" ) );
		}
		if ( b_run ) {
			test_suite.addTest ( new BeanTester ( "testNewUserBean" ) );
			test_suite.addTest ( new BeanTester ( "testNewUserEmail" ) );
		}
		if ( b_run ) {
			test_suite.addTest ( new BeanTester ( "testBasicSession" ) );
		}
		if ( b_run ) {
			test_suite.addTest ( new PickleTester ( "testPickleTwice", PickleType.XML ) );
		}
		if ( b_run ) {
			test_suite.addTest ( new BrowserTypeTester ( "testUserAgent" ) );
		}
			
		log_generic.log ( Level.INFO, "PackageTestSuite.suite () returning ok ..." );
        return test_suite;
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


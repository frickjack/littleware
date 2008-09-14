package littleware.security.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.server.AssetResourceBundle;
import littleware.base.AssertionFailedException;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.security.server.SecurityResourceBundle;



/**
 * Just little utility class that packages up a test suite
 * for the littleware.security package.
 */
public abstract class PackageTestSuite {	
	/**
	 * Little utility that invokes a PriviledgedAction to
	 * access the specified GuardedObject in the specified
	 * resource bundle
	 *
	 * @param s_resource_bundle to lookup
	 * @param s_resource that corresponds to a GuardedObject to access
	 * @return whatever the GuardedObject is guarding - invoke Guard
	 *                within a local PriviledgedAction
	 */
	private static Object getGuardedResource ( String s_resource_bundle, String s_resource ) {
		return AccessController.doPrivileged ( new GetGuardedResourceAction ( s_resource_bundle, s_resource ) );
	}
	
	
    /**
	 * Setup a test suite to exercise this package -
	 * junit.swingui.TestRunner looks for this.
	 */
    public static Test suite () {
        TestSuite test_suite = new TestSuite ( "littleware.security.test.PackageTestSuite" );
		Logger   log_generic = Logger.getLogger ( "littleware.security.test" );
		log_generic.log ( Level.INFO, "Trying to setup littleware.security test suite" );
		
		log_generic.log ( Level.INFO, "Registering littleware SimpleDbLoginConfiguration" );
		// - set at app startup via system property: 
		//    javax.security.auth.login.Configuration.setConfiguration ( new SimpleDbLoginConfiguration () );
				
		// This should get the SimpleSessionManager up and listening on the default port
		SessionManager m_session = (SessionManager) SecurityResourceBundle.getBundle ().getObject ( SecurityResourceBundle.Content.SessionManager );
        AssetSearchManager  m_search = (AssetSearchManager) AssetResourceBundle.getBundle ().getObject ( AssetResourceBundle.Content.AssetSearcher );
		boolean        b_run = true;
		
		if ( b_run ) {
			test_suite.addTest ( new LoginTester ( "testLogin", LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, m_session
												   ) 
								 );
            test_suite.addTest ( new LoginTester ( "testClientModuleLogin", LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, m_session
												   ) 
								 );            
		}
		if ( b_run ) {
			test_suite.addTest ( new LoginTester ( "testSessionSetup",
												   LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, 
												   //LoginTester.OS_TEST_USER, 
												   //LoginTester.OS_TEST_USER_PASSWORD,
												   m_session
												   ) 
								 );
		}
		if ( b_run ) {
			test_suite.addTest ( new LoginTester ( "testSessionUtil", 
												   LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, 
												   m_session
												   )
								 );
		}
		
		SecurityResourceBundle   bundle_security = SecurityResourceBundle.getBundle ();
		AccountManager   m_account = (AccountManager) bundle_security.getObject ( SecurityResourceBundle.Content.AccountManager );
					
		if ( b_run ) {
			try {
				Principal p_administrator = m_account.getPrincipal ( AccountManager.LITTLEWARE_ADMIN );

				test_suite.addTest ( new AclTester ( "testAcl", new SimpleAccessList (), p_administrator, m_search ) );
				test_suite.addTest ( new AclTester ( "testAclOwner", new SimpleAccessList (), p_administrator, m_search ) );
			} catch ( Exception e ) {
				throw new AssertionFailedException ( "Caught unexpected during test initialization: " + e, e );
			}
		}			
		
		AssetManager m_asset = AssetResourceBundle.getAssetManager ();
		
		if ( b_run ) {
			test_suite.addTest ( new AccountManagerTester ( "testGetPrincipals", m_account, m_asset ) );
			test_suite.addTest ( new AccountManagerTester ( "testQuota", m_account, m_asset ) );
			test_suite.addTest ( new AccountManagerTester ( "testPasswordUpdate", m_account, m_asset ) );
			test_suite.addTest ( new AccountManagerTester ( "testGroupUpdate", m_account, m_asset ) );
		}
		if ( b_run ) {
			AclManager  m_acl = (AclManager) bundle_security.getObject ( SecurityResourceBundle.Content.AclManager );
			test_suite.addTest ( new AclManagerTester ( "testAclLoad", m_acl, m_account, m_asset ) );
			test_suite.addTest ( new AclManagerTester ( "testAclUpdate", m_acl, m_account, m_asset ) );
		}
		
		log_generic.log ( Level.INFO, "PackageTestSuite.suite () returning ok ..." );
        return test_suite;
    }
	
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


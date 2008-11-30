package littleware.security.test;

import com.google.inject.Inject;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.base.AssertionFailedException;
import littleware.security.*;
import littleware.security.auth.*;



/**
 * Just little utility class that packages up a test suite
 * for the littleware.security package.
 */
public class PackageTestSuite {
    private static final Logger   olog = Logger.getLogger ( PackageTestSuite.class.getName() );

    private final AssetManager       om_asset;
	private final AssetSearchManager om_search;
    private final SessionManager     om_session;
    private final AccountManager     om_account;
    private final AclManager         om_acl;

    /**
     * Inject the managers to test against
     *
     * @param m_session
     * @param m_search
     * @param m_account
     * @param m_acl
     */
    @Inject
    public PackageTestSuite( SessionManager m_session,
            AssetSearchManager m_search,
            AssetManager m_asset,
            AccountManager m_account,
            AclManager m_acl
            )
    {
        om_asset = m_asset;
        om_session = m_session;
        om_search = m_search;
        om_account = m_account;
        om_acl = m_acl;
    }


    /**
	 * Setup a test suite to exercise this package -
	 * junit.swingui.TestRunner looks for this.
	 */
    public TestSuite buildSuite () {
        TestSuite test_suite = new TestSuite ( "littleware.security.test.PackageTestSuite" );
		olog.log ( Level.INFO, "Trying to setup littleware.security test suite" );
		
		olog.log ( Level.INFO, "Registering littleware SimpleDbLoginConfiguration" );
		boolean        b_run = true;
		
		if ( b_run ) {
			test_suite.addTest ( new LoginTester ( "testLogin", LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, om_session
												   ) 
								 );
            test_suite.addTest ( new LoginTester ( "testClientModuleLogin", LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, om_session
												   ) 
								 );            
		}
		if ( b_run ) {
			test_suite.addTest ( new LoginTester ( "testSessionSetup",
												   LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, 
												   //LoginTester.OS_TEST_USER, 
												   //LoginTester.OS_TEST_USER_PASSWORD,
												   om_session
												   ) 
								 );
		}
		if ( b_run ) {
			test_suite.addTest ( new LoginTester ( "testSessionUtil", 
												   LoginTester.OS_TEST_USER, 
												   LoginTester.OS_TEST_USER_PASSWORD, 
												   om_session
												   )
								 );
		}
		
		if ( b_run ) {
			try {
				Principal p_administrator = om_account.getPrincipal ( AccountManager.LITTLEWARE_ADMIN );

				test_suite.addTest ( new AclTester ( "testAcl", new SimpleAccessList (), p_administrator, om_search ) );
				test_suite.addTest ( new AclTester ( "testAclOwner", new SimpleAccessList (), p_administrator, om_search ) );
			} catch ( Exception e ) {
				throw new AssertionFailedException ( "Caught unexpected during test initialization: " + e, e );
			}
		}			
		
		if ( b_run ) {
			test_suite.addTest ( new AccountManagerTester ( "testGetPrincipals", om_account, om_asset ) );
			test_suite.addTest ( new AccountManagerTester ( "testQuota", om_account, om_asset ) );
			test_suite.addTest ( new AccountManagerTester ( "testPasswordUpdate", om_account, om_asset ) );
			test_suite.addTest ( new AccountManagerTester ( "testGroupUpdate", om_account, om_asset ) );
		}
		if ( b_run ) {
			test_suite.addTest ( new AclManagerTester ( "testAclLoad", om_acl, om_account, om_asset ) );
			test_suite.addTest ( new AclManagerTester ( "testAclUpdate", om_acl, om_account, om_asset ) );
		}
		
		olog.log ( Level.INFO, "PackageTestSuite.suite () returning ok ..." );
        return test_suite;
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.test;

import java.util.*;
import java.security.*;
import javax.security.auth.login.*;

import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.asset.AssetManager;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.base.BaseException;


/**
 * TestFixture runs SecurityMnaager implementations
 * through their paces.
 */
public class AccountManagerTester extends TestCase {
    private static final Logger     olog_generic = Logger.getLogger ( AccountManagerTester.class.getName() );

	private AccountManager   om_account = null;
	private AssetManager     om_asset = null;
	
	
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
	 * Constructor registers the AccountManager to test against.
	 * Use littleware.security.test.LoginTester as LoginContext application name.
	 *
	 * @param s_name of test case to run
	 * @param m_account to run test against
	 * @param m_asset to do saveAsset/deleteAsset/... calls against
	 */
	public AccountManagerTester ( String s_name,
								  AccountManager m_account,
								  AssetManager m_asset
								  ) {
		super ( s_name );
		om_account = m_account;
		om_asset = m_asset;
	}
	

	/**
	 * Erase the test users/groups out of the database,
	 */
	public void setUp () {
	}
	
	/** Call setUp() to clear out test data */
	public void tearDown () {
		setUp ();
	}
	
	/**
	 * Just retrieve some Principals - USER and GROUP
	 */
	public void testGetPrincipals () {
		try {
			LittleUser  p_admin_user = (LittleUser) om_account.getPrincipal ( AccountManager.LITTLEWARE_ADMIN );
			LittleGroup p_admin_group = (LittleGroup) om_account.getPrincipal (
																		  AccountManager.LITTLEWARE_ADMIN_GROUP
																		  );
			
			for ( Enumeration<? extends Principal> enum_x = p_admin_group.members ();
				  enum_x.hasMoreElements ();
				  ) {
				LittlePrincipal p_member = (LittlePrincipal) enum_x.nextElement ();
				olog_generic.log ( Level.INFO, "Got admin group member: " + p_member.getName () +
								   " (" + p_member.getObjectId () + ")" );
			}
			assertTrue ( "administrator should be member of admin group",
						 p_admin_group.isMember ( p_admin_user )
						 );
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught unexpected: " + e +
							   ", " + BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected exception: " + e + ", " + 
						 BaseException.getStackTrace ( e ), false );
		} 
	}
	
	/**
	 * Test interaction between AccountManager.updateUser
	 * and LoginManager login.
	 */
	public void testPasswordUpdate () {
		try {
			LittleUser p_user  = om_account.getAuthenticatedUser ();
			assertTrue ( "Test running as " + LoginTester.OS_TEST_USER, 
						 LoginTester.OS_TEST_USER.equals ( LoginTester.OS_TEST_USER ) 
						 );
			olog_generic.log ( Level.INFO, "Changing password for " + LoginTester.OS_TEST_USER );
			
			try {
				String s_password = "whatever";
				om_account.updateUser ( p_user, s_password, "change password" );
				Principal p_login = LoginTester.runLoginTest ( LoginTester.OS_TEST_USER, s_password, this );
				assertTrue ( "Login ok", p_login.getName ().equals ( LoginTester.OS_TEST_USER ) );
				try {
					LoginContext x_login = new LoginContext ( "littleware.security.simplelogin",
															  new SimpleNamePasswordCallbackHandler ( p_user.getName (), "bogus" )
															  );
					x_login.login ();
					assertTrue ( "Should have failed login with bogus password", false );
				} catch ( LoginException e ) {
					olog_generic.log ( Level.INFO, "Password check ok" );
				}
			} finally {
				om_account.updateUser ( p_user, LoginTester.OS_TEST_USER_PASSWORD, "restore password" );
			}
			Principal x_user  = om_account.getPrincipal ( LoginTester.OS_TEST_USER );
			Principal x_tmp   = om_account.getPrincipal ( "group." + LoginTester.OS_TEST_USER );

		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught unexpected: " + e +
							   ", " + BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected exception: " + e + ", " + 
						 BaseException.getStackTrace ( e ), false );
		} 
	}
	
	/**
	 * Just verify that incrementQuota increments the quota asset.
	 * Must be running the test as a user with an active Quota set.
	 */
	public void testQuota () {
		try {
			LittleUser p_me = om_account.getAuthenticatedUser ();
			Quota      a_quota_before = om_account.getQuota ( p_me );
			assertTrue ( "Got a quota we can test against",
						 (null != a_quota_before) 
						 && (a_quota_before.getQuotaLimit () > 0)
						 && (a_quota_before.getQuotaCount () >= 0)
						 );
			om_account.incrementQuotaCount ();
			Quota      a_quota_after = om_account.getQuota ( p_me );
			assertTrue ( "Quota incremented by 1: " + a_quota_before.getQuotaCount () +
						 " -> " + a_quota_after.getQuotaCount (), 
						 a_quota_before.getQuotaCount () + 1 == a_quota_after.getQuotaCount ()
						 );
			// Verify get/setData parsing
			String s_data = a_quota_after.getData ();
			a_quota_after.setData ( a_quota_after.getData () );
			assertTrue ( "get/setData consistency", s_data.equals ( a_quota_after.getData () ) );
		} catch  ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught unexpected: " + e +
							   ", " + BaseException.getStackTrace ( e )
							   );
			assertTrue ( "Caught unexpected exception: " + e + ", " + 
						 BaseException.getStackTrace ( e ), false );
		} 
	}
	
	
	/**
	 * Test group update
	 */
	public void testGroupUpdate () {
		try {
			LittleGroup  p_test = (LittleGroup) om_account.getPrincipal ( LoginTester.OS_TEST_GROUP );
			LittleUser   p_caller = om_account.getAuthenticatedUser ();
			
			if ( p_test.removeMember ( p_caller ) ) {
				p_test = (LittleGroup) om_asset.saveAsset ( p_test, "Removed tester " + p_caller.getName () );
			}
			p_test = (LittleGroup) om_account.getPrincipal ( LoginTester.OS_TEST_GROUP );
			assertTrue ( "Already removed caller as primary member of group",
						 ! p_test.removeMember ( p_caller )
						 );
			assertTrue ( "Added caller to test group: " + p_caller.getName (),
						 p_test.addMember ( p_caller )
						 );
			p_test = (LittleGroup) om_asset.saveAsset ( p_test, "Added tester " + p_caller.getName () );
			
			p_test = (LittleGroup) om_account.getPrincipal ( LoginTester.OS_TEST_GROUP );
			assertTrue ( "Able to remove caller " + p_caller.getName () + " from test group",
						 p_test.removeMember ( p_caller )
						 );
			p_test = (LittleGroup) om_asset.saveAsset ( p_test, "Removed tester " + p_caller.getName () );
			
			p_test = (LittleGroup) om_account.getPrincipal ( LoginTester.OS_TEST_GROUP );
			assertTrue ( "Already removed caller as primary member of group 2nd time",
						 ! p_test.removeMember ( p_caller )
						 );
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Caught: " + e + ", " + BaseException.getStackTrace ( e ) );
			assertTrue ( "Should not have caught: " + e, false );
		} 
	}

}



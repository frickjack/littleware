package littleware.security.test;

import littleware.security.server.AclManager;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import java.security.acl.*;
import java.util.*;

import junit.framework.*;

import littleware.asset.AssetManager;
import littleware.security.*;
import littleware.base.*;


/**
 * Run AclManager implementations through basic test.
 */
public class AclManagerTester extends TestCase {
	private static final Logger         olog_generic = Logger.getLogger ( "littleware.security.test.AclManagerTester" );
	private final AclManager     om_acl;
	private final AccountManager om_account;
	private final AssetManager   om_asset;
	
	
	/**
	 * Constructor sets testcase name, and stashes manager to test against.
	 *
	 * @param m_acl to test
	 * @param m_account to pull test_user test-user from
	 * @param m_asset to do save/delete tests with
	 */
	public AclManagerTester ( String s_test_name, AclManager m_acl, 
							  AccountManager m_account,
							  AssetManager   m_asset
							  ) {
		super ( s_test_name );
		om_acl = m_acl;
		om_account = m_account;		
        om_asset = m_asset;
	}
	
	/** Do nothing */
	public void setUp () {

	}
	
	/**
	 * Just call setUp ()
	 */
	public void tearDown () {
		setUp ();
	}
	
	/**
	 * Just try to load an ACL
	 */
	public void testAclLoad () {
		try {
			Acl acl_everybody = om_acl.getAcl ( LittleAcl.ACL_EVERYBODY_READ );
		} catch ( Exception e ) {
			olog_generic.severe ( "Caught unexpected: " + 
							   e + ", " + BaseException.getStackTrace ( e ) );
			assertTrue ( "Caught unexpected: " + e, false );
		}
	}

	/**
	 * Try to do some simle updates to the reserved test ACL
	 */
	public void testAclUpdate () {
		try {
			Acl acl_test = om_acl.getAcl ( "acl.littleware.test_acl" );
		} catch ( Exception e ) {
			olog_generic.severe ( "Caught unexpected: " + 
								  e + ", " + BaseException.getStackTrace ( e ) );
			assertTrue ( "Caught unexpected: " + e, false );
		}
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


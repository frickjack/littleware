package littleware.security.test;

import java.util.*;
import java.security.*;
import java.security.acl.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.asset.AssetSearchManager;
import littleware.base.*;
import littleware.security.*;
import littleware.security.server.SecurityResourceBundle;

/**
 * Test case for given ACL instance.
 * Just sets up some bogus permissions in the ACL, then
 * checks that the ACL enforces them correctly.
 */
public class AclTester extends TestCase {
    private static final Logger     olog_generic = Logger.getLogger ( "littelware.security.test" );

	private final LittleAcl           oacl_test;
	private final Principal           op_owner;
    private final AssetSearchManager  om_search;
    
	
	/**
	 * Constructor registers the ACL to run against.
	 * Just uses SimplePrincipal, SimpleGroup, and SimpleAclEntry instances
	 * as principals to test against.
	 * Also includes testOwner method that exercises the Acl's Owner.
	 *
	 * @param s_name of test case to run
	 * @param acl_test to test against - should start empty
	 * @param acl_test_owner with permission to add to the Acl
	 */
	public AclTester ( String s_name,
					   LittleAcl acl_test, 
					   Principal acl_test_owner,
                       AssetSearchManager m_search
					   ) {
		super ( s_name );
		oacl_test = acl_test;
		op_owner = acl_test_owner;
        om_search = m_search;
	}
	
	/** No setup necessary */
	public void setUp () {}
	
	/** No tearDown necessary */
	public void tearDown () {}
	
	/** Stupid little Acl permission for use in test cases */
	public static class BogusAclPermission implements java.security.acl.Permission {
		private int oi_id;
		
		/** Constructor just gives integer id to this guy */
		public BogusAclPermission ( int i_id ) { oi_id = i_id; }
		
		/** Equal if same class and same id */
		public boolean equals ( Object x_other ) {
			return ((null != x_other) && (x_other instanceof AclTester.BogusAclPermission)
					&& (oi_id == ((BogusAclPermission) x_other).oi_id));
		}
		
		/** Just return id number */
		public String toString () {
			return "BogusAclPermission id: " + oi_id;
		}
		
		/** Return id as hash-code */
		public int hashCode () { return oi_id; }
	}
	
	/**
	 * Just run the ACL through some simple scenarios
	 */
	public void testAcl () {
		assertTrue ( "Test ACL should start out empty", ! oacl_test.entries ().hasMoreElements () );
		olog_generic.log ( Level.INFO, "testAcl starting with empty test ACL ok" );
		Factory<UUID> factory_uuid = UUIDFactory.getFactory ();
		
		try {
			{ // Make sure our test permission is working
				BogusAclPermission x_perm = new BogusAclPermission ( 5 );
				assertTrue ( "Test permission type recognizes equality between instances",
							x_perm.equals ( new BogusAclPermission ( 5 ) ) );
				
				UUID u_id = factory_uuid.create ();
				Principal p_test = new SimpleUser ( "10", u_id, "whatever" );
				assertTrue ( "SimplePrincipal recognizes equality between instances",
							 p_test.equals ( new SimpleUser ( "10", u_id, "whatever" ) ) );
			}
							 
			// Generate AclEntries for a bunch of principal's
			for ( int i=10; i < 20; ++i ) {
				Principal p_test = new SimpleUser ( Integer.toString ( i ),
															  factory_uuid.create (), "no comment" );
				Set       v_check = new HashSet ();
				
				olog_generic.log ( Level.INFO, "Registering and verifying AclEntry for principal: " + p_test );
				AclEntry x_entry = (AclEntry) SecurityAssetType.ACL_ENTRY.create ();
				x_entry.setPrincipal ( p_test );
				assertTrue ( "Registered AclEntry principal equals retrieved principal",
							 x_entry.getPrincipal ().equals ( p_test ) );
				
				// Assign a couple permissions to the entry
				for ( int j = 0; j < 10; ++j ) {
					assertTrue ( "Added permission to AclEntry: " + j,
								 x_entry.addPermission ( new BogusAclPermission ( j ) ) );
					assertTrue ( "Simple set add works too",
								 v_check.add ( new BogusAclPermission ( j ) ) );
				}
				// Remove few permissions
				for ( int j = 5; j < 8; ++j ) {
					assertTrue ( "Simple set remove works too: " + j,
								 v_check.remove ( new BogusAclPermission ( j ) ) );

					assertTrue ( "Removed permission from principal " + p_test + " AclEntry: " + j,
								 x_entry.removePermission ( new BogusAclPermission ( j ) ) );
				}
				assertTrue ( "AclEntry is not negative", false == x_entry.isNegative () );
				
				try {
					oacl_test.addEntry ( op_owner, x_entry );
				} catch ( Exception e ) {
					assertTrue ( "Caught unexpected: " + e, false );
				}
			}
			// Ok, let's test a few ops
			for ( int i=10; i < 20; ++i ) {
				Principal p_test = new SimpleUser ( Integer.toString ( i ),
															  factory_uuid.create (), "no comment" );			
				// Assign a couple permissions to the entry
				for ( int j = 0; j < 5; ++j ) {
					java.security.acl.Permission x_permission = new BogusAclPermission ( j );
					assertTrue ( "Principal " + p_test + " does not have permission " + x_permission,
								 ! oacl_test.checkPermission ( p_test, x_permission )
								 );
				}
				// Remove few permissions
				for ( int j = 5; j < 8; ++j ) {
					java.security.acl.Permission x_permission = new BogusAclPermission ( j );
					assertTrue ( "Principal " + p_test + " does not have permission " + x_permission,
								 ! oacl_test.checkPermission ( p_test, x_permission )
								 );
				}			
			}
			{
				// Make sure setData/getData are valid
				LittleAclEntry   acl_entry = (LittleAclEntry) SecurityAssetType.ACL_ENTRY.create ();
				
				acl_entry.addPermission ( LittlePermission.READ );
				String s_data = acl_entry.getData ();
				acl_entry.setData ( s_data );
				assertTrue ( "get/setData consistent", s_data.equals ( acl_entry.getData () ) );
			}
		} catch ( BaseException e ) {
			olog_generic.log ( Level.INFO, "Caught unexepcted: " + e );
			assertTrue ( "Should not have caught: " + e, false );
		}
		
		// Should add test cases involving negative AclEntries and Groups
		olog_generic.log ( Level.INFO, "exiting - add group and negative tests when you get the chance" );
	}
	
	/**
	 * Run the Owner interface of the test ACL through some basic tests
	 */
	public void testAclOwner () {		
		try {
			final Principal p_admin = (LittlePrincipal) om_search.getByName ( AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER );
				
			assertTrue (  "Successfully retrieved administrator", null != p_admin );
			olog_generic.log ( Level.INFO, "Successfull retrieved administrator principal: " + p_admin );
			
            Owner owner_acl = oacl_test.getOwner ( om_search );
			assertTrue ( "administrator is owner of test Acl", owner_acl.isOwner ( p_admin ) );
			Principal p_bogus = (Principal) om_search.getByName ( LoginTester.OS_TEST_USER, SecurityAssetType.USER );
			
            owner_acl.addOwner ( p_admin, p_bogus );
            assertTrue ( "ownership detected on test acl", owner_acl.isOwner ( p_bogus ) );
            assertTrue ( "able to remove owner from acl", owner_acl.deleteOwner ( p_admin, p_bogus ) );
            assertTrue ( "Administrator should be an owner", owner_acl.isOwner ( p_admin ) );
		} catch ( Exception e ) {
			assertTrue ( "Caught unexpected: " + e + ", " + BaseException.getStackTrace ( e ), false );
		}
	}
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


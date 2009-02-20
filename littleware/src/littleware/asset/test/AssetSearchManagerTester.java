/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;


import junit.framework.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.LittlePrincipal;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.SecurityAssetType;


/**
 * Tester for implementations of the AssetSearchManager interface.
 * AssetPathTester also tests AssetSearchManager.
 */
public class AssetSearchManagerTester extends TestCase {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.test.AssetSearchManagerTester" );
	
	private final AssetSearchManager  om_search;
	
	/**
	 * Stash AssetSearchManager instance to run tests against
	 *
	 * @param s_test_name of test to run - pass to super class
	 * @param m_search to test against
	 */
	public AssetSearchManagerTester ( String s_test_name, AssetSearchManager m_search ) {
		super( s_test_name );
		om_search = m_search;
	}
	
	/**
	 * No setUp necessary
	 */
	public void setUp () {
	}
	
	/**
	 * No teardown necessary
	 */
	public void tearDown () {
	}
	
	/**
	 * Just load some test assets.
	 */
	public void testSearch () {
		try {
			Asset a_lookup = om_search.getByName ( AssetManagerTester.MS_TEST_HOME,
															  AssetType.HOME
                                                        );
			assertTrue ( "Got some home-by-name data", null != a_lookup );
            
            LittleGroup group_everybody = (LittleGroup) om_search.getByName ( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                                                                                   SecurityAssetType.GROUP
                                                                                   );
            for ( Enumeration<? extends Principal> enum_members = group_everybody.members ();
                  enum_members.hasMoreElements ();
                  ) {
                LittlePrincipal p_member = (LittlePrincipal) enum_members.nextElement ();
                Set<UUID>       v_links = om_search.getAssetIdsTo ( p_member.getObjectId (),
                                                                    SecurityAssetType.GROUP_MEMBER 
                                                                    );
                assertTrue ( "Group member as links TO it: " + p_member,
                             ! v_links.isEmpty ()
                             );
            }
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught: " + e + ", " +
                               BaseException.getStackTrace ( e )
                               );
			assertTrue ( "Caught: " + e, false );
		}			
	}
	
}


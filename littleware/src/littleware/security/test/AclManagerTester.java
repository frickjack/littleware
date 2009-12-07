/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.test;

import com.google.inject.Inject;
import java.util.logging.Logger;
import java.security.acl.*;


import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.security.*;
import littleware.base.*;
import littleware.test.LittleTest;


/**
 * Run ACL implementations through basic test.
 */
public class AclManagerTester extends LittleTest {
	private static final Logger         olog_generic = Logger.getLogger ( AclManagerTester.class.getName() );
	private final AssetSearchManager osearch;
	private final AssetManager       om_asset;
	
	
	/**
	 * Constructor sets testcase name, and stashes manager to test against.
	 *
	 * @param m_acl to test
	 * @param m_account to pull test_user test-user from
	 * @param m_asset to do save/delete tests with
	 */
    @Inject
	public AclManagerTester ( 
							  AssetSearchManager search,
							  AssetManager   m_asset
							  ) {
		osearch = search;
        om_asset = m_asset;
        setName( "testAclLoad");
	}
	
	
	/**
	 * Just try to load an ACL
	 */
	public void testAclLoad () {
		try {
			final Acl acl_everybody = osearch.getByName ( LittleAcl.ACL_EVERYBODY_READ, SecurityAssetType.ACL ).get();
		} catch ( Exception e ) {
			olog_generic.severe ( "Caught unexpected: " + 
							   e + ", " + BaseException.getStackTrace ( e ) );
			assertTrue ( "Caught unexpected: " + e, false );
		}
	}

}


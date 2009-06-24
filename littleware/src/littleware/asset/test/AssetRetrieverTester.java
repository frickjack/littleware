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

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.*;
import littleware.base.*;
import littleware.security.SecurityAssetType;
import littleware.test.LittleTest;

/**
 * Tester for implementations of the AssetRetriever interface 
 */
public class AssetRetrieverTester extends LittleTest {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.test.AssetRetrieverTester" );
	
	private final AssetRetriever  om_asset;
		
	/**
	 * Stash AssetRetriever instance to run tests against
	 *
	 * @param s_test_name of test to run - pass to super class
	 * @param m_asset to test against
	 */
    @Inject
	public AssetRetrieverTester ( AssetRetriever m_asset ) {
		om_asset = m_asset;
        setName( "testLoad" );
	}
	
	
	/**
	 * Just load some test assets.
	 */
	public void testLoad () {
		try {
			Map<String,UUID> v_home_id = om_asset.getHomeAssetIds ();
			assertTrue ( "Home-asset set is not empty", ! v_home_id.isEmpty () );
			for ( String s_home : v_home_id.keySet () ) {
				olog_generic.log ( Level.INFO, "getHomeAssetIds found home: " + s_home );
			}
			assertTrue ( "Test-home is in home set: " + getTestHome(),
						 v_home_id.containsKey ( getTestHome() )
						 );
			assertTrue( "Able to retrieve test home: " + getTestHome(),
                    om_asset.getAsset ( v_home_id.get ( getTestHome() ) ).isSet()
                    );
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught: " + e );
			assertTrue ( "Caught: " + e, false );
		}			
	}
    
    AssetType<Asset> BOGUS = new AssetType<Asset> (
                                                    UUIDFactory.parseUUID ( "7D7B573B-4BF5-4A2F-BDC1-A614935E56AD" ),
                                                    "littleware.BOGUS"
                                                ) {
		/** Get a LittleUser implementation */
        @Override
		public Asset create () throws FactoryException { return (Asset) null; }

        @Override
        public AssetType getSuperType () { 
            return SecurityAssetType.PRINCIPAL;
        }
	};
    
    
    /**
     * Just stick this test here rather than make a separate class.
     * Verify that AssetType inheritance sort of works.
     */
    public void testAssetType () {
        assertTrue ( "BOGUS isA PRINCIPAL",
                     BOGUS.isA ( SecurityAssetType.PRINCIPAL )
                     );
        assertTrue ( "BOGUS != PRINCIPAL",
                     ! BOGUS.equals ( SecurityAssetType.PRINCIPAL )
                     );
        assertTrue ( "BOGUS is name unique",
                     BOGUS.isNameUnique ()
                     );
        assertTrue ( "BOGUS is not admin-create only",
                     ! BOGUS.mustBeAdminToCreate ()
                     );
    }
	
}



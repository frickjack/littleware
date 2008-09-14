package littleware.asset.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;

import junit.framework.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.SecurityAssetType;

/**
 * Tester for implementations of the AssetRetriever interface 
 */
public class AssetRetrieverTester extends TestCase {
	private static final Logger olog_generic = Logger.getLogger ( "littleware.asset.test.AssetRetrieverTester" );
	
	private final AssetRetriever  om_asset;
		
	/**
	 * Stash AssetRetriever instance to run tests against
	 *
	 * @param s_test_name of test to run - pass to super class
	 * @param m_asset to test against
	 */
	public AssetRetrieverTester ( String s_test_name, AssetRetriever m_asset ) {
		super( s_test_name );
		om_asset = m_asset;
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
	public void testLoad () {
		try {
			Map<String,UUID> v_home_id = om_asset.getHomeAssetIds ();
			assertTrue ( "Home-asset set is not empty", ! v_home_id.isEmpty () );
			for ( String s_home : v_home_id.keySet () ) {
				olog_generic.log ( Level.INFO, "getHomeAssetIds found home: " + s_home );
			}
			assertTrue ( "Test-home is in home set: " + AssetManagerTester.MS_TEST_HOME,
						 v_home_id.containsKey ( AssetManagerTester.MS_TEST_HOME )
						 );
			Asset a_home = om_asset.getAsset ( v_home_id.get ( AssetManagerTester.MS_TEST_HOME ) );
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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.asset.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.*;


/**
 * Tester for implementations of the AssetManager interface 
 */
public class AssetManagerTester extends TestCase {
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.test.AssetManagerTester" );
	
	private AssetManager       om_asset = null;
	private AssetSearchManager om_search = null;
	private List<Asset>        ov_cleanup_list = new ArrayList<Asset> ();
	
	public final static String  MS_TEST_HOME = "littleware.test_home";
	
	/**
	 * Stash AssetManager instance to run tests against
	 *
	 * @param s_test_name of test to run - pass to super class
	 * @param m_asset to test against
	 * @param m_search to verify test results against
	 */
	public AssetManagerTester ( String s_test_name, AssetManager m_asset, AssetSearchManager m_search ) {
		super( s_test_name );
		om_asset = m_asset;
		om_search = m_search;
	}
	
	/**
	 * No setUp necessary
	 */
	public void setUp () {
	}
	
	/**
	 * Try to remove the test-assets from the asset-tree
	 */
	public void tearDown () {
		try {
			for ( Asset a_cleanup : ov_cleanup_list ) {
				om_asset.deleteAsset ( a_cleanup.getObjectId () , "Cleanup after test" );
			}
		} catch ( Exception e ) {
			olog_generic.log ( Level.INFO, "Failed to cleanup all test assets, caught: " + e );
		} finally {
			ov_cleanup_list.clear ();
		}
	}
	
	/**
	 * Just do some simple asset creationg/deletion/bla bla bla.
	 */
	public void testAssetCreation () {
		try {
			Asset       a_home = om_search.getByName ( MS_TEST_HOME, AssetType.HOME );
			Asset       a_acl = null;
			
            olog_generic.log ( Level.INFO, "Running with test home: " + a_home );
			
			LittleUser p_caller = SecurityAssetType.getAuthenticatedUserOrNull ();
            assertTrue ( "Have an authenticated user", null != p_caller );
			String     s_name = "test_" + (new Date());

			Asset a_test = AssetType.GENERIC.create ();
			a_test.setName ( s_name );
			a_test.setData ( "<data>no data </data>" );
            a_test.setToId ( null );
            a_test.setFromId ( null );
            a_test.setOwnerId ( p_caller.getObjectId () );
			
            olog_generic.log ( Level.INFO, "Saving new asset" );
			a_test = om_asset.saveAsset ( a_test, "new asset" );
			
			olog_generic.log ( Level.INFO, "Just created asset: " + s_name );
			assertTrue ( "Created an asset with some valid data",
						 (a_test.getObjectId () != null)
						 && a_test.getName ().equals ( s_name )
						 && a_test.getAssetType ().equals ( AssetType.GENERIC )
						 );
			
			
			Asset a_clone = (Asset) a_test.clone ();
			assertTrue ( "Able to clone new asset",
						 a_test.equals ( a_clone )
						 && a_clone.getName ().equals ( a_test.getName () )
						 && a_clone.getAssetType ().equals ( a_test.getAssetType () )
						 );
						 
			// Try to update the asset
			a_test.setData ( "<data> some data </data>" );
			a_test = om_asset.saveAsset ( a_test, "data update" );
			
			// Delete the asset
			om_asset.deleteAsset ( a_clone.getObjectId () , "Cleanup test case" );
			a_test = om_search.getAssetOrNull ( a_test.getObjectId () );
			
			assertTrue ( "No longer able to retrieve deleted asset: " + a_clone.getObjectId (),
						 null == a_test
						 );
											
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught: " + e + 
							   ", " + BaseException.getStackTrace ( e ) );
			assertTrue ( "Caught: " + e, false );
		}			
	}

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


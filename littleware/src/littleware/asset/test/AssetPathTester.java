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
import littleware.test.LittleTest;


/**
 * Test traversal of some asset paths.
 */
public class AssetPathTester extends LittleTest {
	private static Logger olog_generic = Logger.getLogger ( AssetPathTester.class.getName() );
	
    private final AssetManager        om_asset;
	private final AssetSearchManager  om_search;
    private final AssetPathFactory opathFactory;

	
	/**
     * Stash AssetSearchManager instance to run tests against
	 *
	 * @param s_test_name of test to run - pass to super class
	 * @param m_search to test against
     * @param m_asset to setup test assets with if necessary
	 */
    @Inject
	public AssetPathTester ( AssetSearchManager m_search,
                             AssetManager m_asset,
                             AssetPathFactory pathFactory
                             ) {
		om_search = m_search;
        om_asset = m_asset;
        opathFactory = pathFactory;
        setName( "testPathTraverse" );
	}
	
	/**
     * Setup a test asset tree under the littleware.test_home:
     *         littleware.test_home/AssetPathTester/A,Points2A/1,2,3,biggest,smallest
     *
     * @TODO - switch over to mock-based test
	 */
    @Override
	public void setUp () {
        try {
            final Asset a_home = getTestHome(om_search);
            Asset a_test = om_search.getAssetFrom( a_home.getObjectId (), "AssetPathTester" ).getOr(null);
            if ( null == a_test ) {
                a_test = AssetType.GENERIC.create ();
                a_test.setName ( "AssetPathTester" );
                a_test.setComment ( "AssetPath traversal test area" );
                a_test.setFromId ( a_home.getObjectId () );
                a_test.setHomeId ( a_home.getObjectId () );
                a_test = om_asset.saveAsset ( a_test, "Setting up AssetPathTester test area" );
            }
            Asset a_A = om_search.getAssetFrom( a_test.getObjectId (), "A" ).getOr(null);
            Asset a_pointer = om_search.getAssetFrom ( a_test.getObjectId (), "Points2A" ).getOr( null );
            if ( null == a_A ) {
                a_A = AssetType.GENERIC.create ();
                a_A.setName ( "A" );
                a_A.setFromId ( a_test.getObjectId () );
                a_A.setComment ( "AssetPath traversal test area" );
                a_A.setHomeId ( a_home.getObjectId () );
                a_A = om_asset.saveAsset ( a_A, "Setting up AssetPathTester test area" );
                
                if ( null != a_pointer ) {
                    a_pointer.setToId ( a_A.getObjectId () );
                    a_pointer = om_asset.saveAsset ( a_pointer, "Update TO pointer for new A asset" );
                }
            }
            if ( null == a_pointer ) {
                a_pointer = AssetType.LINK.create ();
                a_pointer.setName ( "Points2A" );
                a_pointer.setComment ( "link to A" );
                a_pointer.setFromId ( a_test.getObjectId () );
                a_pointer.setToId ( a_A.getObjectId () );
                a_pointer.setHomeId ( a_home.getObjectId () );
                a_pointer = om_asset.saveAsset ( a_pointer, "Setup LINK in AssetPathTester test tree" );
            }
            Asset[] v_number = new Asset[3];
            for ( int i=1; i < 4; ++i ) {
                String s_name = Integer.toString( i );
                Asset  a_number = om_search.getAssetFrom ( a_A.getObjectId (), s_name )
                        .getOr(null);
                if ( null == a_number ) {
                    a_number = AssetType.GENERIC.create ();
                    a_number.setName ( s_name );
                    a_number.setComment ( "Setting up AssetPathTester" );
                    a_number.setFromId ( a_A.getObjectId () );
                    a_number.setHomeId ( a_home.getObjectId () );
                    a_number.setToId ( a_A.getObjectId () );
                    om_asset.saveAsset ( a_number, "Setting up AssetPathTester" );
                }
                v_number[ i-1 ] = a_number;
            }
            Asset a_smallest = om_search.getAssetFrom ( a_A.getObjectId (), "smallest" ).getOr( null );
            if ( null == a_smallest ) {
                a_smallest = AssetType.LINK.create ();
                a_smallest.setName ( "smallest" );
                a_smallest.setFromId ( a_A.getObjectId () );
                a_smallest.setToId ( v_number[0].getObjectId () );
                a_smallest.setHomeId ( a_home.getObjectId () );
                a_smallest.setComment ( "link to smallest number" );
                a_smallest = om_asset.saveAsset ( a_smallest, "Setting up AssetPathTester" );
            }
            Asset a_biggest = om_search.getAssetFrom( a_A.getObjectId (), "biggest" ).getOr( null );
            if ( null == a_biggest ) {
                a_biggest = AssetType.LINK.create ();
                a_biggest.setName ( "biggest" );
                a_biggest.setFromId ( a_A.getObjectId () );
                a_biggest.setToId ( v_number[ v_number.length - 1 ].getObjectId () );
                a_biggest.setHomeId ( a_home.getObjectId () );
                a_biggest.setComment ( "link to biggest number" );
                a_biggest = om_asset.saveAsset ( a_biggest, "Setting up AssetPathTester" );
            }
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            olog_generic.log ( Level.SEVERE, "Failed to setup AssetPathTester, caught: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
            throw new AssertionFailedException ( "Failed setup, caught: " + e, e );
        }
        
	}
	
	/**
     * No teardown necessary
	 */
    @Override
	public void tearDown () {
	}
	
	/**
     * Traverse some test assets under 
     *             /byname:littleware.test_home:type:littleware.HOME/AssetPathTester/bla
	 */
	public void testPathTraverse () {
        List<AssetPath>  v_tests = new ArrayList<AssetPath> ();

		try {
            v_tests.add ( opathFactory.createPath ( "littleware.test_home/AssetPathTester/A/../A/1/../1"
                                                    )
                          );
            v_tests.add ( opathFactory.createPath ( "/littleware.test_home/AssetPathTester/A/2/../../Points2A/2/@/2"
                                                    )
                          );
            v_tests.add ( opathFactory.createPath ( "littleware.test_home/AssetPathTester/Points2A/2/@/3"
                                                    )
                          );
            
            assertTrue ( "Got a search manager", null != om_search );
            int i_count = 0;
            for( AssetPath path_test : v_tests ) {
                ++i_count;
                assertTrue ( "Path properly normalized: " + path_test,
                             path_test.toString ().indexOf ( ".." ) < 0
                             );
                assertTrue( "Path has expected basename " + i_count + ": " + path_test.getBasename(),
                        path_test.getBasename().equals( Integer.toString( i_count ) )
                        );
                AssetPath path_root = opathFactory.createPath( path_test.getRoot ( om_search ).get().getObjectId (),
                                                               path_test.getSubRootPath ()
                                                               );
                assertTrue ( "SubRoot paths match: " + path_test.getSubRootPath () + " == " +
                             path_root.getSubRootPath (),
                             path_test.getSubRootPath ().equals ( path_root.getSubRootPath () )
                             );
                AssetPath path_name_string = opathFactory.createPath ( path_test.toString () );
                AssetPath path_id_string = opathFactory.createPath( path_root.getRoot ( om_search ).get().getObjectId (),
                                                               path_root.getSubRootPath ()
                                                               );
                
                assertTrue ( path_name_string.toString () + " == " + path_test.toString (),
                             path_name_string.equals ( path_test )
                             );
                assertTrue ( path_id_string.toString () + " == " + path_root.toString (),
                             path_id_string.equals ( path_root )
                             );
                final Asset a_test = path_test.getAsset( om_search ).get();
                assertTrue ( "Got the same asset from " + path_test + " and " + path_root,
                             a_test.equals ( path_root.getAsset ( om_search ).get() )
                             );
                final AssetPath pathRoot = opathFactory.toRootedPath( opathFactory.createPath( a_test.getObjectId() ) );
                assertTrue( "Root path resolver works ok: " + pathRoot,
                        pathRoot.getAsset(om_search).equals( a_test )
                        && pathRoot.toString().equals( opathFactory.toRootedPath( opathFactory.createPath( a_test.getObjectId() ) ).toString() )
                        );
                AssetPath  path_parent = path_test.getParent ();
                assertTrue ( "Path has parent: " + path_test,
                             path_test.hasParent ()
                             );
                assertTrue ( "Path compares greater than parent: " + path_test + " <>? " + path_parent,
                             path_test.compareTo ( path_parent ) > 0
                             );
                //---
                assertTrue ( "Got expected name: " + i_count + " == " + a_test.getName (),
                             Integer.toString ( i_count ).equals ( a_test.getName () )
                             );
                
                if ( 1 == i_count ) {
                    AssetPath path_smallest = opathFactory.createPath( path_test.toString () + "/@/smallest" );
                    Asset     a_smallest = path_smallest.getAsset ( om_search ).get();
                    assertTrue ( "Smallest link resolved to asset 1: " + a_smallest.getName (),
                                 a_smallest.equals ( a_test )
                                 );
                    assertTrue( "Get by id ok",
                            opathFactory.createPath( a_smallest.getObjectId().toString() ).getAsset( om_search ).equals( a_smallest )
                            );
                } else if ( i_count == v_tests.size () ) {
                    AssetPath path_biggest = opathFactory.createPath( path_test.toString () + "/@/biggest" );
                    Asset     a_biggest = path_biggest.getAsset ( om_search ).get();
                    assertTrue ( "Biggest link resolved to asset 1: " + a_biggest.getName (),
                                 a_biggest.equals ( a_test )
                                 );
                }                                
            }
            
		} catch ( Exception e ) {
			olog_generic.log ( Level.WARNING, "Caught unexpected: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
			assertTrue ( "Caught: " + e, false );
		}			
	}
	
}


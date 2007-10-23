package littleware.base.test;

import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.base.*;


/**
 * Test implementations of littleware.base.Cache
 */
public class CacheTester extends TestCase {
	private Logger  olog_generic = Logger.getLogger ( "littleware.base.test.CacheTester" );
	private Cache   ocache_testobj = null;
	private long    ol_ageout_secs = 0L;
	private int     oi_size = 0;
	
	/**
	 * Constructor just stuffs away the cache to test
	 *
	 * @param s_name of test to run
	 * @param cache_testobj to test
	 * @param l_ageout_secs associated with the cache if any
	 * @param i_size limit on cache
	 */
	public CacheTester ( String s_name, Cache cache_testobj, long l_ageout_secs, int i_size ) {
		super ( s_name );
		ocache_testobj = cache_testobj;
		ol_ageout_secs = l_ageout_secs;
		oi_size = i_size;
	}
	
	/** Just flush the cache */
	public void setUp () { ocache_testobj.clear (); }
	
	/** Just flush the cache */
	public void tearDown () { setUp (); }
	
	/**
	 * Run some generic put/get/flush tests
	 */
	public void testGeneric () {
		for ( int i=0; i < 10; ++i ) {
			String  s_key = Integer.toString ( i );
			Integer x_value = Integer.valueOf ( i );
			
			assertTrue ( "Cache should be empty", null == ocache_testobj.put ( s_key, x_value ) );
		}
		for ( int i=0; i < 10; ++i ) {
			String s_key = Integer.toString ( i );
			Object x_value = ocache_testobj.get ( s_key );
			assertTrue ( "Cache should have entry: " + i, null != x_value );
			assertTrue ( "Value instance of Integer", x_value instanceof Integer );
			assertTrue ( "Retrieved wrong value for key: " + i, i == ((Integer) x_value).intValue () );
		}
		for ( int i=0; i < 10; ++i ) {
			String  s_key = Integer.toString ( i );
			Integer x_value = Integer.valueOf ( i+5 );
			
			assertTrue ( "Cache should not be empty", null != ocache_testobj.put ( s_key, x_value ) );
		}
		for ( int i=0; i < 10; ++i ) {
			String s_key = Integer.toString ( i );
			Object x_value = ocache_testobj.get ( s_key );
			assertTrue ( "Cache should have entry: " + i, null != x_value );
			assertTrue ( "Value instance of Integer", x_value instanceof Integer );
			assertTrue ( "Retrieved wrong 2nd value for key: " + i, i+5 == ((Integer) x_value).intValue () );
			
			ocache_testobj.remove ( s_key );
			assertTrue ( "Should have lost entry after flush: " + i, null == ocache_testobj.get ( s_key ) );
		}
	}
	
	/**
	 * Test that the freakin' thing doesn't go beyond the max-size
	 */
	public void testSizeLimit () {
		int i_max = oi_size + 50;
		
		for ( int i = 0; i < i_max; ++i ) {
			String  s_key = Integer.toString ( i );
			Integer x_value = Integer.valueOf ( i );
			
			assertTrue ( "Cache should be empty", null == ocache_testobj.put ( s_key, x_value ) );
		}
		
		int i_hit = 0;
		int i_miss = 0;
		for ( int i=0; i < i_max; ++i ) {
			String s_key = Integer.toString ( i );
			Object x_value = ocache_testobj.get ( s_key );
			if ( null != x_value ) {
				++i_hit;
			} else {
				++i_miss;
			}
		}
		
		assertTrue ( "Cache of size " + oi_size + " maxed out with hits: " + i_hit,
					 i_hit > oi_size - 2 );
		assertTrue ( "Cache of size " + oi_size + " grew beyond size limit to: " + i_hit,
					 i_miss > 0 );
	}
	
	/**
	 * Verify that the cache ages entries out
	 */
	public void testAgeOut () {
		int i_max = 10;
		
		for ( int i = 0; i < i_max; ++i ) {
			String  s_key = Integer.toString ( i );
			Integer x_value = Integer.valueOf ( i );
			
			assertTrue ( "Cache should be empty", null == ocache_testobj.put ( s_key, x_value ) );
		}
		
		assertTrue ( "ageout secs > 0 ? : " + ol_ageout_secs, ol_ageout_secs > 0 );
		olog_generic.log ( Level.INFO, "Sleeping to age out cache: " + (ol_ageout_secs + 5) + " secs" );
		try {
			Thread.sleep ( 1000 * (ol_ageout_secs + 5) );
		
			for ( int i=0; i < i_max; ++i ) {
				String s_key = Integer.toString ( i );
				Object x_value = ocache_testobj.get ( s_key );
				assertTrue ( "Value should have aged out: " + i, null == x_value ); 
			}
		} catch ( InterruptedException e ) {
			assertTrue ( "Test interrupted, caught: " + e, false );
		}
	}
		
		
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


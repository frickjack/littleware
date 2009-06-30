/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import java.util.*;
import java.util.logging.Logger;

/**
 * Simple Cache implementation using FIFO replacement policy.
 * Methods are synchronzied internally.
 */
public class SimpleCache<K,V> implements Cache<K,V> {
	private int           oi_ageout_secs = 300;
	private int           oi_size		= 3000;
	private Map<K,CacheEntry>      ov_cache       = new HashMap<K,CacheEntry> ();
	// List of CacheEntry (below) objects
	private List<CacheEntry>	 ov_entries     = new LinkedList<CacheEntry> ();
	
	private static Logger   olog_generic      = Logger.getLogger ( "littleware.base.SimpleCache" );
	
	public static final int   MIN_AGEOUT_SECS = 10;
	public static final int   MIN_SIZE        = 10;
	
	/**
	 * Internal data bucket 
	 */
	private class CacheEntry {
		private Date     ot_created = new Date ();
		private K   ox_key;
		private V   ox_value;
		private boolean  ob_in_cache = true;
		
		/** 
		 * Constructor sets internal key/value,
		 * creation date, and in_cache to true
		 */
		public CacheEntry ( K x_key, V x_value ) {
			ox_key = x_key;
			ox_value = x_value;
		}
		
		public Date getCreateDate () { return ot_created; }
		public K getKey () { return ox_key; }
		public V getValue () { return ox_value; }
		public boolean getInCache () { return ob_in_cache; }
		/** Set this entry getInCache() value to false */
		public void    markOutOfCache () { ob_in_cache = false; }
		
		/** Equals operation only true if exactly the same object */
        @Override
		public boolean equals ( Object x_other ) {
			return ((null != x_other)
					&& (x_other instanceof SimpleCache.CacheEntry)
					&& ox_key.equals ( ((CacheEntry) x_other).ox_key ) 
					);
		}
        
        @Override
        public int hashCode () {
            return ox_key.hashCode ();
        }
	}
			
	
	/**
	 * Default constructor - timeout 300 secs, maxsize 3000
	 */
	public SimpleCache () {}
	
	/**
	 * Constructor sets user supplied values for maxsize and timeout.
	 * Illegal values are silently converted to legal range.
	 *
	 * @param i_ageout_secs age at which an entry is automatically 
	 *               aged out of the cache - must be at least MIN_AGEOUT_SECS
	 * @param i_size of the chache - FIFO replacement policy kicks in
	 *                      - must be at lest MIN_SIZE
	 */
	public SimpleCache ( int i_ageout_secs, int i_size ) {
		if ( i_ageout_secs >= MIN_AGEOUT_SECS ) {
			oi_ageout_secs = i_ageout_secs;
		} else {
			oi_ageout_secs = MIN_AGEOUT_SECS;
		}
		if ( i_size >= MIN_SIZE ) {
			oi_size = i_size;
		} else {
			oi_size = MIN_SIZE;
		}
	}
	
    @Override
	public int getMaxEntryAgeSecs () { return oi_ageout_secs; }
    @Override
	public int getMaxSize () { return oi_size; }

	/**
	 * Insert the given value into the cache under the given key.
	 * Age out members as necessary.
	 * 
	 * @param x_key
	 * @param x_value
	 * @return null or previous value registered with key
	 */
    @Override
	public synchronized V put ( K x_key, V x_value ) {
		CacheEntry x_entry = new CacheEntry ( x_key, x_value );
		CacheEntry x_old_entry = ov_cache.put ( x_key, x_entry );
		
		ov_entries.add ( x_entry );
		if ( null != x_old_entry ) {
			x_old_entry.markOutOfCache ();
			return x_old_entry.getValue ();
		}
		// Just added an entry, need to make sure we haven't overflowed the cache
		for ( int i_size = ov_entries.size ();
			  i_size > oi_size;
			  --i_size ) {
			x_old_entry = (CacheEntry) ov_entries.remove( 0 );
			ov_cache.remove ( x_old_entry.getKey () );
			x_old_entry.markOutOfCache ();
		}
		return null;
	}

	/**
	 * Retrieve the entry with the given key from the cache.
	 *
	 * @param x_key
	 * @return the cache entry value, or null if no entry or aged out
	 */
    @Override
	public synchronized V get ( K x_key ) {
		CacheEntry x_entry = ov_cache.get ( x_key );
		
		if ( null == x_entry ) {
			return null;
		}
		Date       t_now = new Date ();
		if ( x_entry.getCreateDate ().getTime () + oi_ageout_secs * 1000 < t_now.getTime () ) {
			x_entry.markOutOfCache ();
			return null;
		}
		return x_entry.getValue ();
	}
	
	/**
	 * Flush the entry associated with the given key out of the cache
	 */
    @Override
	public synchronized V remove ( K x_key ) {
		CacheEntry x_entry = ov_cache.remove ( x_key );
		
		if ( null != x_entry ) {
			x_entry.markOutOfCache ();
			ov_entries.remove ( x_entry );
			return x_entry.getValue ();
		}
		return (V) null;
	}
	

    @Override
	public synchronized void clear () {
		ov_cache.clear ();
		ov_entries.clear ();
	}
	
    @Override
	public synchronized Map<K,V> cacheContents ()
	{
		Map<K,V> v_copy = new HashMap<K,V> ();
		for ( CacheEntry x_entry: ov_cache.values () ) {
			v_copy.put ( x_entry.getKey (), x_entry.getValue () );
		}
		return v_copy;
	}
	
    @Override
	public Cache.Policy getPolicy () { return Cache.Policy.FIFO; }

	
    @Override
	public synchronized boolean isEmpty () { return ov_cache.isEmpty (); }

    @Override
	public synchronized int size () { return ov_cache.size (); }

}


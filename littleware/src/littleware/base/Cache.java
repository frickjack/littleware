package littleware.base;

import java.util.Map;

/**
 * Simple cache interface.
 * Implementations are required to be synchronized
 * for access by multiple threads.
 */
public interface Cache<K,V> {
	/**
	 * Some common cache policies <br />
	 *    - LFU is least frequently used <br />
	 *    - UNLIMITED cache grows unbound if not flushed <br />
	 *    - OTHER is a catch-all
	 */
	public enum Policy { OTHER, FIFO, LRU, LFU, UNLIMITED };
	
	
	/**
	 * Get the cache policy in effect.
	 */
	public Policy getPolicy ();
	
	/**
	 * Get the maximum size of the cache before old entries
	 * start getting flused out.
	 *
	 * @return max size, -1 indicates no max
	 */
	public int getMaxSize ();
	
	/**
	 * Get the maximum age in seconds of a cache-entry
	 * before the entry is automatically cleared from the cache.
	 *
	 * @return max age in secs, -1 indicates no maximum age
	 */
	public int getMaxEntryAgeSecs ();
	
	/**
	 * Put an item in the cache
	 *
	 * @param x_key must be hashable and comparable
	 * @param x_value - may be null
	 * @return previous value associated with key, null if none
	 */
	public V put ( K x_key, V x_value );
	
	/**
	 * Retrieve the value associated with the given key,
	 * or null if non value registered
	 */
	public V get ( K x_key );
	
	/**
	 * Remove the specified entry from the cache
	 *
	 * @param x_key to erase
	 * @return value formerly associated with key, or null
	 */
	public V remove ( K x_key );
	
	/**
	 * Flush every item out of the cache
	 */
	public void clear ();
	
	/**
	 * Return the number of elements currently active in the cache
	 */
	public int size ();
	
	/**
	 * Return true if the cache is empty, false otherwise
	 */
	public boolean isEmpty ();
	
	/**
	 * Get a copy of the current active contents of the cache.
	 * Intended for debugging support.
	 * The result is not updated as the cache changes.
	 *
	 * @return a copy of the currently active contents of the cache -
	 *     may be read-only depending on implementation.
	 */
	public Map<K,V> cacheContents ();
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


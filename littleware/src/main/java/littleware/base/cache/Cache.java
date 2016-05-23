/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.cache;

import java.util.Map;

/**
 * Simple cache interface.
 * Implementations are required to be synchronized
 * for access by multiple threads.
 */
public interface Cache<K, V> {

    /**
     * Some common cache policies <br />
     *    - LFU is least frequently used <br />
     *    - UNLIMITED cache grows unbound if not flushed <br />
     *    - OTHER is a catch-all
     */
    public enum Policy {

        OTHER, FIFO, LRU, LFU, UNLIMITED
    };

    /**
     * Get the cache policy in effect.
     */
    public Policy getPolicy();

    /**
     * Maximum size of the cache before old entries
     * start getting flused out.  Resetting this value
     * may trigger a cache clear() depending on implementation.
     *
     * @return max size
     */
    public int getMaxSize();

    /**
     * Get the maximum age in seconds of a cache-entry
     * before the entry is automatically cleared from the cache.
     * Resetting this value
     * may trigger a cache clear() depending on implementation.
     * @return max age in secs
     */
    public int getMaxEntryAgeSecs();

    /**
     * Put an item in the cache
     *
     * @param x_key must be hashable and comparable
     * @param x_value - may be null
     * @return previous value associated with key, null if none
     */
    public V put(K x_key, V x_value);

    /**
     * Retrieve the value associated with the given key,
     * or null if non value registered
     */
    public V get(K x_key);

    /**
     * Remove the specified entry from the cache
     *
     * @param x_key to erase
     * @return value formerly associated with key, or null
     */
    public V remove(K x_key);

    /**
     * Flush every item out of the cache
     */
    public void clear();

    /**
     * Return the number of elements currently active in the cache
     */
    public int size();

    /**
     * Return true if the cache is empty, false otherwise
     */
    public boolean isEmpty();

    /**
     * Get a copy of the current active contents of the cache.
     * Intended for debugging support.
     * The result is not updated as the cache changes.
     *
     * @return a copy of the currently active contents of the cache -
     *     may be read-only depending on implementation.
     */
    public Map<K, V> cacheContents();

    public interface Builder {
        public void setMaxSize( int value );
        public int  getMaxSize();
        public Builder maxSize( int value );

        public void setMaxAgeSecs( int value );
        public int getMaxAgeSecs();
        public Builder maxAgeSecs( int value );

        public <K,V> Cache<K,V> build();
    }
}

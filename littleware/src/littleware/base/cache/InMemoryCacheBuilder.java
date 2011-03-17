/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.cache;

import com.google.common.collect.ImmutableMap;
import java.util.Date;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

public class InMemoryCacheBuilder extends AbstractCacheBuilder {


    @Override
    public <K, V> Cache<K, V> build() {
        return new InMemoryCache<K, V>(getMaxAgeSecs(), getMaxSize());
    }

    // --------------------------------------------------
    /**
     * Simple Cache implementation using FIFO replacement policy.
     * Methods are synchronized internally.
     */
    private static class InMemoryCache<K, V> implements Cache<K, V> {

        private static final Logger log = Logger.getLogger(InMemoryCache.class.getName());
        private final int oi_ageout_secs;
        private final int oi_size;
        private final Map<K, CacheEntry> entryMap = new HashMap<K, CacheEntry>();
        // List of CacheEntry (below) objects
        private final List<CacheEntry> entryList = new LinkedList<CacheEntry>();

        /**
         * Internal data bucket
         */
        private class CacheEntry {

            private final Date ot_created = new Date();
            private final K ox_key;
            private final V ox_value;

            /**
             * Constructor sets internal key/value,
             * creation date, and in_cache to true
             */
            public CacheEntry(K x_key, V x_value) {
                ox_key = x_key;
                ox_value = x_value;
            }

            public Date getCreateDate() {
                return ot_created;
            }

            public K getKey() {
                return ox_key;
            }

            public V getValue() {
                return ox_value;
            }

            /** Equals operation only true if exactly the same object */
            @Override
            public boolean equals(Object x_other) {
                return ((null != x_other) && (x_other instanceof InMemoryCache.CacheEntry) && ox_key.equals(((CacheEntry) x_other).ox_key));
            }

            @Override
            public int hashCode() {
                return ox_key.hashCode();
            }
        }


        /**
         * Constructor sets user supplied values for maxsize and timeout.
         * Illegal values are silently converted to legal range.
         *
         * @param i_ageout_secs age at which an entry is automatically
         *               aged out of the cache - must be at least MIN_AGEOUT_SECS
         * @param i_size of the chache - FIFO replacement policy kicks in
         *                      - must be at lest MIN_SIZE
         */
        public InMemoryCache(int i_ageout_secs, int i_size) {
            oi_ageout_secs = i_ageout_secs;
            oi_size = i_size;
        }

        @Override
        public int getMaxEntryAgeSecs() {
            return oi_ageout_secs;
        }


        @Override
        public int getMaxSize() {
            return oi_size;
        }


        /**
         * Insert the given value into the cache under the given key.
         * Age out members as necessary.
         *
         * @param x_key
         * @param x_value
         * @return null or previous value registered with key
         */
        @Override
        public synchronized V put(K x_key, V x_value) {
            final CacheEntry entry = new CacheEntry(x_key, x_value);
            final CacheEntry oldEntry = entryMap.put(x_key, entry);

            if (null != oldEntry) {
                entryList.remove(oldEntry);
                entryList.add(entry);
                return oldEntry.getValue();
            }
            entryList.add(entry);
            // Just added an entry, need to make sure we haven't overflowed the cache
            for (int i_size = entryList.size();
                    i_size > oi_size;
                    --i_size) {
                final CacheEntry removeEntry = (CacheEntry) entryList.remove(0);
                entryMap.remove(removeEntry.getKey());
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
        public synchronized V get(K key) {
            final CacheEntry entry = entryMap.get(key);

            if (null == entry) {
                return null;
            }
            final Date t_now = new Date();
            if (entry.getCreateDate().getTime() + oi_ageout_secs * 1000 < t_now.getTime()) {
                remove(key);
                return null;
            }
            return entry.getValue();
        }

        /**
         * Flush the entry associated with the given key out of the cache
         */
        @Override
        public synchronized V remove(K key) {
            final CacheEntry entry = entryMap.remove(key);

            if (null != entry) {
                entryList.remove(entry);
                return entry.getValue();
            }
            return (V) null;
        }

        @Override
        public synchronized void clear() {
            entryMap.clear();
            entryList.clear();
        }

        @Override
        public synchronized Map<K, V> cacheContents() {
            final ImmutableMap.Builder<K, V> builder = ImmutableMap.builder();
            for (CacheEntry entry : entryMap.values()) {
                builder.put(entry.getKey(), entry.getValue());
            }

            return builder.build();
        }

        @Override
        public Cache.Policy getPolicy() {
            return Cache.Policy.FIFO;
        }

        @Override
        public synchronized boolean isEmpty() {
            return entryMap.isEmpty();
        }

        @Override
        public synchronized int size() {
            return entryMap.size();
        }
    }
}

/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.cache;

import java.util.Collections;
import java.util.Map;
import littleware.base.cache.Cache.Policy;

public class NullCacheBuilder extends AbstractCacheBuilder {

    @Override
    public <K, V> Cache<K, V> build() {
        return new NullCache<K, V>(getMaxAgeSecs(), getMaxSize());
    }

    /**
     * Cache interface implementation doesn't do anything
     */
    public static class NullCache<K, V> implements Cache<K, V>, java.io.Serializable {
        private int maxSize;
        private int maxAgeSecs;

        public NullCache(int maxAgeSecs, int maxSize) {
            this.maxAgeSecs = maxAgeSecs;
            this.maxSize = maxSize;
        }

        @Override
        public Policy getPolicy() {
            return Cache.Policy.LRU;
        }


        @Override
        public int getMaxSize() {
            return maxSize;
        }


        @Override
        public int getMaxEntryAgeSecs() {
            return maxAgeSecs;
        }

        @Override
        public V put(K x_key, V x_value) {
            return null;
        }

        @Override
        public V get(K x_key) {
            return null;
        }

        @Override
        public V remove(K x_key) {
            return null;
        }

        @Override
        public void clear() {
        }

        @Override
        public int size() {
            return 0;
        }

        @Override
        public boolean isEmpty() {
            return true;
        }

        @Override
        public Map<K, V> cacheContents() {
            return Collections.emptyMap();
        }
    }
}

/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.base;

import java.util.HashMap;
import java.util.Map;
import littleware.base.Cache.Policy;

/**
 * Cache interface implementation doesn't do anything
 */
public class NullCache<K,V> implements Cache<K,V>, java.io.Serializable {

    @Override
    public Policy getPolicy() {
        return Cache.Policy.LRU;
    }

    private int maxSize = 0;

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public void setMaxSize(int iSize) {
        if ( iSize < 0 ) {
            maxSize = 0;
        } else {
            maxSize = iSize;
        }
    }

    private int maxAgeSec = 0;

    @Override
    public int getMaxEntryAgeSecs() {
        return maxAgeSec;
    }

    @Override
    public void setMaxEntryAgeSecs(int iSecs) {
        if ( maxAgeSec < 0 ) {
            maxAgeSec = 0;
        } else {
            maxAgeSec = 0;
        }
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
        return new HashMap<K,V>();
    }

}

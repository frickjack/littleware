/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.client;

import com.google.inject.Inject;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.base.cache.Cache;
import littleware.base.cache.NullCacheBuilder;

/**
 * NOOP ClientCache
 */
public class NullClientCache implements ClientCache, java.io.Serializable {
    private static final long serialVersionUID = 42234L;
    private Cache<String,Object> cache;

    @Inject
    public NullClientCache( NullCacheBuilder cacheBuilder ) {
        this.cache = cacheBuilder.build();
    }

    @Override
    public long getTransaction() {
        return 0L;
    }


    @Override
    public Cache<String, Object> getCache() {
        return cache;
    }

    @Override
    public Asset put(Asset asset) {
        return null;
    }

    @Override
    public Asset get(UUID uId) {
        return null;
    }

    @Override
    public Object putLongTerm(String key, Object value) {
        return null;
    }

}

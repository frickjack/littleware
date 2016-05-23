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

import littleware.base.validate.ValidatorUtil;

/**
 * Little base class for Cache.Builder implementations
 */
public abstract class AbstractCacheBuilder implements Cache.Builder {
    private int maxSize = 10000;
    private int maxAgeSecs = 300;
    private final int maxMaxSize;
    private final int maxMaxAgeSecs;

    /**
     * Sets up infinite cache
     */
    protected AbstractCacheBuilder() {
        maxMaxSize = -1;
        maxMaxAgeSecs = -1;
    }

    /**
     * Specify limits on cache size and item age
     *
     * @param maxMaxSize
     * @param maxMaxAgeSecs
     */
    protected AbstractCacheBuilder( int maxMaxSize, int maxMaxAgeSecs ) {
        this.maxMaxSize = maxMaxSize;
        this.maxMaxAgeSecs = maxMaxAgeSecs;
    }

    @Override
    public final void setMaxSize(int value) {
        ValidatorUtil.check((maxMaxSize == -1) || ((value > 0) && (value <= maxMaxSize)),
                "maxSize value exceeds limit: " + maxMaxSize
                );
        maxSize = value;
    }

    @Override
    public final int getMaxSize() {
        return maxSize;
    }

    @Override
    public Cache.Builder maxSize(int value) {
        setMaxSize(value);
        return this;
    }


    @Override
    public final void setMaxAgeSecs(int value) {
        ValidatorUtil.check( (maxMaxAgeSecs == -1) || ((value > 0) && (value <= maxMaxAgeSecs)),
                "maxAgeSecs exceeds limit: " + maxMaxAgeSecs
                );
        maxAgeSecs = value;
    }

    @Override
    public final int getMaxAgeSecs() {
        return maxAgeSecs;
    }

    @Override
    public Cache.Builder maxAgeSecs(int value) {
        setMaxAgeSecs(value);
        return this;
    }

}

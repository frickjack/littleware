/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.cache;

import java.io.Serializable;
import java.util.UUID;

/**
 * Simple base implementation of CacheableObject
 */
public abstract class AbstractCacheableObject implements CacheableObject, Serializable {

    private UUID id;
    private long transaction;

    /**
     * For serialization support
     */
    protected AbstractCacheableObject() {
    }

    /**
     * Initialize id and transaction count
     */
    protected AbstractCacheableObject(UUID id, long transaction) {
        this.id = id;
        this.transaction = transaction;
    }


    /**
     * Get the object id
     */
    @Override
    public UUID getId() {
        return this.id;
    }

    /**
     * Get the transaction count
     */
    @Override
    public long getTimestamp() {
        return transaction;
    }


    @Override
    public boolean equals(Object x_other) {
        if ((null == this.id) || (null == x_other) || (!(x_other instanceof CacheableObject)) || (null == ((CacheableObject) x_other).getId())) {
            return false;
        }
        return this.id.equals(((CacheableObject) x_other).getId());
    }

    /**
     * Just return this.getObjectId ().hashCode ()
     */
    @Override
    public int hashCode() {
        return this.getId().hashCode();
    }
}


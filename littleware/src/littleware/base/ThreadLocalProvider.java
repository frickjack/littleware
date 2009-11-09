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

import com.google.inject.Provider;
import com.google.inject.Singleton;

/**
 * Simple ThreadLocalProvider base class.
 * Allocate the provider as a singleton.
 * The provider keeps an internal thread-local variable
 * initialized by the subtype provided build() method.
 */
@Singleton
public abstract class ThreadLocalProvider<T> implements Provider<T> {
    private final ThreadLocal<T>  othreadCache = new ThreadLocal<T>() {
        @Override
		protected T initialValue() {
			return build();
		}
	};

    /**
     * NOTE: SimpleLittleTransaction.setDataSource must be called  before the
     *      first call here.
     */
    @Override
    public T get () {
        return othreadCache.get ();
    }

    /**
     * Subtype needs to allocate a new object for us
     * 
     * @return new object to cache in thread scope
     */
    protected abstract T build();
}

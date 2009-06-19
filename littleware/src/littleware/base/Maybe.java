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

import java.util.NoSuchElementException;
import java.util.concurrent.Callable;


/**
 * A little set/not-set object.
 * Fascilitates deferred loading and other patterns.
 */
public class Maybe<T> implements java.io.Serializable {
    private static final long serialVersionUID = 10000001L;

    private boolean  ob_set = false;
    public boolean isSet() { return ob_set; }

    /** Construct an unset Maybe */
    public Maybe () {}
    /** Construct an isSet Maybe */
    public Maybe ( T val ) {
        oval = val;
        ob_set = true;
    }

    private T  oval;

    public T getOr( T alt ) {
        if ( ob_set ) {
            return oval;
        } else {
            return alt;
        }
    }

    public T getOrCall( Callable<T> call ) throws Exception {
        if ( ob_set ) {
            return oval;
        } else {
            return call.call ();
        }
    }

    /**
     * Get the value if set, otherwise throw NoSuchElementException
     */
    public T get () {
        if ( ! ob_set ) {
            throw new NoSuchElementException();
        }
        return oval;
    }

    @Override
    public boolean equals( final Object other ) {
        if ( other instanceof Maybe ) {
            final Maybe<?> maybe = (Maybe<?>) other;
            return (isSet() == maybe.isSet()) &&
                    (isSet() ? get().equals( maybe.get() ) : true);
        } else {
            return isSet() && get().equals( other );
        }
    }

    @Override
    public int hashCode() {
        int hash = 3;
        hash = 37 * hash + (this.ob_set ? 1 : 0);
        hash = 37 * hash + (this.oval != null ? this.oval.hashCode() : 0);
        return hash;
    }
}

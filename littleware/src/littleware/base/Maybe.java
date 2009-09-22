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
import littleware.asset.Asset;


/**
 * A little set/not-set object.
 * Fascilitates deferred loading and other patterns.
 */
public class Maybe<T> implements java.io.Serializable {
    private static final long serialVersionUID = 10000001L;

    private boolean  ob_set = false;
    private String osError = null;

    public boolean isSet() { return ob_set; }
    public boolean isEmpty() { return ! ob_set; }

    /** Construct an unset Maybe */
    private Maybe () {}
    /** Construct an isSet Maybe */
    private Maybe ( T val ) {
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
            if ( null != osError ) {
                throw new NoSuchElementException( osError );
            }
            throw new NoSuchElementException();
        }
        return oval;
    }

    /**
     * Just calls get() - setup as Property to simplify access
     * from JSF/JSP expression language, etc.
     */
    public T getThing() {
        return get();
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

    /**
     * Internal setter for error message property
     * attached to NoSuchElementException on get() call
     * against unset option.
     *
     * @param sError
     * @return this
     */
    private Maybe<T> putError( String sError ) {
        osError = sError;
        return this;
    }

    @Override
    public String toString () {
        return isSet() ? get().toString() : "null";
    }

    /** Factory for unset Maybe */
    public static <T> Maybe<T> empty() { return new Maybe<T>(); }
    public static <T> Maybe<T> empty( Class<T> type ) { return new Maybe<T>(); }

    /**
     * Factory method for unset Maybe
     *
     * @param sError message to attach to the NoSuchElementException if
     *     the client invokes get
     */
    public static <T> Maybe<T> empty( String sError ) {
        return new Maybe<T>().putError( sError );
    }
    public static <T> Maybe<T> empty( Class<T> type, String sError ) {
        return new Maybe<T>().putError( sError );
    }

    /** Maybe factory set if val is not null */
    public static <T> Maybe<T> emptyIfNull( T val ) {
        if ( null == val ) {
            return new Maybe<T>();
        } else {
            return new Maybe<T>( val );
        }
    }
    /** Factory builds Maybe set with val */
    public static <T> Maybe<T> something( T val ) {
        return new Maybe<T>( val );
    }


}

/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;



/**
 * A little set/not-set object.
 * Facilitates deferred loading and other patterns.
 */
public final class Maybe<T> {
    private static class SimpleOption<T> extends AbstractReference<T> implements Option<T> {
        public SimpleOption( T value ) {
            super();
        }
        public SimpleOption() {}
    }
    
    private static final long serialVersionUID = 10000001L;


    /** Factory for unset Maybe */
    public static <T> Option<T> empty() { return new SimpleOption<T>(); }
    public static <T> Option<T> empty( Class<T> type ) { return new SimpleOption<T>(); }

    /**
     * Factory method for unset Maybe
     *
     * @param sError message to attach to the NoSuchElementException if
     *     the client invokes get
     */
    public static <T> Option<T> empty( String sError ) {
        final SimpleOption<T> result = new SimpleOption<T>().putError( sError );
        return result;
    }
    public static <T> Option<T> empty( Class<T> type, String sError ) {
        final SimpleOption<T> result = new SimpleOption<T>().putError( sError );
        return result;
    }

    /** Maybe factory set if val is not null */
    public static <T> Option<T> emptyIfNull( T val ) {
        if ( null == val ) {
            return new SimpleOption<T>();
        } else {
            return new SimpleOption<T>( val );
        }
    }

    public static <T> Option<T> emptyIfNull( Class<T> clazz, T val ) {
        if ( null == val ) {
            return new SimpleOption<T>();
        } else {
            return new SimpleOption<T>( val );
        }
    }

    private static Option<String> EmptyString = new SimpleOption<String>();

    /** emptyIfNull( val ) || val.trim().length == 0 */
    public static Option<String> emptyString( String val ) {
        if ( (null == val) || (0 == val.trim().length()) ) {
            return EmptyString;
        } else {
            return new SimpleOption<String>( val );
        }
    }
    /** Factory builds Maybe set with val */
    public static <T> Option<T> something( T val ) {
        return new SimpleOption<T>( val );
    }



}

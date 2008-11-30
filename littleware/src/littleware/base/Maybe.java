package edu.auburn.library.util;

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
}

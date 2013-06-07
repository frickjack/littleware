/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

import java.util.concurrent.Callable;

/**
 * Option interface based on scala.Option.
 * Usually used in conjuction with the static methods in littleware.base.Maybe
 */
public interface Option<T> extends Iterable<T> {
    boolean isSet();
    boolean isEmpty();
    /** Alias for isSet */
    boolean nonEmpty();
    T getOr( T alt );
    T getOrCall( Callable<T> call ) throws Exception;
    T getOrThrow( RuntimeException ex );
    T getOrThrow( Exception ex ) throws Exception;
    
    /**
     * Get the value if set, otherwise throw NoSuchElementException
     */
    T get ();
    /**
     * Just calls get() - setup as Property to simplify access
     * from JSF/JSP expression language, etc.
     */
    T getRef();


    
    /**
     * Either return this if Filter.accept == true, or EMPTY
     */
    Option<T> filter( Filter<? super T> filter );
}

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
 * Option interface based on scala.Option
 */
public interface Option<T> extends Iterable<T> {
    public boolean isSet();
    public boolean isEmpty();
    public T getOr( T alt );
    public T getOrCall( Callable<T> call ) throws Exception;
    /**
     * Get the value if set, otherwise throw NoSuchElementException
     */
    public T get ();
    /**
     * Just calls get() - setup as Property to simplify access
     * from JSF/JSP expression language, etc.
     */
    public T getThing();
}

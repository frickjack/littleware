/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.base;


import java.util.concurrent.Callable;
import littleware.base.event.LittleBean;

/**
 * Reference to a (usually immutable) object where the
 * reference object might be changed by some underlying
 * engine (like a cache) notifying property-change listeners.
 * Basically a mutable Option.
 * The underlying system may change the thing referenced
 * when a new version of the thing becomes available firing
 * a PropertyChangeEvent.
 */
public interface LittleReference<T> extends LittleBean {
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
    public T getRef();

    /**
     * Update the thing referenced if value.getTimestamp > thing.getTimestamp,
     * or clear thing if value is null
     *
     * @return this
     */
    public <R extends LittleReference<T>> R updateRef( T value );
    /**
     * isSet becomes false
     */
    public void clear();
}

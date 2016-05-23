/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap;

import com.google.inject.Injector;

/**
 * A little wrapper around a session's Guice injector - which is 
 * a child of the application's root injector.
 * Inject this class to avoid ambiguous Injector injection.
 * Note: as usual - avoid accessing the injector in the application - this
 * is just here to support corner cases.
 */
public interface SessionInjector {
    public Injector getInjector();
    /**
     * Shortcut for getInjector().getInstance() ...
     */
    public <T> T getInstance( Class<T> clazz );
    
    /**
     * Shortcut for getInjector().injectMembers( injectMe ); return injectMe;
     */
    public <T>  T  injectMembers( T injectMe );
}

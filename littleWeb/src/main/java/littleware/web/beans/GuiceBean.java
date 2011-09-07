/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.beans;

import com.google.inject.Inject;
import com.google.inject.Injector;
import littleware.base.Maybe;
import littleware.base.Option;

/**
 * Attempt to marry littleware world with
 * web jsp/jsf servlet world.
 */
public class GuiceBean implements java.io.Serializable {
    private transient Option<Injector> maybeInjector;

    /**
     * Just here for deserialization ...
     */
    public GuiceBean() {
        this.maybeInjector = Maybe.empty();
    }
    
    
    /**
     * Inject the injector with which GuiceBean.injectMe
     * injects other beans.
     */
    @Inject
    public GuiceBean( Injector injector ) {
        this.maybeInjector = Maybe.something( injector );
    }


    /**
     * setInjector throws IllegalStateException if GuiceBean already has an injector
     */
    public void setInjector( Injector value ) {
        if ( maybeInjector.isSet() ) {
            throw new IllegalStateException( "Attemp to reset GuiceBean injector" );
        }
        maybeInjector = Maybe.something( value );
    }
    
    public Option<Injector> getInjector() {
        return maybeInjector;
    }
    
    /**
     * Inject members into the given object with the
     * internal Guice injector if getHasInjector is true, otherwise NOOP.
     * NOOP should only occur after a session-restore from storage, and
     * an application filter should go through and restore the active session,
     * and re-inject the session beans ... bla.
     *
     * @param injectMe needs members injected by Guice
     * @throws IllegalStateException if injector not initialized
     */
    public <T> T injectMembers( T injectMe ) {
        if ( maybeInjector.isSet() ) {
            maybeInjector.get().injectMembers(injectMe);
        }
        return injectMe;
    }
    
}

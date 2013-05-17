/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.beans;

import com.google.inject.Inject;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.bootstrap.SessionInjector;

/**
 * Attempt to marry littleware world with
 * web jsp/jsf servlet world.
 * The servlet.login.LoginFilter injects a session-scoped GuiceBean
 * into each request with attribute WebBootstrap.littleGuice,
 * and servlet.AppBootstrapListener injects an application-scoped
 * injector into the servlet context with the same attribute.
 * Avoid injecting into session as this bean doesn't 
 * deserialize into a valid state.
 */
public class GuiceBean implements java.io.Serializable {
    private transient Option<SessionInjector> maybeInjector;

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
    public GuiceBean( SessionInjector sessionInjector ) {
        this.maybeInjector = Maybe.something( sessionInjector );
    }


    /**
     * setInjector throws IllegalStateException if GuiceBean already has an injector
     */
    public void setInjector( SessionInjector value ) {
        maybeInjector = Maybe.something( value );
    }
    
    /**
     * Guice bean may be in uninitialized state if restored from
     * serialization.  This littleware injection stuff does not
     * support restore at server restart.
     */
    public Option<SessionInjector> getInjector() {
        return maybeInjector;
    }
    
    /**
     * Shortcut for getInject().get().getInstance()
     * @throws IllegalStateException if bean not properly initialized 
     */
    public final <T> T getInstance( Class<T> clazz ) {
      if( maybeInjector.isSet() ) {
        return maybeInjector.get().getInstance( clazz );
      }
      throw new IllegalStateException( "Guice bean not properly initialized" );
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

/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.beans;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;
import littleware.base.Maybe;

/**
 * Attempt to marry littleware world with
 * web jsp/jsf servlet world.
 */
@Singleton
public class GuiceBean {
    private final Maybe<Injector> maybeInjector;

    /**
     * Inject the injector with which GuiceBean.injectMe
     * injects other beans.
     */
    @Inject
    public GuiceBean( Injector injector ) {
        this.maybeInjector = Maybe.something( injector );
    }

    /**
     * Setup a GuiceBean for a web session that has
     * not logged into littleware.  The injectMe method
     * acts as a NOOP, and loggedIn is false.
     */
    public GuiceBean () {
        this.maybeInjector = Maybe.empty();
    }

    /**
     * Return true if a user session is logged in,
     * false if no littleware login is active.
     */
    public boolean isLoggedIn() {
        return maybeInjector.isSet();
    }
    

    /**
     * Inject members into the given object with the
     * internal Guice injector.
     * If the Guice injector is not initialized,
     * then append the object to a list to inject
     * later if possible.
     *
     * @param injectMe needs members injected by Guice
     * @exception IllegalStateException if injector not initialized
     */
    public void injectMembers( Object injectMe ) {
        if ( maybeInjector.isEmpty() ) {
            return;
        }
        maybeInjector.get().injectMembers(injectMe);
    }

}

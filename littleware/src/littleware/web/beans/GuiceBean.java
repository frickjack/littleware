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

/**
 * Attempt to marry littleware world with
 * web jsp/jsf servlet world.
 */
public class GuiceBean {

    /**
     * Return true if a user session is logged in,
     * false if no littleware login is active.
     */
    public boolean isLoggedIn() {
        return false;
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
    }
}

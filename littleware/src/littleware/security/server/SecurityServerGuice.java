/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.server;

import com.google.inject.Binder;
import com.google.inject.Module;

import com.google.inject.Scopes;
import littleware.security.*;



/**
 * Setup server-side implementations of littleware.security interfaces
 */
public class SecurityServerGuice implements Module {

    @Override
    public void configure(Binder binder) {
        binder.bind( AccountManager.class ).to( SimpleAccountManager.class ).in( Scopes.SINGLETON );
    }

}

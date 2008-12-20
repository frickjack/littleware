/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.server;

import com.google.inject.Binder;
import com.google.inject.Module;
import com.google.inject.Scopes;
import littleware.security.auth.SessionManager;
import littleware.security.auth.server.db.DbAuthManager;
import littleware.security.auth.server.db.postgres.PostgresDbAuthManager;

/**
 *
 * @author pasquini
 */
public class AuthServerGuice implements Module {

    public void configure(Binder binder) {
        binder.bind( SessionManager.class ).to( SimpleSessionManager.class ).in( Scopes.SINGLETON );
        binder.bind( DbAuthManager.class ).to( PostgresDbAuthManager.class ).in( Scopes.SINGLETON );
        binder.bind( ServiceProviderRegistry.class ).to( SimpleServiceProviderRegistry.class ).in( Scopes.SINGLETON );
    }

}

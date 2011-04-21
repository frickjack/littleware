/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server.internal;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.security.AccessController;
import java.util.Set;
import javax.security.auth.Subject;
import littleware.base.AssertionFailedException;
import littleware.base.UUIDFactory;
import littleware.security.LittleUser;
import littleware.security.auth.SessionManager;
import littleware.security.auth.server.ServiceRegistry;

/**
 * Bind SessionManager, ServiceProvideRegistery, and LittleUser
 * for server-side access.
 */
public class AuthServerGuice implements Module {

    public static class UserProvider implements Provider<LittleUser> {
        private final LittleUser testUser;

        @Inject
        public UserProvider(Provider<LittleUser.Builder> userProvider) {
            this.testUser = userProvider.get().id(UUIDFactory.parseUUID("7AC5D21049254265B224B7512EFCF0D1")).
                    name("littleware.test_user").
                    parentId(UUIDFactory.parseUUID("BD46E5588F9D4F41A6310100FE68DCB4")).
                    homeId(UUIDFactory.parseUUID("BD46E5588F9D4F41A6310100FE68DCB4")).
                    build();
        }

        @Override
        public LittleUser get() {
            final Subject subject = Subject.getSubject(AccessController.getContext());
            if (null == subject) {
                return testUser;
            }
            final Set<LittleUser> userSet = subject.getPrincipals(LittleUser.class);
            if (userSet.isEmpty()) {
                return testUser;
            }
            if (userSet.size() > 1) {
                throw new AssertionFailedException("What the frick ?");
            }
            return userSet.iterator().next();
        }
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(SessionManager.class).to(SimpleSessionManager.class).in(Scopes.SINGLETON);
        //binder.bind( DbAuthManager.class ).to( PostgresDbAuthManager.class ).in( Scopes.SINGLETON );
        binder.bind(ServiceRegistry.class).to(SimpleServiceRegistry.class).in(Scopes.SINGLETON);
        binder.bind(LittleUser.class).toProvider(UserProvider.class).in(Scopes.SINGLETON);
        binder.bind(UserProvider.class).in(Scopes.SINGLETON);
    }
}

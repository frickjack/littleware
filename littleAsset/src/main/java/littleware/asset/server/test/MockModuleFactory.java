/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.test;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerProfile;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.base.AssertionFailedException;
import littleware.security.auth.LittleSession;
import littleware.security.auth.internal.RemoteSessionManager;

/**
 * Setup bindings for the Mock interfaces required to run
 * the client-side test cases directly against the server-side service implementations.
 */
public class MockModuleFactory implements ServerModuleFactory {

    public static class MockModule extends AbstractServerModule {

        public MockModule(ServerProfile profile) {
            super(profile);
        }

        @Provides
        @Singleton
        public LittleSession testSessionProvider(RemoteSessionManager sessionMgr) {
            try {
                return sessionMgr.login("littleware.test_user", "bla", "setup server test");
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new AssertionFailedException("Failed to setup test session", ex);
            }
        }

        @Override
        public void configure(Binder binder) {
            //binder.bind( AssetManager.class ).to( MockAssetManager.class ).in( Scopes.SINGLETON );
            //binder.bind( AssetSearchManager.class ).to( MockSearchManager.class ).in( Scopes.SINGLETON );
            //binder.bind(LittleSession.class).in(Scopes.SINGLETON);
        }
    }

    @Override
    public ServerModule build(ServerProfile profile) {
        return new MockModule(profile);
    }
}

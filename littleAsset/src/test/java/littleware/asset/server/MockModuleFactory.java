package littleware.asset.server;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppModuleFactory;
import littleware.security.auth.LittleSession;
import littleware.security.auth.internal.RemoteSessionManager;

/**
 * Setup bindings for the Mock interfaces required to run
 * the client-side test cases directly against the server-side service implementations.
 */
public class MockModuleFactory implements AppModuleFactory {

    public static class MockModule extends AbstractServerModule {

        public MockModule(AppBootstrap.AppProfile profile) {
            super(profile);
        }

        @Provides
        @Singleton
        public LittleSession testSessionProvider(RemoteSessionManager sessionMgr) {
            try {
                return sessionMgr.login(AbstractAssetTest.getTestUserName(), "bla", "setup server test");
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                throw new IllegalStateException("Failed to setup test session", ex);
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
    public ServerModule build(AppBootstrap.AppProfile profile) {
        return new MockModule(profile);
    }
}

package littleware.asset.client.bootstrap;

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.Configuration;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetTreeTool;
import littleware.asset.client.ClientScannerFactory;
import littleware.asset.client.internal.SimpleAssetLibrary;
import littleware.asset.client.internal.SimpleAssetManagerService;
import littleware.asset.client.internal.SimpleAssetTreeTool;
import littleware.asset.client.internal.SimpleClientCache;
import littleware.asset.client.internal.SimpleScannerFactory;
import littleware.asset.client.internal.SimpleSearchService;
import littleware.asset.client.internal.SimpleServiceBus;
import littleware.asset.client.spi.ClientCache;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.ClientLoginModule;
import littleware.security.auth.client.ClientLoginModule.ConfigurationBuilder;
import littleware.security.auth.client.KeyChain;
import littleware.security.auth.client.SessionManager;
import littleware.security.auth.client.internal.LoginConfigBuilder;
import littleware.security.auth.client.internal.SessionManagerProxy;
import littleware.security.auth.client.internal.SimpleKeyChain;

/**
 * Setup session-scoped bindings for asset-client code
 */
public class AssetSessionModule implements littleware.bootstrap.SessionModule {

    private final static Logger log = Logger.getLogger(AssetSessionModule.class.getName());

    private AssetSessionModule() {
    }

    @Provides
    @Singleton
    public LittleSession provideSession(KeyChain chain, AssetSearchManager search) {
        try {
            final UUID id = chain.getDefaultSessionId().get();
            log.log(Level.FINE, "Loading session: {0}", id);
            return search.getAsset(chain.getDefaultSessionId().get()).get().narrow();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load default session", ex);
        }
    }

    /** Note - late/lazy binding of LittleSession - wait till the user authenticates! */
    @Provides
    @Singleton
    public LittleUser provideUser(Provider<LittleSession> sessionProvider, AssetSearchManager search) {
        try {
            return search.getAsset(sessionProvider.get().getOwnerId()).get().narrow();
        } catch (Exception ex) {
            throw new IllegalStateException("Failed to load session owner", ex);
        }
    }

    @Override
    public Optional<Class<Runnable>> getSessionStarter() {
        return Optional.empty();
    }

    /*
    @Provides
    @Singleton
    public javax.security.auth.login.Configuration provideLoginConfig( ClientLoginModule.ConfigurationBuilder loginBuilder ) {
    return loginBuilder.build();
    }
     * 
     */
    public static class ConfigProvider implements Provider<javax.security.auth.login.Configuration> {

        private final ConfigurationBuilder builder;

        @Inject
        public ConfigProvider(ClientLoginModule.ConfigurationBuilder builder) {
            this.builder = builder;
        }

        @Override
        public Configuration get() {
            return builder.build();
        }
    }

    @Override
    public void configure(Binder binder) {
        log.log(Level.FINE, "Asset session configuration running ...");
        binder.bind(AssetSearchManager.class).to(SimpleSearchService.class).in(Scopes.SINGLETON);
        binder.bind(AssetManager.class).to(SimpleAssetManagerService.class).in(Scopes.SINGLETON);
        binder.bind(SessionManager.class).to(SessionManagerProxy.class).in(Scopes.SINGLETON);

        binder.bind(KeyChain.class).to( SimpleKeyChain.class ).in( Scopes.SINGLETON );
        binder.bind(AssetLibrary.class).to(SimpleAssetLibrary.class).in(Scopes.SINGLETON);
        binder.bind(ClientCache.class).to(SimpleClientCache.class).in(Scopes.SINGLETON);
        binder.bind(AssetTreeTool.class).to(SimpleAssetTreeTool.class).in(Scopes.SINGLETON);
        binder.bind(LittleServiceBus.class).to(SimpleServiceBus.class).in(Scopes.SINGLETON);
        binder.bind(ClientScannerFactory.class).to(SimpleScannerFactory.class).in(Scopes.SINGLETON);
        binder.bind(ClientLoginModule.ConfigurationBuilder.class).to(LoginConfigBuilder.class);
        binder.bind(Configuration.class).toProvider(ConfigProvider.class).in(Scopes.SINGLETON);
    }

    private static AssetSessionModule _singleton = new AssetSessionModule();

    public static AssetSessionModule get() {
        return _singleton;
    }

    public static class Factory implements SessionModuleFactory {

        @Override
        public SessionModule buildSessionModule(AppProfile ap) {
            return AssetSessionModule.get();
        }
    }
}
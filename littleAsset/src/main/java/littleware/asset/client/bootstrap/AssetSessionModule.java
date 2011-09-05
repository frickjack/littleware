/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.bootstrap;

import com.google.inject.Binder;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
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
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.SessionModule;
import littleware.bootstrap.SessionModuleFactory;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.ClientLoginModule;
import littleware.security.auth.client.KeyChain;
import littleware.security.auth.client.SessionManager;
import littleware.security.auth.client.internal.LoginConfigBuilder;
import littleware.security.auth.client.internal.SessionManagerProxy;
import littleware.security.auth.client.internal.SimpleKeyChain;

/**
 * Setup session-scoped bindings for asset-client code
 */
public class AssetSessionModule implements littleware.bootstrap.SessionModule {
    private final static Logger log = Logger.getLogger( AssetSessionModule.class.getName() );

    @Provides
    @Singleton
    public LittleSession provideSession(KeyChain chain, AssetSearchManager search) {
        try {
            final UUID id = chain.getDefaultSessionId().get();
            log.log(Level.FINE, "Loading session: {0}", id);
            return search.getAsset(chain.getDefaultSessionId().get()).get().narrow();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AssertionFailedException("Failed to load default session", ex);
        }
    }

    @Provides
    @Singleton
    public LittleUser provideUser(LittleSession session, AssetSearchManager search) {
        try {
            return search.getAsset(session.getOwnerId()).get().narrow();
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AssertionFailedException("Failed to load session owner", ex);
        }
    }
    
    @Provides
    @Singleton
    public javax.security.auth.login.Configuration provideLoginConfig( ClientLoginModule.ConfigurationBuilder loginBuilder ) {
        return loginBuilder.build();
    }

    @Override
    public void configure(Binder binder) {
        log.log( Level.FINE, "Asset session configuration running ..." );
        binder.bind(AssetSearchManager.class).to(SimpleSearchService.class).in(Scopes.SINGLETON);
        binder.bind(AssetManager.class).to(SimpleAssetManagerService.class).in(Scopes.SINGLETON);
        binder.bind(SessionManager.class).to(SessionManagerProxy.class).in(Scopes.SINGLETON);
        binder.bind(KeyChain.class).to(SimpleKeyChain.class).in(Scopes.SINGLETON);
        binder.bind(AssetLibrary.class).to(SimpleAssetLibrary.class).in(Scopes.SINGLETON);
        binder.bind(ClientCache.class).to(SimpleClientCache.class).in(Scopes.SINGLETON);
        binder.bind(AssetTreeTool.class).to(SimpleAssetTreeTool.class).in(Scopes.SINGLETON);
        binder.bind(LittleServiceBus.class).to(SimpleServiceBus.class).in(Scopes.SINGLETON);
        binder.bind(ClientScannerFactory.class).to(SimpleScannerFactory.class).in(Scopes.SINGLETON);
        binder.bind( ClientLoginModule.ConfigurationBuilder.class ).to( LoginConfigBuilder.class );
    }

    @Override
    public Class<? extends Runnable> getSessionStarter() {
        return SessionModule.NullStarter.class;
    }
    
    public static class Factory implements SessionModuleFactory {

        @Override
        public SessionModule build(AppProfile ap) {
            return new AssetSessionModule();
        }
        
    }
}
/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.bootstrap.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.google.inject.Scopes;
import com.google.inject.Singleton;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.client.AssetLibrary;
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetTreeTool;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.base.AssertionFailedException;
import littleware.asset.client.bootstrap.ClientBootstrap.ClientBuilder;
import littleware.asset.client.bootstrap.SessionModule;
import littleware.asset.client.bootstrap.SessionModuleFactory;
import littleware.asset.client.internal.SimpleAssetLibrary;
import littleware.asset.client.internal.SimpleAssetManagerService;
import littleware.asset.client.internal.SimpleAssetTreeTool;
import littleware.asset.client.internal.SimpleClientCache;
import littleware.asset.client.internal.SimpleSearchService;
import littleware.asset.client.internal.SimpleServiceBus;
import littleware.asset.client.spi.ClientCache;
import littleware.asset.client.spi.LittleServiceBus;
import littleware.bootstrap.AppBootstrap;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.client.KeyChain;
import littleware.security.auth.client.SessionManager;
import littleware.security.auth.client.internal.SessionManagerProxy;
import littleware.security.auth.client.internal.SimpleKeyChain;

public class SimpleClientBuilder implements ClientBootstrap.ClientBuilder {

    private static final Logger log = Logger.getLogger(SimpleClientBuilder.class.getName());
    private final List<SessionModuleFactory> sessionFactoryList = new ArrayList<SessionModuleFactory>();

    {
        for (SessionModuleFactory moduleFactory : ServiceLoader.load(SessionModuleFactory.class)) {
            sessionFactoryList.add(moduleFactory);
        }
    }
    private final AppBootstrap.AppProfile profile;
    private final Injector injector;

    @Inject
    public SimpleClientBuilder( AppBootstrap.AppProfile profile, Injector injector ) {
        this.profile = profile;
        this.injector = injector;
    }


    @Override
    public Collection<SessionModuleFactory> getSessionModuleSet() {
        return ImmutableList.copyOf(sessionFactoryList);
    }

    @Override
    public ClientBuilder addModuleFactory(SessionModuleFactory factory) {
        sessionFactoryList.add(factory);
        return this;
    }

    @Override
    public ClientBuilder removeModuleFactory(SessionModuleFactory factory) {
        sessionFactoryList.remove(factory);
        return this;
    }

    private SimpleClientBuilder copy() {
        final SimpleClientBuilder result = new SimpleClientBuilder( profile, injector );
        result.sessionFactoryList.clear();
        result.sessionFactoryList.addAll(this.sessionFactoryList);
        return result;
    }

    @Override
    public ClientBootstrap build() {
        final ImmutableList.Builder<SessionModule> sessionBuilder = ImmutableList.builder();
        for (SessionModuleFactory factory : sessionFactoryList) {
            sessionBuilder.add(factory.build(profile));
        }
        return new Bootstrap( sessionBuilder.build(), injector);
    }

    //---------------------------------------------------
    private static class Bootstrap implements ClientBootstrap {
        private final Collection<SessionModule> sessionModuleSet;
        private final Injector injector;

        public Bootstrap(
                ImmutableList<SessionModule> sessionModuleSet,
                Injector injector) {
            this.injector = injector;
            this.sessionModuleSet = sessionModuleSet;
        }



        @Override
        public Collection<SessionModule> getSessionModuleSet() {
            return sessionModuleSet;
        }


        @Override
        public <T> T startSession(Class<T> clazz) {
            final ImmutableList.Builder<SessionModule> builder = ImmutableList.builder();
            builder.addAll(this.sessionModuleSet);
            builder.add(new SessionSetupModule( this ));
            return injector.createChildInjector(builder.build()).getInstance(clazz);
        }

        @Override
        public <T> T startTestSession(Class<T> clazz) {
            final Injector child = startSession(Injector.class);
            // login as test user
            final SessionManager sessionMgr = child.getInstance(SessionManager.class);
            try {
                sessionMgr.login("littleware.test_user", "test123", "running test");
            } catch (RuntimeException ex) {
                throw ex;
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to authenticate as test user", ex);
                throw new AssertionFailedException("Failed to authenticate as littleware.test_user", ex);
            }
            return child.getInstance(clazz);
        }
    }

    //-------------------------------------------------------------
    public static class SessionSetupModule implements SessionModule {
        private final ClientBootstrap boot;
        public SessionSetupModule( ClientBootstrap boot ) {
            this.boot = boot;
        }

        @Provides
        @Singleton
        public LittleSession provideSession(KeyChain chain, AssetSearchManager search) {
            try {
                final UUID id = chain.getDefaultSessionId().get();
                log.log( Level.FINE, "Loading session: {0}", id);
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

        @Override
        public void configure(Binder binder) {
            binder.bind(AssetSearchManager.class).to(SimpleSearchService.class).in(Scopes.SINGLETON);
            binder.bind(AssetManager.class).to(SimpleAssetManagerService.class).in(Scopes.SINGLETON);
            binder.bind(SessionManager.class).to(SessionManagerProxy.class).in(Scopes.SINGLETON);
            binder.bind( KeyChain.class ).to( SimpleKeyChain.class ).in( Scopes.SINGLETON );
            binder.bind( AssetLibrary.class ).to( SimpleAssetLibrary.class ).in( Scopes.SINGLETON );
            binder.bind( ClientCache.class ).to( SimpleClientCache.class ).in( Scopes.SINGLETON );
            binder.bind( AssetTreeTool.class ).to( SimpleAssetTreeTool.class ).in( Scopes.SINGLETON );
            binder.bind( LittleServiceBus.class ).to( SimpleServiceBus.class ).in( Scopes.SINGLETON );
            binder.bind( ClientBootstrap.class ).toInstance(boot);
        }
    }
}

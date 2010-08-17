/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.client;

import com.google.common.collect.ImmutableList;
import com.google.inject.Binder;
import com.google.inject.Provider;
import com.google.inject.name.Names;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.ServiceLoader;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetManager;
import littleware.asset.AssetRetriever;
import littleware.asset.AssetSearchManager;
import littleware.asset.client.AssetManagerService;
import littleware.asset.client.AssetSearchService;
import littleware.asset.client.LittleService;
import littleware.base.AssertionFailedException;
import littleware.bootstrap.AbstractLittleBootstrap;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.client.ClientBootstrap.ClientBuilder;
import littleware.bootstrap.client.AppBootstrap.AppProfile;
import littleware.security.AccountManager;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.client.AccountManagerService;

public class SimpleClientBuilder implements ClientBootstrap.ClientBuilder {

    private static final Logger log = Logger.getLogger(SimpleClientBuilder.class.getName());
    private final List<AppModuleFactory> factoryList = new ArrayList<AppModuleFactory>();
    private AppProfile profile = AppProfile.SwingApp;

    @Override
    public AppProfile getProfile() {
        return profile;
    }
    
    {
        for (ClientModuleFactory moduleFactory : ServiceLoader.load(ClientModuleFactory.class)) {
            factoryList.add(moduleFactory);
        }
        for ( final AppModuleFactory moduleFactory : ServiceLoader.load(AppModuleFactory.class)) {
            factoryList.add(moduleFactory);
        }

        if ( factoryList.isEmpty() ) {
            throw new AssertionFailedException( "Failed to find base client modules: " + ClientModuleFactory.class  );
        }
    }

    @Override
    public Collection<AppModuleFactory> getModuleSet() {
        return ImmutableList.copyOf(factoryList);
    }

    @Override
    public ClientBuilder addModuleFactory(AppModuleFactory factory) {
        factoryList.add(factory);
        return this;
    }

    @Override
    public ClientBuilder removeModuleFactory(AppModuleFactory factory) {
        factoryList.remove(factory);
        return this;
    }

    @Override
    public ClientBuilder profile(AppProfile value) {
        this.profile = value;
        return this;
    }

    /**
     * Internal module injects SessionHelper
     */
    private static class SessionModule extends AbstractClientModule {

        private final SessionHelper helper;
        private final ClientBootstrap bootstrap;

        private <T extends LittleService> Provider<T> bindService(final Binder binder,
                final ServiceType<T> service) {
            Provider<T> provider = new Provider<T>() {

                @Override
                public T get() {
                    try {
                        final T result = helper.getService(service);
                        if (null == result) {
                            throw new AssertionFailedException("Failure to allocate service: " + service);
                        }
                        return result;
                    } catch (Exception e) {
                        throw new littleware.base.FactoryException("service retrieval failure for service " + service, e);
                    }
                }
            };
            binder.bind(service.getInterface()).toProvider(provider);
            log.log(Level.FINE, "Just bound service {0} interface {1}", new Object[]{service, service.getInterface().getName()});
            return provider;
        }

        public SessionModule(SessionHelper helper,
                AppBootstrap.AppProfile profile,
                ClientBootstrap bootstrap) {
            super(profile);
            this.helper = helper;
            this.bootstrap = bootstrap;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind(SessionHelper.class).toInstance(helper);
            binder.bind( LittleBootstrap.class ).to( AppBootstrap.class );
            binder.bind(AppBootstrap.class).to(ClientBootstrap.class);
            binder.bind(ClientBootstrap.class).toInstance(bootstrap);
            for (ServiceType<? extends LittleService> service : ServiceType.getMembers()) {
                log.log(Level.FINE, "Binding service provider: {0}", service);
                bindService(binder, service);
            }

            // Frick - need to bind core interfaces here explicitly
            binder.bind(AssetSearchManager.class).to(AssetSearchService.class);
            binder.bind(AccountManager.class).to(AccountManagerService.class);
            binder.bind(AssetManager.class).to(AssetManagerService.class);
            binder.bind( AppBootstrap.AppProfile.class ).toInstance( bootstrap.getProfile() );
            binder.bind(LittleSession.class).toProvider(new Provider<LittleSession>() {

                @Override
                public LittleSession get() {
                    try {
                        return helper.getSession();
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new AssertionFailedException("Failed to retrieve active session", e);
                    }
                }
            });
            try {
                binder.bindConstant().annotatedWith(Names.named("littleware.startupServerVersion")).to(helper.getServerVersion());
            } catch (RemoteException ex) {
                throw new AssertionFailedException("Failed to bind littleware.startupServerVersion constant", ex);
            }

            binder.bind(LittleUser.class).toProvider(new Provider<LittleUser>() {

                @Override
                public LittleUser get() {
                    try {
                        final AssetSearchManager search = helper.getService(ServiceType.ASSET_SEARCH);
                        return search.getAsset(helper.getSession().getOwnerId()).get().narrow(LittleUser.class);
                    } catch (RuntimeException e) {
                        throw e;
                    } catch (Exception e) {
                        throw new AssertionFailedException("Failed to retrieve active session", e);
                    }
                }
            });
            binder.bind(AssetRetriever.class).to(AssetSearchManager.class);
        }
    }

    private static class Bootstrap extends AbstractLittleBootstrap<AppModule> implements ClientBootstrap {

        private final SessionHelper helper;
        private final AppProfile profile;

        public Bootstrap(Collection<? extends AppModule> moduleSet,
                AppProfile profile, SessionHelper helper) {
            super(moduleSet);
            this.helper = helper;
            this.profile = profile;
        }

        @Override
        public AppProfile getProfile() { return profile; }

        @Override
        protected <T> T bootstrap(Class<T> injectTarget, Collection<? extends AppModule> originalModuleSet) {
            final ImmutableList.Builder<AppModule> builder = ImmutableList.builder();
            builder.addAll(originalModuleSet);
            builder.add(new SessionModule(helper, profile, this));
            return super.bootstrap(injectTarget, builder.build());
        }
    }

    private SimpleClientBuilder copy() {
        final SimpleClientBuilder result = new SimpleClientBuilder();
        result.factoryList.clear();
        result.factoryList.addAll( this.factoryList );
        result.profile = this.profile;
        return result;
    }

    @Override
    public ClientBootstrap.LoginSetup build() {
        return new SimpleLoginSetup( this.copy() );
    }

    ClientBootstrap build( SessionHelper helper ) {
        final ImmutableList.Builder<AppModule> builder = ImmutableList.builder();
        for (AppModuleFactory factory : factoryList) {
            builder.add(factory.build(profile));
        }
        return new Bootstrap(builder.build(), profile, helper);
    }
}

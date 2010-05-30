/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.server;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import java.io.IOException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;

import java.util.logging.Logger;

import littleware.asset.*;
import littleware.asset.client.AssetManagerService;
import littleware.asset.client.AssetSearchService;
import littleware.asset.client.SimpleAssetManagerService;
import littleware.asset.client.SimpleAssetSearchService;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.asset.server.NullAssetSpecializer;
import littleware.asset.server.QuotaUtil;
import littleware.asset.server.RmiAssetManager;
import littleware.asset.server.RmiSearchManager;
import littleware.asset.server.SimpleAssetManager;
import littleware.asset.server.SimpleAssetSearchManager;
import littleware.asset.server.SimpleQuotaUtil;
import littleware.asset.server.SimpleSpecializerRegistry;
import littleware.asset.server.db.jpa.HibernateGuice;
import littleware.asset.server.db.jpa.J2EEGuice;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.PropertiesGuice;
import littleware.bootstrap.server.ServerBootstrap.ServerProfile;
import littleware.db.DbGuice;
import littleware.security.AccountManager;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SessionManager;
import littleware.security.auth.server.AbstractServiceFactory;
import littleware.security.auth.server.ServiceFactory;
import littleware.security.client.AccountManagerService;
import littleware.security.client.SimpleAccountManagerService;
import littleware.security.server.RmiAccountManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Simple server-side guice module for littleware.asset
 */
public class AssetServerModule extends AbstractServerModule {

    private static final Logger log = Logger.getLogger(AssetServerModule.class.getName());

    private AssetServerModule(ServerBootstrap.ServerProfile profile,
            Map<AssetType, Class<? extends AssetSpecializer>> typeMap,
            Map<ServiceType, Class<? extends ServiceFactory>> serviceMap
            ) {
        super(profile, typeMap, emptyServiceMap, emptyServerListeners);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(AssetManager.class).to(SimpleAssetManager.class).in(Scopes.SINGLETON);
        binder.bind(AssetRetriever.class).to(AssetSearchManager.class).in(Scopes.SINGLETON);
        binder.bind(AssetSearchManager.class).to(SimpleAssetSearchManager.class).in(Scopes.SINGLETON);
        binder.bind(QuotaUtil.class).to(SimpleQuotaUtil.class).in(Scopes.SINGLETON);
        binder.bind(AssetSpecializerRegistry.class).to(SimpleSpecializerRegistry.class).in(Scopes.SINGLETON);
        binder.bind(AssetPathFactory.class).to(SimpleAssetPathFactory.class);
        if (getProfile().equals(ServerBootstrap.ServerProfile.J2EE)) {
            (new J2EEGuice()).configure(binder);
        } else {
            try {
                (new DbGuice( "littleware_jdbc.properties" )).configure(binder);
            } catch (IOException ex) {
                throw new AssertionFailedException( "Failed to load littleware_jdbc.properties", ex );
            }
            (new HibernateGuice()).configure(binder);
        }
        try {
            (new PropertiesGuice()).configure(binder);
        } catch (IOException ex) {
            throw new AssertionFailedException("Failed to access littleware.properties file", ex);
        }
    }

    public static class Activator implements BundleActivator {
        private final int registryPort;
        private boolean localRegistry = false;
        private Maybe<Registry> maybeRegistry;
        private final SessionManager sessionMgr;

        @Inject
        public Activator(ServerBootstrap bootstrap,
                @Named("int.lw.rmi_port") int registryPort,
                AssetSpecializerRegistry registry,
                SessionManager sessionMgr,
                Injector injector) {
            this.registryPort = registryPort;
            this.sessionMgr = sessionMgr;
            for (ServerModule module : bootstrap.getModuleSet()) {
                for (Map.Entry<AssetType, Class<? extends AssetSpecializer>> entry : module.getAssetTypes().entrySet()) {
                    registry.registerService(entry.getKey(), injector.getInstance(entry.getValue()));
                }
            }
        }

        @Override
        public void start(BundleContext bc) throws Exception {
            try {
                Registry rmi_registry = null;
                final int i_port = registryPort;
                if (i_port > 0) {
                    try {
                        log.log(Level.INFO, "Looking for RMI registry on port: " + i_port);
                        rmi_registry = LocateRegistry.createRegistry(i_port);
                        localRegistry = true;
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Failed to start RMI registry on port " + i_port
                                + " attempting to bind to already running registry", e);

                        rmi_registry = LocateRegistry.getRegistry(i_port);
                    }
                    maybeRegistry = Maybe.something(rmi_registry);

                    /**
                     * Need to wrap session manager with an invocation handler,
                     * because the RMI server thread inherits the ActivationContext
                     * of the client thread.  Frick.
                     */
                    /**
                     * Publish the reference in the Naming Service using JNDI API
                     * Context jndi_context = new InitialContext();
                     * jndi_context.rebind("/littleware/SessionManager", om_session );
                     */
                    rmi_registry.rebind("littleware/SessionManager", sessionMgr);
                } else {
                    log.log(Level.INFO, "Not exporing RMI registry - port set to: " + i_port);
                }
            } catch (Exception e) {
                //throw new AssertionFailedException("Failed to setup SessionManager, caught: " + e, e);
                log.log(Level.SEVERE, "Failed to bind to RMI registry "
                        + " running without exporting root SessionManager object to RMI universe",
                        e);

            }
            log.log(Level.INFO, "littleware RMI start ok");
        }

        @Override
        public void stop(BundleContext bc) throws Exception {
            if (maybeRegistry.isSet()) {
                final Registry reg = maybeRegistry.get();
                reg.unbind("littleware/SessionManager");
                if (localRegistry) {
                    UnicastRemoteObject.unexportObject(maybeRegistry.get(), true);
                }
            }
            log.log(Level.INFO, "littleware shutdown ok");

        }
    }

    @Override
    public Maybe<Class<Activator>> getActivator() {
        return Maybe.something(Activator.class);
    }

    public static class AccountServiceFactory extends AbstractServiceFactory<AccountManagerService> {

        private final AccountManager accountMgr;

        @Inject
        public AccountServiceFactory(AssetSearchManager search, AccountManager accountMgr) {
            super(ServiceType.ACCOUNT_MANAGER, search);
            this.accountMgr = accountMgr;
        }

        @Override
        public AccountManagerService createServiceProvider(SessionHelper helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
            return new SimpleAccountManagerService(new RmiAccountManager(this.checkAccessMakeProxy(helper, false, accountMgr, AccountManager.class)));
        }
    }

    public static class AssetServiceFactory extends AbstractServiceFactory<AssetManagerService> {

        private final AssetManager saver;

        @Inject
        public AssetServiceFactory(AssetSearchManager search, AssetManager saver) {
            super(ServiceType.ASSET_MANAGER, search);
            this.saver = saver;
        }

        @Override
        public AssetManagerService createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
            return new SimpleAssetManagerService(new RmiAssetManager(this.checkAccessMakeProxy(m_helper, false, saver, AssetManager.class)));
        }
    }

    public static class SearchServiceFactory extends AbstractServiceFactory<AssetSearchService> {

        private final AssetSearchManager search;

        public SearchServiceFactory(AssetSearchManager search) {
            super(ServiceType.ASSET_SEARCH, search);
            this.search = search;
        }

        @Override
        public AssetSearchService createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
            return new SimpleAssetSearchService(new RmiSearchManager(this.checkAccessMakeProxy(m_helper, true, search, AssetSearchManager.class)));
        }
    }

    public static class Factory implements ServerModule.ServerFactory {

        private final Map<AssetType, Class<? extends AssetSpecializer>> typeMap;

        {
            final ImmutableMap.Builder<AssetType, Class<? extends AssetSpecializer>> builder = ImmutableMap.builder();
            for (AssetType assetType : Arrays.asList(AssetType.GENERIC, AssetType.HOME, AssetType.LINK, AssetType.LOCK)) {
                builder.put(assetType, NullAssetSpecializer.class);
            }
            typeMap = builder.build();
        }
        private final Map<ServiceType, Class<? extends ServiceFactory>> serviceMap;

        {
            final ImmutableMap.Builder<ServiceType, Class<? extends ServiceFactory>> builder =
                    ImmutableMap.builder();
            serviceMap = builder.put(ServiceType.ASSET_SEARCH, SearchServiceFactory.class
                    ).put( ServiceType.ACCOUNT_MANAGER, AccountServiceFactory.class
                    ).put( ServiceType.ASSET_MANAGER, AssetServiceFactory.class
                    ).build();
        }

        @Override
        public ServerModule build(ServerProfile profile) {
            return new AssetServerModule(profile, typeMap, serviceMap );
        }
    }
}

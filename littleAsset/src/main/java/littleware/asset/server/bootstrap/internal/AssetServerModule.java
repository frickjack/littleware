/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.bootstrap.internal;

import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetManager;
import littleware.asset.internal.AssetRetriever;
import com.google.common.collect.ImmutableMap;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import com.google.inject.name.Named;
import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.Map;
import java.util.logging.Level;

import java.util.logging.Logger;
import javax.imageio.spi.ServiceRegistry;

import littleware.asset.*;
import littleware.asset.server.AssetSpecializer;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.NullAssetSpecializer;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.security.server.QuotaUtil;
import littleware.asset.server.internal.SimpleAssetManager;
import littleware.asset.server.internal.SimpleAssetSearchManager;
import littleware.security.server.internal.SimpleQuotaUtil;
import littleware.asset.server.internal.SimpleSpecializerRegistry;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.server.db.jpa.HibernateGuice;
import littleware.asset.server.db.jpa.J2EEGuice;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.PropertiesGuice;
import littleware.asset.server.bootstrap.ServerBootstrap.ServerProfile;
import littleware.asset.server.bootstrap.ServerModule;
import littleware.asset.server.bootstrap.ServerModuleFactory;
import littleware.base.cache.Cache;
import littleware.base.cache.InMemoryCacheBuilder;
import littleware.bootstrap.LittleModule;
import littleware.db.DbGuice;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import littleware.security.LittleUser;
import littleware.security.Quota;
import littleware.security.auth.client.SessionManager;
import littleware.security.auth.internal.SessionUtil;
import littleware.security.auth.server.internal.AuthServerGuice;
import littleware.security.server.internal.SimpleAccountManager;
import littleware.security.server.internal.SimpleAclManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Simple server-side guice module for littleware.asset
 */
public class AssetServerModule extends AbstractServerModule {

    private static final Logger log = Logger.getLogger(AssetServerModule.class.getName());

    private AssetServerModule(ServerBootstrap.ServerProfile profile,
            Map<AssetType, Class<? extends AssetSpecializer>> typeMap
            ) {
        super(profile, typeMap, emptyServerListeners);
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(Cache.Builder.class).to(InMemoryCacheBuilder.class);
        binder.bind(AssetManager.class).to(SimpleAssetManager.class).in(Scopes.SINGLETON);
        binder.bind(AssetRetriever.class).to(AssetSearchManager.class).in(Scopes.SINGLETON);
        binder.bind(AssetSearchManager.class).to(SimpleAssetSearchManager.class).in(Scopes.SINGLETON);
        binder.bind(QuotaUtil.class).to(SimpleQuotaUtil.class).in(Scopes.SINGLETON);
        binder.bind(AssetSpecializerRegistry.class).to(SimpleSpecializerRegistry.class).in(Scopes.SINGLETON);
        if (getProfile().equals(ServerBootstrap.ServerProfile.J2EE)) {
            log.log(Level.INFO, "Configuring JPA in J2EE mode ...");
            (new J2EEGuice()).configure(binder);
        } else {
            log.log(Level.INFO, "Configuring JPA in standalone (hibernate) mode ...");
            try {
                DbGuice.build("littleware_jdbc.properties").configure(binder);
            } catch (IOException ex) {
                throw new AssertionFailedException("Failed to load littleware_jdbc.properties", ex);
            }
            (new HibernateGuice()).configure(binder);
        }
        try {
            PropertiesGuice.build().configure(binder);
        } catch (IOException ex) {
            throw new AssertionFailedException("Failed to access littleware.properties file", ex);
        }
        (new AuthServerGuice()).configure(binder);
    }

    public static class Activator implements BundleActivator {

        private final int registryPort;
        private boolean localRegistry = false;
        private Option<Registry> maybeRegistry;
        private final SessionManager sessionMgr;

        @Inject
        public Activator(
                ServerBootstrap bootstrap,
                @Named("int.lw.rmi_port") int registryPort,
                AssetSpecializerRegistry assetRegistry,
                ServiceRegistry serviceRegistry,
                SessionManager sessionMgr,
                Provider<LittleTransaction> transactionProvider,
                DbAssetManager dbManager,
                Injector injector) {
            this.registryPort = registryPort;
            this.sessionMgr = sessionMgr;
            boolean rollback = true;
            // setup an overall transaction for the asset type auto-register code
            final LittleTransaction transaction = transactionProvider.get();
            transaction.startDbUpdate();
            try {

                for (LittleModule scan : bootstrap.getModuleSet()) {
                    if (scan instanceof ServerModule) {
                        final ServerModule module = (ServerModule) scan;
                        for (Map.Entry<AssetType, Class<? extends AssetSpecializer>> entry : module.getAssetTypes().entrySet()) {
                            assetRegistry.registerService(entry.getKey(), injector.getInstance(entry.getValue()));
                            dbManager.makeTypeChecker().saveObject(entry.getKey());
                        }
                    } 
                }
                rollback = false;
            } catch (SQLException ex) {
                throw new IllegalStateException("Failed to auto-register asset types", ex);
            } finally {
                transaction.endDbUpdate(rollback);
            }
        }

        @Override
        public void start(BundleContext bc) throws Exception {
            try {
                // inject local SessionManager for colocated server-client situation
                SessionUtil.get().injectLocalManager(sessionMgr);
                Registry rmi_registry = null;
                final int port = registryPort;
                if (port > 0) {
                    try {
                        log.log(Level.INFO, "Looking for RMI registry on port: {0}", port);
                        rmi_registry = LocateRegistry.createRegistry(port);
                        localRegistry = true;
                    } catch (Exception e) {
                        log.log(Level.SEVERE, "Failed to start RMI registry on port " + port
                                + " attempting to bind to already running registry", e);

                        rmi_registry = LocateRegistry.getRegistry(port);
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
                    log.log(Level.INFO, "Not exporing RMI registry - port set to: " + port);
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
    public Class<Activator> getActivator() {
        return Activator.class;
    }



    public static class Factory implements ServerModuleFactory {

        private final Map<AssetType, Class<? extends AssetSpecializer>> typeMap;

        {
            final ImmutableMap.Builder<AssetType, Class<? extends AssetSpecializer>> builder = ImmutableMap.builder();
            for (AssetType assetType : Arrays.asList(GenericAsset.GENERIC, LittleHome.HOME_TYPE, LinkAsset.LINK_TYPE)) {
                builder.put(assetType, NullAssetSpecializer.class);
            }
            for (AssetType assetType : Arrays.asList(LittlePrincipal.PRINCIPAL_TYPE,
                    LittleGroup.GROUP_TYPE,
                    Quota.QUOTA_TYPE,
                    LittleUser.USER_TYPE)) {
                builder.put(assetType, SimpleAccountManager.class);
            }
            for (AssetType assetType : Arrays.asList(LittleAcl.ACL_TYPE,
                    LittleAclEntry.ACL_ENTRY)) {
                builder.put(assetType, SimpleAclManager.class);
            }


            typeMap = builder.build();
        }

        @Override
        public ServerModule build(ServerProfile profile) {
            return new AssetServerModule(profile, typeMap);
        }
    }
}

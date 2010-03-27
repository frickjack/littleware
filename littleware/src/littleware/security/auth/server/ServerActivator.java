/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.server.CacheManager;
import littleware.asset.client.AssetManagerService;
import littleware.asset.client.AssetSearchService;
import littleware.asset.client.SimpleAssetManagerService;
import littleware.asset.client.SimpleAssetSearchService;
import littleware.asset.server.RmiAssetManager;
import littleware.asset.server.RmiSearchManager;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.security.AccountManager;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SessionManager;
import littleware.security.client.AccountManagerService;
import littleware.security.client.SimpleAccountManagerService;
import littleware.security.server.RmiAccountManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Startup the RMI-registyr if necessary, and registry
 * the remote SessionManager
 */
public class ServerActivator implements BundleActivator {

    private static final Logger log = Logger.getLogger(ServerActivator.class.getName());
    private final SessionManager sessionMgr;
    private final CacheManager cacheMgr;
    private final int registryPort;
    private Maybe<Registry> maybeRegistry = Maybe.empty();
    private boolean  localRegistry = false;

    @Inject
    public ServerActivator(SessionManager mgr_session,
            @Named("int.lw.rmi_port") int i_registry_port,
            ServiceProviderRegistry reg_service,
            final CacheManager mgr_cache,
            final AssetSearchManager mgr_search,
            final AssetManager mgr_asset,
            final AccountManager mgr_account //final AclManager     mgr_acl
            //final DbAuthManager      dbauth
            ) {
        sessionMgr = mgr_session;
        cacheMgr = mgr_cache;
        registryPort = i_registry_port;
        //SimpleDbLoginModule.start( mgr_account, dbauth, mgr_transaction );
        reg_service.registerService(ServiceType.ACCOUNT_MANAGER,
                new AbstractServiceProviderFactory<AccountManagerService>(ServiceType.ACCOUNT_MANAGER, mgr_search) {

                    @Override
                    public AccountManagerService createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new SimpleAccountManagerService(new RmiAccountManager(this.checkAccessMakeProxy(m_helper, false, mgr_account, AccountManager.class)));
                    }
                });
        reg_service.registerService(ServiceType.ASSET_MANAGER,
                new AbstractServiceProviderFactory<AssetManagerService>(ServiceType.ASSET_MANAGER, mgr_search) {

                    @Override
                    public AssetManagerService createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new SimpleAssetManagerService(new RmiAssetManager(this.checkAccessMakeProxy(m_helper, false, mgr_asset, AssetManager.class)));
                    }
                });
        reg_service.registerService(ServiceType.ASSET_SEARCH,
                new AbstractServiceProviderFactory<AssetSearchService>(ServiceType.ASSET_SEARCH, mgr_search) {

                    @Override
                    public AssetSearchService createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new SimpleAssetSearchService(new RmiSearchManager(this.checkAccessMakeProxy(m_helper, true, mgr_search, AssetSearchManager.class)));
                    }
                });

    }

    /**
     * Startup RMI registry if necessary and bind SessionManager
     *
     * @param ctx
     * @throws java.lang.Exception
     */
    @Override
    public void start(BundleContext ctx) throws Exception {
        try {
            Registry rmi_registry = null;
            final int i_port = registryPort;
            if (i_port > 0) {
                try {
                    log.log(Level.INFO, "Looking for RMI registry on port: " + i_port);
                    rmi_registry = LocateRegistry.createRegistry(i_port);
                    localRegistry = true;
                } catch (Exception e) {
                    log.log(Level.SEVERE, "Failed to start RMI registry on port " + i_port +
                            " attempting to bind to already running registry", e);

                    rmi_registry = LocateRegistry.getRegistry(i_port);
                }
                maybeRegistry = Maybe.something(rmi_registry );

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
            log.log(Level.SEVERE, "Failed to bind to RMI registry " +
                    " running without exporting root SessionManager object to RMI universe",
                    e);

        }
        log.log(Level.INFO, "littleware RMI start ok");
        // clear the cache at startup for now
        cacheMgr.clear();
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        if ( maybeRegistry.isSet() ) {
            final Registry reg = maybeRegistry.get();
            reg.unbind( "littleware/SessionManager" );
            if( localRegistry ) {
                UnicastRemoteObject.unexportObject( maybeRegistry.get(), true);
            }
        }
        log.log(Level.INFO, "littleware shutdown ok");
    }
}

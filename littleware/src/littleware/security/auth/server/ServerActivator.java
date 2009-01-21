/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 * 
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
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
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetException;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.CacheManager;
import littleware.asset.server.RmiAssetManager;
import littleware.asset.server.RmiSearchManager;
import littleware.asset.server.TransactionManager;
import littleware.base.BaseException;
import littleware.security.AccountManager;
import littleware.security.AclManager;
import littleware.security.auth.ServiceType;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SessionManager;
import littleware.security.auth.server.db.DbAuthManager;
import littleware.security.server.RmiAccountManager;
import littleware.security.server.RmiAclManager;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Startup the RMI-registyr if necessary, and registry
 * the remote SessionManager
 */
public class ServerActivator implements BundleActivator {
    private static final Logger  olog = Logger.getLogger( ServerActivator.class.getName() );

    private final SessionManager   omgr_session;
    private final CacheManager     omgr_cache;
    private final int              oi_registry_port;
    
    @Inject
    public ServerActivator( SessionManager mgr_session, 
            @Named( "int.lw.rmi_port" ) int i_registry_port,
            ServiceProviderRegistry reg_service,
            final CacheManager       mgr_cache,
            final AssetSearchManager mgr_search,
            final AssetManager mgr_asset,
            final AccountManager mgr_account,
            final AclManager     mgr_acl,
            final TransactionManager mgr_transaction,
            final DbAuthManager      dbauth
            )
    {
        omgr_session = mgr_session;
        omgr_cache = mgr_cache;
        oi_registry_port = i_registry_port;
        SimpleDbLoginModule.start( mgr_account, dbauth, mgr_transaction );
        reg_service.registerService( ServiceType.ACCOUNT_MANAGER,
                new AbstractServiceProviderFactory<AccountManager> ( ServiceType.ACCOUNT_MANAGER, mgr_search ) {
                    @Override
                    public AccountManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new RmiAccountManager( this.checkAccessMakeProxy(m_helper, false, mgr_account, AccountManager.class ));
                    }
                }
        );
        reg_service.registerService( ServiceType.ACL_MANAGER,
                new AbstractServiceProviderFactory<AclManager> ( ServiceType.ACL_MANAGER, mgr_search ) {
                    @Override
                    public AclManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new RmiAclManager( this.checkAccessMakeProxy(m_helper, false, mgr_acl, AclManager.class ));
                    }
                }
        );
        reg_service.registerService( ServiceType.ASSET_MANAGER,
                new AbstractServiceProviderFactory<AssetManager> ( ServiceType.ASSET_MANAGER, mgr_search ) {
                    @Override
                    public AssetManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new RmiAssetManager( this.checkAccessMakeProxy(m_helper, false, mgr_asset, AssetManager.class ));
                    }
                }
        );
        reg_service.registerService( ServiceType.ASSET_SEARCH,
                new AbstractServiceProviderFactory<AssetSearchManager> ( ServiceType.ASSET_SEARCH, mgr_search ) {
                    @Override
                    public AssetSearchManager createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return new RmiSearchManager( this.checkAccessMakeProxy(m_helper, true, mgr_search, AssetSearchManager.class ));
                    }
                }
        );
        reg_service.registerService( ServiceType.SESSION_HELPER,
                new ServiceProviderFactory<SessionHelper> () {
                    public SessionHelper createServiceProvider(SessionHelper m_helper) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
                        return m_helper;
                    }
                }
        );
    }

    /**
     * Startup RMI registry if necessary and bind SessionManager
     *
     * @param ctx
     * @throws java.lang.Exception
     */
    public void start(BundleContext ctx) throws Exception {
        try {
            Registry rmi_registry = null;
            final int i_port = oi_registry_port;

            try {
                olog.log(Level.INFO, "Looking for RMI registry on port: " + i_port);
                rmi_registry = LocateRegistry.createRegistry(i_port);
            } catch ( Exception e ) {
               olog.log( Level.SEVERE, "Failed to locate or start RMI registry on port " + i_port +
                            " running without exporting root SessionManager object to RMI universe", e
                            );

                rmi_registry = LocateRegistry.getRegistry( i_port );
            }

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
            rmi_registry.rebind("littleware/SessionManager", omgr_session );
        } catch (Exception e) {
            //throw new AssertionFailedException("Failed to setup SessionManager, caught: " + e, e);
            olog.log( Level.SEVERE, "Failed to bind to RMI registry " +
                                " running without exporting root SessionManager object to RMI universe",
                                e
                                );

        }
        olog.log( Level.INFO, "littleware RMI start ok" );
        // clear the cache at startup for now
        omgr_cache.clear();
    }

    public void stop(BundleContext ctx) throws Exception {
        olog.log( Level.INFO, "littleware shutdown ok" );
    }

}
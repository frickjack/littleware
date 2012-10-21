/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server.internal;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.*;
//import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.*;
import javax.security.auth.*;
import javax.security.auth.login.Configuration;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import littleware.asset.*;
import littleware.asset.AssetTreeTemplate.AssetInfo;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.asset.TemplateScanner.ExistInfo;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerScannerFactory;
import littleware.asset.server.ServerSearchManager;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.*;
import littleware.base.login.LoginCallbackHandler;
import littleware.base.stat.Sampler;
import littleware.base.stat.SimpleSampler;
import littleware.net.LittleRemoteObject;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.security.auth.LittleSession.Builder;
import littleware.security.auth.internal.RemoteSessionManager;
import littleware.security.auth.server.ServerConfigFactory;
import org.joda.time.DateTime;

/**
 * Simple implementation of SessionManager.
 * Hands of authentication to new LoginContext,
 * then passes the authenticated Subject onto the SessionHelper.
 * This class ought to be registered as a Singleton and exported
 * for RMI access.
 */
public class SimpleSessionManager extends LittleRemoteObject implements RemoteSessionManager {

    private static final Logger log = Logger.getLogger(SimpleSessionManager.class.getName());
    private static final long serialVersionUID = 8144056326046717141L;
    private static final String serverVersionName = "ServerVersion";
    private final ServerSearchManager search;
    private final ServerAssetManager assetMgr;
    private static boolean isSingletonUp = false;
    private final Sampler statSampler = new SimpleSampler();
    private final Provider<UserTreeBuilder> userTreeBuilder;
    private final Provider<GenericBuilder> provideGenerics;
    private final Provider<TemplateBuilder> templateProvider;
    private final Provider<AssetPathFactory> pathFactory;
    private final Provider<Builder> sessionProvider;
    private final LittleContext.ContextFactory contextFactory;
    private final ServerScannerFactory scannerFactory;
    private final Provider<Configuration> loginConfigProvider;

    @Inject
    public SimpleSessionManager(ServerAssetManager assetMgr,
            ServerSearchManager search,
            Provider<UserTreeBuilder> provideUserTree,
            Provider<GenericAsset.GenericBuilder> provideGenerics,
            Provider<AssetTreeTemplate.TemplateBuilder> templateProvider,
            Provider<AssetPathFactory> pathFactory,
            Provider<LittleSession.Builder> sessionProvider,
            LittleContext.ContextFactory contextFactory,
            ServerScannerFactory scannerFactory,
            ServerConfigFactory loginConfigProvider) throws RemoteException {
        this.assetMgr = assetMgr;
        this.search = search;
        if (isSingletonUp) {
            throw new IllegalStateException("SimpleSessionManager must be a singleton");
        }
        isSingletonUp = true;
        this.userTreeBuilder = provideUserTree;
        this.provideGenerics = provideGenerics;
        this.templateProvider = templateProvider;
        this.pathFactory = pathFactory;
        this.sessionProvider = sessionProvider;
        this.contextFactory = contextFactory;
        this.scannerFactory = scannerFactory;
        this.loginConfigProvider = loginConfigProvider;
    }

    /**
     * Save the given LittleSession to the appropriate location in the repository node graph
     */
    private LittleSession setupSession(LittleContext adminCtx, LittleSession session, String sessionComment) throws BaseException, GeneralSecurityException {
        final LittleHome home = search.getByName(adminCtx, "littleware.home", LittleHome.HOME_TYPE).get().narrow();

        // First - verify ServerVersion node exists -
        // TODO: find a better place to do this
        if (search.getAssetFrom(adminCtx, home.getId(), serverVersionName).isEmpty()) {
            // Only administrator can creat child of littleware.home ...
            assetMgr.saveAsset(adminCtx,
                    provideGenerics.get().parent(home).name(serverVersionName).data("v0.0").build(),
                    "Setup v0.0 ServerVersion node");
        }
        // Let's create a date-hierarchy to store the user session under
        final DateTime now = new DateTime();
        final AssetPath path = pathFactory.get().createPath("/" + home.getName() + "/"
                + Integer.toString(now.getYear()) + "/"
                + now.toString("MM") + "/"
                + now.toString("dd"));
        final AssetTreeTemplate template = templateProvider.get().path(path).build();
        Asset parent = home;
        for (AssetInfo x : template.scan(home, scannerFactory.build(adminCtx))) {
            final TemplateScanner.ExistInfo info = (TemplateScanner.ExistInfo) x;
            parent = info.getAsset();
            if (!info.getAssetExists()) {
                // be sure to null out aclId, so random users cannot steal session ids
                parent = assetMgr.saveAsset(adminCtx,
                        info.getAsset().copy().aclId(null).build(),
                        sessionComment).get(info.getAsset().getId());
            }
        }

        return assetMgr.saveAsset(adminCtx,
                ((AbstractAssetBuilder) session.copy()).parentId(parent.getId()).homeId(parent.getHomeId()).build(),
                sessionComment).get(session.getId()).narrow();
    }

    /**
     * For now just authenticate anyone with a user account
     */
    @Override
    public LittleSession login(final String name, final String password, String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final LoginContext loginCtx;
        {
            LoginContext tmp = null;
            try {
                tmp = new LoginContext("littleware.login",
                        new Subject(),
                        new LoginCallbackHandler(name, password),
                        loginConfigProvider.get());
            } catch (Exception ex) {
                log.log(Level.INFO, "Assuming pass-through login - no littleware.login context available", ex);
            }
            loginCtx = tmp;
        }

        if (null != loginCtx) {
            try {
                loginCtx.login();
                final Subject caller = loginCtx.getSubject();
            } catch (FailedLoginException ex) {
                // dispose of cause - probably not serializable
                log.log(Level.WARNING, "Login failed", ex);
                throw new FailedLoginException();
            }
        }

        final LittleContext adminCtx = contextFactory.buildAdminContext();
        adminCtx.getTransaction().startDbAccess();
        try {
            final LittleHome littleHome = search.getByName(adminCtx, "littleware.home", LittleHome.HOME_TYPE).get().narrow(LittleHome.class);
            final LittleUser user;
            {
                Option<? extends Asset> maybeUser = search.getByName(adminCtx, name, LittleUser.USER_TYPE);
                if (!maybeUser.isSet()) {
                    // Create the user
                    for (AssetTreeTemplate.AssetInfo assetInfo : userTreeBuilder.get().user(name).build().scan(littleHome, scannerFactory.build(adminCtx))) {
                        final ExistInfo treeInfo = (ExistInfo) assetInfo;
                        if (!treeInfo.getAssetExists()) {
                            assetMgr.saveAsset(adminCtx, treeInfo.getAsset(), "Setup new user: " + name);
                        }
                    }
                    user = search.getByName(adminCtx, name, LittleUser.USER_TYPE).get().narrow();
                } else {
                    user = maybeUser.get().narrow();
                }
            }
            // ok - user authenticated ok by here - setup user session
            final LittleSession session;
            {
                final LittleSession.Builder sessionBuilder = sessionProvider.get();
                sessionBuilder.setId(UUID.randomUUID());
                sessionBuilder.setName(name + "_" + UUIDFactory.makeCleanString(sessionBuilder.getId()));
                sessionBuilder.setOwnerId(user.getId());
                sessionBuilder.setComment("User login");
                session = sessionBuilder.build();

                if (session.getOwnerId() != user.getId()) {
                    throw new AssertionFailedException("Owner mismatch");
                }
            }
            // Create the session asset as the admin user - session has null from-id
            return setupSession(adminCtx, session, sessionComment);
        } finally {
            adminCtx.getTransaction().endDbAccess();
        }
    }

    @Override
    public String getServerVersion() throws RemoteException {
        // Create the session asset as the admin user - session has null from-id
        final LittleContext adminCtx = contextFactory.buildAdminContext();
        try {
            final Asset home = search.getByName(adminCtx, "littleware.home", LittleHome.HOME_TYPE).get();
            final Option<Asset> maybe = search.getAssetFrom(adminCtx, home.getId(), serverVersionName);

            if (maybe.isSet()) {
                return maybe.get().narrow(GenericAsset.class).getData();
            } else {
                // Note: ServerVersionNode should be initialized in SessionManager if it doesn't exist
                return "v0.0";
            }
        } catch (RuntimeException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new AssertionFailedException("Unexpected exception: " + ex);
        } finally {
            adminCtx.getTransaction().endDbAccess();
        }
    }

    @Override
    public LittleSession createNewSession(UUID currentSessionId, String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final LittleContext adminCtx = contextFactory.buildAdminContext();
        adminCtx.getTransaction().startDbAccess();
        
        try {
            final LittleContext ctx = contextFactory.build(currentSessionId);
            final LittleSession.Builder sessionBuilder = sessionProvider.get();
            final LittleUser caller = ctx.getCaller();
            final UUID id = UUID.randomUUID();
            sessionBuilder.id( id
                    ).name(caller.getName() + "_" + UUIDFactory.makeCleanString( id )
                    ).comment( sessionComment
                    ).ownerId( caller.getId() );

            // Create the session asset as the admin user - session has null from-id
            return setupSession(adminCtx, sessionBuilder.build(), sessionComment);
        } catch (FactoryException e) {
            throw new AssertionFailedException("Caught: " + e, e);
        } catch (NoSuchThingException e) {
            throw new AssertionFailedException("Caught: " + e, e);
        } finally {
            adminCtx.getTransaction().endDbAccess();
        }
    }
}

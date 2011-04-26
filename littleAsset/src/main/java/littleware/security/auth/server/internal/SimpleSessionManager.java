/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server.internal;

import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.AssetManager;
import littleware.security.auth.client.SessionManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.*;
//import java.rmi.server.UnicastRemoteObject;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.*;
import javax.security.auth.*;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.asset.*;
import littleware.asset.AssetTreeTemplate.AssetInfo;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.*;
import littleware.base.stat.Sampler;
import littleware.base.stat.SimpleSampler;
import littleware.net.LittleRemoteObject;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.security.auth.LittleSession.Builder;
import org.joda.time.DateTime;

/**
 * Simple implementation of SessionManager.
 * Hands of authentication to new LoginContext,
 * then passes the authenticated Subject onto the SessionHelper.
 * This class ought to be registered as a Singleton and exported
 * for RMI access.
 */
public class SimpleSessionManager extends LittleRemoteObject implements SessionManager {

    private static final Logger log = Logger.getLogger(SimpleSessionManager.class.getName());
    private static final long serialVersionUID = 8144056326046717141L;
    private static final String serverVersionName = "ServerVersion";
    
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private static boolean isSingletonUp = false;
    private final Sampler statSampler = new SimpleSampler();
    private final Provider<UserTreeBuilder> userTreeBuilder;
    private final Provider<GenericBuilder> provideGenerics;
    private final Provider<TemplateBuilder> templateProvider;
    private final Provider<AssetPathFactory> pathFactory;
    private final Provider<Builder> sessionProvider;

    @Inject
    public SimpleSessionManager(AssetManager m_asset,
            AssetSearchManager m_search,
            Provider<UserTreeBuilder> provideUserTree,
            Provider<GenericAsset.GenericBuilder> provideGenerics,
            Provider<AssetTreeTemplate.TemplateBuilder> templateProvider,
            Provider<AssetPathFactory> pathFactory,
            Provider<LittleSession.Builder> sessionProvider) throws RemoteException {
        assetMgr = m_asset;
        search = m_search;
        if (isSingletonUp) {
            throw new IllegalStateException("SimpleSessionManager must be a singleton");
        }
        isSingletonUp = true;
        this.userTreeBuilder = provideUserTree;
        this.provideGenerics = provideGenerics;
        this.templateProvider = templateProvider;
        this.pathFactory = pathFactory;
        this.sessionProvider = sessionProvider;
    }

    @Override
    public LittleSession createNewSession(UUID currentSessionId, String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        try {
            final LittleSession.Builder sessionBuilder = sessionProvider.get();
            final LittleUser caller = Subject.getSubject(AccessController.getContext()).
                    getPrincipals(LittleUser.class).iterator().next();

            sessionBuilder.setName(caller.getName() + ", " + sessionBuilder.getCreateDate().getTime());
            sessionBuilder.setComment(sessionComment);

            for (int i = 0; i < 20; ++i) {
                try {
                    return assetMgr.saveAsset(sessionBuilder.build(), sessionComment).narrow();
                } catch (AlreadyExistsException ex) {
                    if (i < 10) {
                        sessionBuilder.setName(caller.getName() + ", " + sessionBuilder.getCreateDate().getTime() + "," + i);
                    } else {
                        throw new AccessDeniedException("Too many simultaneous session setups running for user: " + sessionBuilder.getName());
                    }
                }
            }
            throw new AssertionFailedException("Failed to derive an unused session name");
        } catch (FactoryException e) {
            throw new AssertionFailedException("Caught: " + e, e);
        } catch (NoSuchThingException e) {
            throw new AssertionFailedException("Caught: " + e, e);
        }
    }

    /**
     * Privileged action creates a session-asset
     * as the user logging in.
     */
    private class SetupSessionAction implements PrivilegedExceptionAction<LittleSession> {

        private final String sessionComment;
        private final LittleSession session;

        /**
         * Stash the comment to attach to the new session asset,
         * and the name of the user creating the session.
         */
        public SetupSessionAction(LittleSession session, String sessionComment) {
            this.session = session;
            this.sessionComment = sessionComment;
        }

        @Override
        public LittleSession run() throws Exception {
            final LittleHome home = search.getByName("littleware.home", LittleHome.HOME_TYPE).get().narrow();

            // First - verify ServerVersion node exists -
            // TODO: find a better place to do this
            if (search.getAssetFrom(home.getId(), serverVersionName).isEmpty()) {
                // Only administrator can creat child of littleware.home ...
                assetMgr.saveAsset(
                        provideGenerics.get().parent(home).name(serverVersionName).data("v0.0").build(),
                        "Setup v0.0 ServerVersion node");
            }
            // Let's create a hierarchy
            final DateTime now = new DateTime();
            final AssetPath path = pathFactory.get().createPath("/" + home.getName() + "/"
                    + Integer.toString(now.getYear()) + "/"
                    + now.toString("MM") + "/"
                    + now.toString("dd"));
            final AssetTreeTemplate template = templateProvider.get().path(path).build();
            Asset parent = home;
            for (AssetInfo info : template.visit(home, search)) {
                parent = info.getAsset();
                if (!info.getAssetExists()) {
                    parent = assetMgr.saveAsset(info.getAsset(), sessionComment);
                }
            }

            return assetMgr.saveAsset(((AbstractAssetBuilder) session.copy()).parentId(parent.getId()).homeId(parent.getHomeId()).build(),
                    sessionComment).narrow();
        }
    }

    /**
     * Extract cause of PrivilegedActionException, and throw that
     *
     * @param exIn
     * @throws BaseException
     * @throws GeneralSecurityException
     * @throws DataAccessException fall through case
     */
    private static void handlePrivilegedException(PrivilegedActionException exIn) throws BaseException, GeneralSecurityException {
        try {
            throw exIn.getCause();
        } catch (BaseException ex) {
            throw ex;
        } catch (GeneralSecurityException ex) {
            throw ex;
        } catch (Throwable ex) {
            // do not include cause - since result is passed over RMI,
            // and cause class may not be in client classpath
            throw new DataAccessException("Caught exception: " + ex);
        }
    }

    /**
     * For now just authenticate anyone with a user account
     */
    @Override
    public LittleSession login(final String s_name, final String s_password, String sessionComment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Subject j_caller = null;
        LoginContext x_login = null;
        try {
            x_login = new LoginContext("littleware.login",
                    new SimpleCallbackHandler(s_name, s_password));
        } catch (Exception ex) {
            log.log(Level.INFO, "Assuming pass-through login - no littleware.login context available", ex);
            j_caller = new Subject();
        }
        if (null != x_login) {
            try {
                x_login.login();
                j_caller = x_login.getSubject();
            } catch (FailedLoginException ex) {
                // dispose of cause - probably not serializable
                throw new FailedLoginException();
            }
        }

        if (null == j_caller) {
            throw new LoginException("Failed to authenticate");
        }

        final Subject adminSubject = new Subject();
        adminSubject.getPrincipals().add(search.getByName(AccountManager.LITTLEWARE_ADMIN, LittleUser.USER_TYPE).get().narrow(LittlePrincipal.class));
        adminSubject.setReadOnly();

        // Do a little LoginContext sim - need to clean this up
        // Who am I running as now ?  What the frick ?
        final LittleUser user;
        {
            Option<? extends Asset> maybeUser = search.getByName(s_name, LittleUser.USER_TYPE);
            if (!maybeUser.isSet()) {
                try {
                    maybeUser = Subject.doAs(adminSubject, new PrivilegedExceptionAction<Option<Asset>>() {

                        @Override
                        public Option<Asset> run() throws BaseException, GeneralSecurityException, RemoteException {
                            for (AssetTreeTemplate.AssetInfo treeInfo : userTreeBuilder.get().user(s_name).build().visit(search.getByName("littleware.home", LittleHome.HOME_TYPE).get().narrow(LittleHome.class), search)) {
                                if (!treeInfo.getAssetExists()) {
                                    assetMgr.saveAsset(treeInfo.getAsset(), "Setup new user: " + s_name);
                                }
                            }
                            return search.getByName(s_name, LittleUser.USER_TYPE);
                        }
                    });
                } catch (PrivilegedActionException ex) {
                    log.log(Level.INFO, "Failed to setup new user", ex);
                    handlePrivilegedException(ex);
                    throw new AssertionFailedException("Should not make it here");
                }
            }
            user = maybeUser.get().narrow();
        }
        j_caller.getPrincipals().add(user);
        /*... disable for now ...
        javax.security.auth.spi.LoginModule module = new PasswordDbLoginModule();
        module.initialize ( j_caller, 
        new SimpleCallbackHandler(s_name, s_password),
        new HashMap<String,String>(),
        new HashMap<String,String>()
        );
        module.login();
        module.commit ();
         */
        j_caller.setReadOnly();
        // ok - user authenticated ok by here - setup user session
        final LittleSession session;
        {
            final LittleSession.Builder sessionBuilder = sessionProvider.get();
            sessionBuilder.setId(UUID.randomUUID());
            sessionBuilder.setName(s_name + "_" + UUIDFactory.makeCleanString(sessionBuilder.getId()));
            sessionBuilder.setOwnerId(user.getId());
            sessionBuilder.setComment("User login");
            session = sessionBuilder.build();
            if (session.getOwnerId() != user.getId()) {
                throw new AssertionFailedException("Owner mismatch");
            }
        }
        // Create the session asset as the admin user - session has null from-id
        final PrivilegedExceptionAction setupSessionAction = new SetupSessionAction(session, sessionComment);
        try {
            return (LittleSession) Subject.doAs(adminSubject, setupSessionAction);
        } catch (PrivilegedActionException e) {
            handlePrivilegedException(e);
            throw new AssertionFailedException("Should not make it here");
        }
    }

    @Override
    public String getServerVersion() throws RemoteException {
        // Create the session asset as the admin user - session has null from-id
        try {
            final Asset home = search.getByName("littleware.home", LittleHome.HOME_TYPE).get();
            final Option<Asset> maybe = search.getAssetFrom(home.getId(), serverVersionName);
            if (maybe.isSet()) {
                return maybe.get().narrow(GenericAsset.class).getData();
            } else {
                // Note: ServerVersionNode should be initialized in SessionManager if it doesn't exist
                return "v0.0";
            }
        } catch (Exception ex) {
            throw new AssertionFailedException("Unexpected exception: " + ex);
        }
    }
}

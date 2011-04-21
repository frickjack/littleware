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
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.security.*;
import javax.security.auth.*;
import java.lang.ref.WeakReference;

import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.asset.*;
import littleware.asset.AssetTreeTemplate.AssetInfo;
import littleware.asset.AssetTreeTemplate.TemplateBuilder;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.base.*;
import littleware.base.stat.Sampler;
import littleware.base.stat.SimpleSampler;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.security.auth.LittleSession.Builder;
import littleware.security.auth.server.ServiceRegistry;
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
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final ServiceRegistry serviceRegistry;
    private final Map<UUID, WeakReference<SessionHelper>> sessionCache = new HashMap<UUID, WeakReference<SessionHelper>>();
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
            ServiceRegistry reg_service,
            Provider<UserTreeBuilder> provideUserTree,
            Provider<GenericAsset.GenericBuilder> provideGenerics,
            Provider<AssetTreeTemplate.TemplateBuilder> templateProvider,
            Provider<AssetPathFactory> pathFactory,
            Provider<LittleSession.Builder> sessionProvider
            ) throws RemoteException {
        assetMgr = m_asset;
        search = m_search;
        if ( isSingletonUp ) {
            throw new IllegalStateException("SimpleSessionManager must be a singleton");
        }
        isSingletonUp = true;
        serviceRegistry = reg_service;
        this.userTreeBuilder = provideUserTree;
        this.provideGenerics = provideGenerics;
        this.templateProvider = templateProvider;
        this.pathFactory = pathFactory;
        this.sessionProvider = sessionProvider;
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
            if (search.getAssetFrom(home.getId(), SimpleSessionHelper.serverVersionName).isEmpty()) {
                // Only administrator can creat child of littleware.home ...
                assetMgr.saveAsset(
                        provideGenerics.get().parent(home).name(SimpleSessionHelper.serverVersionName).data("v0.0").build(),
                        "Setup v0.0 ServerVersion node");
            }
            // Let's create a hierarchy
            final DateTime now = new DateTime();
            final AssetPath path = pathFactory.get().createPath( "/" + home.getName() + "/" +
                    Integer.toString(now.getYear()) + "/" +
                    now.toString("MM") + "/" +
                    now.toString("dd")
                    );
            final AssetTreeTemplate template = templateProvider.get().path(path).build();
            Asset parent = home;
            for( AssetInfo info : template.visit( home, search)) {
                parent = info.getAsset();
                if ( ! info.getAssetExists() ) {
                    parent = assetMgr.saveAsset( info.getAsset(), sessionComment);
                }
            }

            return assetMgr.saveAsset(session.copy().parentId(parent.getId()).homeId(parent.getHomeId()).build(),
                    sessionComment).narrow();
        }
    }

    /**
     * Internal utility to setup RmiSessionHelper given a session asset
     *
     * @param session to setup and cache a new SessionHelper for
     */
    private SessionHelper setupNewHelper(LittleSession session) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Subject caller = session.getSubject(search);

        final SessionHelper helper = new SimpleSessionHelper(session.getId(), search, assetMgr, this, serviceRegistry, sessionProvider );
        final InvocationHandler handler = new SubjectInvocationHandler(caller, helper, statSampler);
        final SessionHelper proxy = (SessionHelper) Proxy.newProxyInstance(SessionHelper.class.getClassLoader(),
                new Class[]{SessionHelper.class},
                handler);
        final SessionHelper rmiWrapper = new RmiSessionHelper(proxy);
        sessionCache.put(session.getId(), new WeakReference(rmiWrapper));
        return rmiWrapper;
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
    public SessionHelper login(final String s_name, final String s_password, String s_session_comment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
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
            Maybe<? extends Asset> maybeUser = search.getByName(s_name, LittleUser.USER_TYPE);
            if (!maybeUser.isSet()) {
                try {
                    maybeUser = Subject.doAs(adminSubject, new PrivilegedExceptionAction<Maybe<Asset>>() {

                        @Override
                        public Maybe<Asset> run() throws BaseException, GeneralSecurityException, RemoteException {
                            for (AssetTreeTemplate.AssetInfo treeInfo : userTreeBuilder.get().user(s_name).build().visit(search.getByName("littleware.home", LittleHome.HOME_TYPE).get().narrow( LittleHome.class ), search)) {
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
        final PrivilegedExceptionAction setupSessionAction = new SetupSessionAction(session, s_session_comment);
        try {
            return setupNewHelper((LittleSession) Subject.doAs(adminSubject, setupSessionAction));
        } catch (PrivilegedActionException e) {
            handlePrivilegedException(e);
            throw new AssertionFailedException("Should not make it here");
        }
    }

    @Override
    public SessionHelper getSessionHelper( final UUID sessionId ) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        // Note that the SessionHelper will take care of doing SessionExpired checks, etc.
        WeakReference<SessionHelper> ref_helper = sessionCache.get(sessionId);

        if (null != ref_helper) {
            final SessionHelper helper = ref_helper.get();
            if (null != helper) {
                // Make sure the sesion hasn't expired
                if (helper.getSession().getEndDate().getTime() > new Date().getTime()) {
                    return helper;
                } else {
                    throw new SessionExpiredException("Expired at: " + helper.getSession().getEndDate());
                }
            } else {
                sessionCache.remove(sessionId);
            }
        }

        try {
            // Need to do this as administrator!  A LittleSession is not globally accessible ...
            final Subject adminSubject = new Subject();
            adminSubject.getPrincipals().add(search.getByName(AccountManager.LITTLEWARE_ADMIN, LittleUser.USER_TYPE).get().narrow(LittlePrincipal.class));
            adminSubject.setReadOnly();

            final LittleSession session = Subject.doAs( adminSubject,
                    new PrivilegedExceptionAction<LittleSession>() {
                        @Override
                        public LittleSession run() throws BaseException, GeneralSecurityException, RemoteException {
                            return search.getAsset(sessionId).get().narrow(LittleSession.class);
                        }
                    }
            );
            return setupNewHelper(session);
        } catch ( PrivilegedActionException ex ) {
            handlePrivilegedException( ex );
            throw new AssertionFailedException( "Should not reach here!" );
        } catch (GeneralSecurityException e) {
            throw new AccessDeniedException("Caught unexpected: " + e, e);
        }
    }
}

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
import littleware.base.*;
import littleware.base.stat.Sampler;
import littleware.base.stat.SimpleSampler;
import littleware.security.*;
import littleware.security.auth.*;
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
    private final ServiceProviderRegistry serviceRegistry;
    private final Map<UUID, WeakReference<SessionHelper>> sessionCache = new HashMap<UUID, WeakReference<SessionHelper>>();
    private static SimpleSessionManager sessionMgr = null;
    private final Sampler statSampler = new SimpleSampler();
    private final Provider<UserTreeBuilder> userTreeBuilder;

    /**
     * Inject dependencies
     */
    @Inject
    public SimpleSessionManager(AssetManager m_asset,
            AssetSearchManager m_search,
            ServiceProviderRegistry reg_service,
            Provider<UserTreeBuilder> provideUserTree ) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        assetMgr = m_asset;
        search = m_search;
        if (null != sessionMgr) {
            throw new IllegalStateException("SimpleSessionManager must be a singleton");
        }
        sessionMgr = this;
        serviceRegistry = reg_service;
        this.userTreeBuilder = provideUserTree;
    }



    /**
     * Privileged action creates a session-asset
     * as the user logging in.
     */
    private class SetupSessionAction implements PrivilegedExceptionAction<LittleSession> {

        private final String os_session_comment;
        private final LittleSession osession;

        /**
         * Stash the comment to attach to the new session asset,
         * and the name of the user creating the session.
         */
        public SetupSessionAction(LittleSession session, String s_session_comment) {
            osession = session;
            os_session_comment = s_session_comment;
        }

        @Override
        public LittleSession run() throws Exception {
            // Let's create a hierarchy
            final DateTime now = new DateTime();
            final List<String> pathList = Arrays.asList(
                    "Sessions", Integer.toString(now.getYear()),
                    now.toString("MM"),
                    now.toString("dd"));
            Asset parent = search.getByName("littleware.home", AssetType.HOME).get();
            for (String childName : pathList) {
                final Maybe<Asset> maybe = search.getAssetFrom(parent.getId(), childName);
                if (maybe.isSet()) {
                    parent = maybe.get();
                    continue;
                }
                final Asset child = AssetType.GENERIC.create().parent(parent).name(childName).build();
                parent = assetMgr.saveAsset(child, os_session_comment);
            }
            return assetMgr.saveAsset(osession.copy().fromId(parent.getId()).homeId(parent.getHomeId()).build(),
                    os_session_comment).narrow();
        }
    }

    /**
     * Internal utility to setup RmiSessionHelper given a session asset
     *
     * @param session to setup and cache a new SessionHelper for
     */
    private SessionHelper setupNewHelper(LittleSession session) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Subject caller = session.getSubject(search);
        final SessionHelper helper = new SimpleSessionHelper(session.getId(), search, assetMgr, this, serviceRegistry);
        final InvocationHandler handler = new SubjectInvocationHandler(caller, helper, statSampler );
        final SessionHelper proxy = (SessionHelper) Proxy.newProxyInstance(SessionHelper.class.getClassLoader(),
                new Class[]{SessionHelper.class},
                handler);
        final SessionHelper rmiWrapper = new RmiSessionHelper(proxy);
        sessionCache.put(session.getId(), new WeakReference(rmiWrapper));
        return rmiWrapper;
    }

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
                    new SimpleNamePasswordCallbackHandler(s_name, s_password));
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
        adminSubject.getPrincipals().add(search.getByName(AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER).get().narrow(LittlePrincipal.class));
        adminSubject.setReadOnly();

        // Do a little LoginContext sim - need to clean this up
        // Who am I running as now ?  What the frick ?
        Maybe<? extends Asset> maybeUser = search.getByName(s_name, SecurityAssetType.USER);
        if (!maybeUser.isSet()) {
            try {
                maybeUser = Subject.doAs(adminSubject, new PrivilegedExceptionAction<Maybe<Asset>>() {

                    @Override
                    public Maybe<Asset> run() throws BaseException, GeneralSecurityException, RemoteException {
                        for (AssetTreeTemplate.AssetInfo treeInfo : userTreeBuilder.get().user(s_name).build().visit(search.getByName("littleware.home", AssetType.HOME).get(), search)) {
                            if (!treeInfo.getAssetExists()) {
                                assetMgr.saveAsset(treeInfo.getAsset(), "Setup new user: " + s_name);
                            }
                        }
                        return search.getByName(s_name, SecurityAssetType.USER);
                    }
                });
            } catch (PrivilegedActionException ex) {
                log.log(Level.INFO, "Failed to setup new user", ex);
                handlePrivilegedException(ex);
                throw new AssertionFailedException("Should not make it here");
            }
        }

        j_caller.getPrincipals().add(maybeUser.get().narrow(LittleUser.class));
        /*... disable for now ...
        javax.security.auth.spi.LoginModule module = new PasswordDbLoginModule();
        module.initialize ( j_caller, 
        new SimpleNamePasswordCallbackHandler(s_name, s_password),
        new HashMap<String,String>(),
        new HashMap<String,String>()
        );
        module.login();
        module.commit ();
         */
        j_caller.setReadOnly();
        // ok - user authenticated ok by here - setup user session
        final LittleSession.Builder sessionBuilder = SecurityAssetType.SESSION.create();
        sessionBuilder.setId(UUID.randomUUID());
        sessionBuilder.setName(s_name + "_" + UUIDFactory.makeCleanString(sessionBuilder.getId()));
        sessionBuilder.setOwnerId(maybeUser.get().getId());
        sessionBuilder.setComment("User login");

        // Create the session asset as the admin user - session has null from-id
        final PrivilegedExceptionAction act_setup_session = new SetupSessionAction(sessionBuilder.build(), s_session_comment);
        try {
            return setupNewHelper((LittleSession) Subject.doAs(adminSubject, act_setup_session));
        } catch (PrivilegedActionException e) {
            handlePrivilegedException(e);
            throw new AssertionFailedException("Should not make it here");
        }
    }

    @Override
    public SessionHelper getSessionHelper(UUID u_session) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        // Note that the SessionHelper will take care of doing SessionExpired checks, etc.
        WeakReference<SessionHelper> ref_helper = sessionCache.get(u_session);

        if (null != ref_helper) {
            SessionHelper m_helper = ref_helper.get();
            if (null != m_helper) {
                // Make sure the sesion hasn't expired
                if (m_helper.getSession().getEndDate().getTime() > new Date().getTime()) {
                    return m_helper;
                } else {
                    throw new SessionExpiredException("Expired at: " + m_helper.getSession().getEndDate());
                }
            } else {
                sessionCache.remove(u_session);
            }
        }

        try {
            final LittleSession a_session = search.getAsset(u_session).get().narrow(LittleSession.class);
            return setupNewHelper(a_session);
        } catch (GeneralSecurityException e) {
            throw new AccessDeniedException("Caught unexpected: " + e, e);
        }
    }
}

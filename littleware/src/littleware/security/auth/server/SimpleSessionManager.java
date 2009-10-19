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

    private static final Logger olog_generic = Logger.getLogger(SimpleSessionManager.class.getName());
    private static final long serialVersionUID = 8144056326046717141L;
    private final AssetSearchManager om_search;
    private final AssetManager om_asset;
    private final ServiceProviderRegistry oreg_service;
    private final Map<UUID, WeakReference<SessionHelper>> ov_session_map = new HashMap<UUID, WeakReference<SessionHelper>>();
    private static SimpleSessionManager om_session = null;
    private final Sampler ostatSessionHelper = new SimpleSampler();
    private final Provider<UserTreeBuilder> provideUserTree;

    /**
     * Inject dependencies
     */
    @Inject
    public SimpleSessionManager(AssetManager m_asset, 
            AssetSearchManager m_search,
            ServiceProviderRegistry reg_service,
            Provider<UserTreeBuilder> provideUserTree
            ) throws RemoteException
    {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        om_asset = m_asset;
        om_search = m_search;
        if (null != om_session) {
            throw new IllegalStateException("SimpleSessionManager must be a singleton");
        }
        om_session = this;
        oreg_service = reg_service;
        this.provideUserTree = provideUserTree;
    }

    /**
     * Little auto-create user routine.
     *
     *
     * @param s_name
     * @return
     */
    private LittleUser createUser(String s_name) {
        throw new UnsupportedOperationException("Not yet implemented");
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
            Asset parent = om_search.getByName("littleware.home", AssetType.HOME).get();
            for (String childName : pathList) {
                final Maybe<Asset> maybe = om_search.getAssetFrom(parent.getObjectId(), childName);
                if (maybe.isSet()) {
                    parent = maybe.get();
                    continue;
                }
                Asset child = AssetType.createSubfolder(AssetType.GENERIC, childName, parent);
                parent = om_asset.saveAsset(child, os_session_comment);
            }
            osession.setFromId(parent.getObjectId());
            osession.setHomeId(parent.getHomeId());
            return om_asset.saveAsset(osession, os_session_comment);
        }
    }

    /**
     * Internal utility to setup RmiSessionHelper given a session asset
     *
     * @param a_session to setup and cache a new SessionHelper for
     */
    private SessionHelper setupNewHelper(LittleSession a_session) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Subject j_caller = a_session.getSubject(om_search);
        SessionHelper m_helper = new SimpleSessionHelper(a_session.getObjectId(), om_search, om_asset, this, oreg_service);
        InvocationHandler handler_helper = new SubjectInvocationHandler(j_caller, m_helper, ostatSessionHelper );
        SessionHelper m_proxy = (SessionHelper) Proxy.newProxyInstance(SessionHelper.class.getClassLoader(),
                new Class[]{SessionHelper.class},
                handler_helper);
        SessionHelper m_rmi = new RmiSessionHelper(m_proxy);
        ov_session_map.put(a_session.getObjectId(), new WeakReference(m_rmi));
        return m_rmi;
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
        } catch ( Exception ex ) {
            olog_generic.log(Level.INFO, "Assuming pass-through login - no littleware.login context available", ex);
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

        final Subject j_admin = new Subject();
        j_admin.getPrincipals().add(om_search.getByName(AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER).get());
        j_admin.setReadOnly();

        // Do a little LoginContext sim - need to clean this up
        // Who am I running as now ?  What the frick ?
        Maybe<LittleUser> maybeUser = om_search.getByName(s_name, SecurityAssetType.USER);
        if (!maybeUser.isSet()) {
            try {
                maybeUser = (Maybe<LittleUser>) Subject.doAs(j_admin, new PrivilegedExceptionAction<Maybe<LittleUser>>() {

                    @Override
                    public Maybe<LittleUser> run() throws BaseException, GeneralSecurityException, RemoteException {
                        for( AssetTreeTemplate.AssetInfo treeInfo : provideUserTree.get().user( s_name ).build().visit( om_search.getByName("littleware.home", AssetType.HOME).get(), om_search ) ) {
                            if ( ! treeInfo.getAssetExists() ) {
                                om_asset.saveAsset( treeInfo.getAsset(), "Setup new user: " + s_name );
                            }
                        }
                        return om_search.getByName(s_name, SecurityAssetType.USER);
                    }
                });
            } catch (PrivilegedActionException ex) {
                olog_generic.log(Level.INFO, "Failed to setup new user", ex);
                try {
                    throw ex.getCause();
                } catch (BaseException ex2) {
                    throw ex2;
                } catch (GeneralSecurityException ex2) {
                    throw ex2;
                } catch (RuntimeException ex2) {
                    throw ex2;
                } catch (Throwable ex2) {
                    throw new AssertionFailedException("Unexpected exception", ex2);
                }
            }
        }

        j_caller.getPrincipals().add(maybeUser.get());
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
        final LittleSession session = SecurityAssetType.SESSION.create();
        session.setObjectId(UUID.randomUUID());
        session.setName(s_name + "_" + UUIDFactory.makeCleanString(session.getObjectId()));
        session.setOwnerId(maybeUser.get().getObjectId());
        session.setComment("User login");

        // Create the session asset as the admin user - session has null from-id
        PrivilegedExceptionAction act_setup_session = new SetupSessionAction(session, s_session_comment);
        try {
            try {
                return setupNewHelper((LittleSession) Subject.doAs(j_admin, act_setup_session));
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
        } catch (BaseException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Caught :" + e, e);
        }
    }

    @Override
    public SessionHelper getSessionHelper(UUID u_session) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        // Note that the SessionHelper will take care of doing SessionExpired checks, etc.
        WeakReference<SessionHelper> ref_helper = ov_session_map.get(u_session);

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
                ov_session_map.remove(u_session);
            }
        }

        try {
            final LittleSession a_session = om_search.getAsset(u_session).get().narrow(LittleSession.class);
            return setupNewHelper(a_session);
        } catch (GeneralSecurityException e) {
            throw new AccessDeniedException("Caught unexpected: " + e, e);
        }
    }
}

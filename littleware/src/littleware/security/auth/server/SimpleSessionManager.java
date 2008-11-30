package littleware.security.auth.server;

import com.google.inject.Inject;
import com.google.inject.name.Named;
import java.rmi.*;
import java.rmi.registry.*;
//import java.rmi.server.UnicastRemoteObject;
import java.lang.reflect.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import javax.security.auth.*;
import java.lang.ref.WeakReference;

import java.net.URL;
import java.net.InetAddress;
import littleware.asset.*;
import littleware.base.*;
import littleware.security.*;
import littleware.security.auth.*;

/**
 * Simple implementation of SessionManager.
 * Hands of authentication to new LoginContext,
 * then passes the authenticated Subject onto the SessionHelper.
 * This class ought to be registered as a Singleton and exported
 * for RMI access.
 */
public class SimpleSessionManager extends LittleRemoteObject implements SessionManager {

    private static final Logger olog_generic = Logger.getLogger("littleware.security.auth.server.SimpleSessionManager");
    private static URL ourl_local = null;
    static {
        try {
            ourl_local = new URL("http://localhost/littleware/SimpleSessionManager");
            String s_hostname = InetAddress.getLocalHost().getHostName();
            ourl_local = new URL("http://" + s_hostname + "/littleware/SimpleSessionManager");
        } catch (java.io.IOException e) {
            olog_generic.log(Level.SEVERE, "Unable to cache hostname, caught: " + e);
        }
    }
    private final AssetSearchManager om_search;
    private final AssetManager om_asset;
    private final Map<UUID, WeakReference<SessionHelper>> ov_session_map = new HashMap<UUID, WeakReference<SessionHelper>>();

    private static SimpleSessionManager om_session = null;

    /**
     * Inject dependencies
     */
    @Inject
    public SimpleSessionManager(AssetManager m_asset, AssetSearchManager m_search ) throws RemoteException {
        //super( littleware.security.auth.SessionUtil.getRegistryPort() );
        om_asset = m_asset;
        om_search = m_search;
        if ( null != om_session ) {
            throw new IllegalStateException( "SimpleSessionManager must be a singleton" );
        }
        om_session = this;
    }

    /**
     * Privileged action creates a session-asset
     * as the user logging in.
     */
    private class SetupSessionAction implements PrivilegedExceptionAction<LittleSession> {

        private String os_session_comment = "No comment";
        private String os_name = "unknown";

        /**
         * Stash the comment to attach to the new session asset,
         * and the name of the user creating the session.
         */
        public SetupSessionAction(String s_name, String s_session_comment) {
            os_name = s_name;
            os_session_comment = s_session_comment;
        }

        public LittleSession run() throws Exception {
            LittleSession a_session = (LittleSession) SecurityAssetType.SESSION.create ();

            a_session.setName(os_name + ", " + a_session.getStartDate().getTime());
            a_session.setComment(os_session_comment);

            a_session = (LittleSession) om_asset.saveAsset ( a_session, os_session_comment );
            return a_session;
        }
    }

    /**
     * Internal utility to setup RmiSessionHelper given a session asset
     *
     * @param a_session to setup and cache a new SessionHelper for
     */
    private SessionHelper setupNewHelper(LittleSession a_session) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Subject j_caller = a_session.getSubject(om_search);
        SessionHelper m_helper = new SimpleSessionHelper(a_session.getObjectId(), om_search, om_asset, this);
        InvocationHandler handler_helper = new SessionInvocationHandler(j_caller, m_helper, ServiceType.SESSION_HELPER.getCallLogger(), ServiceType.SESSION_HELPER.getCallSampler(), m_helper);
        SessionHelper m_proxy = (SessionHelper) Proxy.newProxyInstance ( SessionHelper.class.getClassLoader (),
														 new Class[] { SessionHelper.class },
														 handler_helper
														 );
        SessionHelper m_rmi = new RmiSessionHelper(m_proxy);
        ov_session_map.put(a_session.getObjectId(), new WeakReference(m_rmi));
        return m_rmi;
    }

    public SessionHelper login(String s_name, String s_password, String s_session_comment) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        /*..
          Avoid chicken-and-egg problem with registering our own
          login config with our parent J2EE container.

        LoginContext x_login = new LoginContext("littleware.security.simplelogin", 
                                                new SimpleNamePasswordCallbackHandler(s_name, s_password)
                                                );
        Subject j_caller = x_login.getSubject();
        */

        // Do a little LoginContext sim - need to clean this up
        Subject j_caller = new Subject ();
        javax.security.auth.spi.LoginModule module = new PasswordDbLoginModule();
        module.initialize ( j_caller, 
                            new SimpleNamePasswordCallbackHandler(s_name, s_password),
                            new HashMap<String,String>(), 
                            new HashMap<String,String>()
                            );
        module.login();
        module.commit ();
        j_caller.setReadOnly ();
    
        PrivilegedExceptionAction act_setup_session = new SetupSessionAction(s_name, s_session_comment);
        try {
            LittleSession a_session = null;
            try {
                a_session = (LittleSession) Subject.doAs ( j_caller, act_setup_session );
            } catch (PrivilegedActionException e) {
                throw e.getException();
            }
            return setupNewHelper(a_session);
        } catch (BaseException e) {
            throw e;
        } catch (GeneralSecurityException e) {
            throw e;
        } catch (Exception e) {
            throw new DataAccessException("Caught :" + e, e);
        }
    }

    public SessionHelper getSessionHelper(UUID u_session) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        // Note that the SessionHelper will take care of doing SessionExpired checks, etc.
        WeakReference<SessionHelper> ref_helper = ov_session_map.get(u_session);

        if (null != ref_helper) {
            SessionHelper m_helper = ref_helper.get();
            if (null != m_helper) {
                // Make sure the sesion hasn't expireda
                if ( m_helper.getSession().getEndDate().getTime() < new Date().getTime () ) {
                    return m_helper;
                } else { 
                    throw new SessionExpiredException ();
                }
            }
        }

        try {
            LittleSession a_session = (LittleSession) om_search.getAsset ( u_session );
            return setupNewHelper(a_session);
        } catch (GeneralSecurityException e) {
            throw new AccessDeniedException("Caught unexpected: " + e, e);
        }
    }

    public URL getUrl() throws RemoteException {
        return ourl_local;
    }
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com

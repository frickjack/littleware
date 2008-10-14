package littleware.security.auth;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.*;

import littleware.asset.*;
import littleware.security.*;
import littleware.base.*;

/**
 * Proxy SessionHelper implementation that has brains to 
 * retry SessionHelper methods on RemoteException,
 * and auto-wrap Remote service interfaces returned by getServiceProvider()
 * with retry Dynamic proxies.
 * The retry code is tricky.  We assume that the 
 * remote server has died, so under the hood we trigger the
 * proxy SessionManager to reconnect (update its remote object reference),
 * then reset our own SessionHelper remote-object reference, and finally
 * the service dynamic proxy will refresh its reference.
 */
class SessionHelperProxy implements SessionHelper {

    private static final Logger olog_generic = Logger.getLogger( SessionHelperProxy.class.getName () );
    private SessionHelper  om_real = null;
    private SessionManager om_session = null;
    private UUID           ou_session = null;
    private RemoteExceptionHandler ohandler_remote =
            new RemoteExceptionHandler() {

                @Override
                public void handle(RemoteException e_remote) throws RemoteException {
                    super.handle(e_remote);
                    try {
                        om_real = om_session.getSessionHelper(ou_session);
                    } catch (Exception e) {
                        olog_generic.log(Level.WARNING, "Failure to re-establish session, caught: " + e);
                        throw e_remote;
                    }
                }
            };

    /**
     * Constructor stashes reference to SessionManagerProxy to
     * use to reset remote references if necessary,
     * 
     * @param m_real reference to helper to use
     * @param m_session proxy-wrapped SessionManager for reconnects
     * @param u_session ID of our SessionHelper session for reconnects
     */
    public SessionHelperProxy(SessionHelper m_real,
            SessionManagerProxy m_session,
            UUID u_session) {
        om_real = m_real;
        om_session = m_session;
        ou_session = u_session;
    }

    public LittleSession getSession() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return om_real.getSession();
            } catch (RemoteException e) {
                ohandler_remote.handle(e);
            }
        }
    }

    /**
     * Retry factory for freakin' resetting remote references
     * to Service handlers.
     */
    private class RemoteRetryInvocationHandler<T extends Remote> implements InvocationHandler {

        ServiceType<T> on_type = null;
        T om_service = null;
        InvocationHandler ohandler_proxy = null;

        public RemoteRetryInvocationHandler(T m_service,
                ServiceType<T> n_type) {
            om_service = m_service;
            on_type = n_type;
            if (Proxy.isProxyClass(om_service.getClass())) {
                ohandler_proxy = Proxy.getInvocationHandler(om_service);
            }
        }

        public Object invoke(Object proxy, Method method_call, Object[] v_args) throws Throwable {
            // Try to reset service reference on RemoteException
            RemoteExceptionHandler handler_retry = new RemoteExceptionHandler() {

                @Override
                public void handle(RemoteException e_remote) throws RemoteException {
                    super.handle(e_remote);
                    try {
                        ohandler_proxy = null;
                        om_service = om_real.getService(on_type);
                        if (Proxy.isProxyClass(om_service.getClass())) {
                            ohandler_proxy = Proxy.getInvocationHandler(om_service);
                        }
                    } catch (Exception e) {
                        // e already logged below
                        throw e_remote;
                    }
                }
            };

            while (true) { // Retry on RemoteException
                try {
                    if (null != ohandler_proxy) {
                        // An RMI reference is itself a Proxy - cannot use Method.invoke on that
                        return ohandler_proxy.invoke(proxy, method_call, v_args);
                    } else {
                        return method_call.invoke(om_service, v_args);
                    }
                } catch (RemoteException e) {
                    handler_retry.handle(e);
                } catch (IllegalAccessException e) {
                    throw new AssertionFailedException("Illegal access: " + e, e);
                } catch (InvocationTargetException e) {
                    Throwable err = e.getCause();

                    if (err instanceof RemoteException) {
                        handler_retry.handle((RemoteException) err);
                    } else if (err instanceof Exception) {
                        throw (Exception) err;
                    } else if (err instanceof Error) {
                        throw (Error) err;
                    } else {
                        throw new AssertionFailedException("Unexpected throwable error: " + e, e);
                    }
                }
            }
        }
    }
    private Map<ServiceType<?>, Object> ov_cache = new HashMap<ServiceType<?>, Object>();

    /**
     * Return a dynamic-proxy wrapper around the
     * returned service handler that will attempt to reset
     * the remote-reference if a RemoteException gets thrown.
     */
    public <T extends Remote> T getService(ServiceType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (n_type.equals(ServiceType.SESSION_HELPER)) {
            return (T) this;
        }


        T m_service = (T) ov_cache.get(n_type);
        if (null != m_service) {
            return m_service;
        }

        try {
            m_service = om_real.getService(n_type);
        } catch (RemoteException e) {
            om_real = om_session.getSessionHelper(ou_session);
            m_service = om_real.getService(n_type);
        }

        InvocationHandler handler_retry = new RemoteRetryInvocationHandler<T>(m_service, n_type);
        Class<T> class_service = n_type.getServiceInterface();
        olog_generic.log(Level.FINE, "Setting up service " + n_type + " proxy with interface " +
                class_service.toString() + " using object of class: " +
                m_service.getClass().toString());
        m_service = (T) Proxy.newProxyInstance(class_service.getClassLoader(),
                new Class[]{class_service},
                handler_retry);
        ov_cache.put(n_type, m_service);
        return m_service;
    }

    public SessionHelper createNewSession(String s_session_comment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return om_real.createNewSession(s_session_comment);
            } catch (RemoteException e) {
                ohandler_remote.handle(e);
            }
        }
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


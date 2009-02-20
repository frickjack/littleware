/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client;

import littleware.security.auth.*;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.*;

import java.util.ArrayList;
import java.util.List;
import littleware.asset.*;
import littleware.asset.client.LittleService;
import littleware.asset.client.ServiceListener;
import littleware.base.*;

/**
 * Proxy SessionHelper implementation that has brains to 
 * retry SessionHelper methods on RemoteException,
 * and auto-wrap Remote service interfaces returned by getServiceProvider()
 * with retry Dynamic proxies.
 *
 * The retry code is tricky.  We assume that the 
 * remote server has died, so under the hood we trigger the
 * proxy SessionManager to reconnect (update its remote object reference),
 * then reset our own SessionHelper remote-object reference, and finally
 * the service dynamic proxy will refresh its reference.
 *
 * Also, SessionHelperProxy implements logic so that each LittleServiceListener
 * registered on SessionHelperProxy is subsequently registered as a listener
 * on LittleServices returned by getService.
 */
public class SessionHelperProxy implements SessionHelperService {

    private static final Logger olog_generic = Logger.getLogger(SessionHelperProxy.class.getName());
    private static final long serialVersionUID = -1391174273951630071L;
    private SessionHelper om_real = null;
    private SessionManager om_session = null;
    private UUID ou_session = null;
    private transient RemoteExceptionHandler ohandler_remote =
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
    private class RemoteRetryInvocationHandler<T extends LittleService> implements InvocationHandler {

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
                    // super throws exception after max retries
                    super.handle(e_remote);
                    try {
                        ohandler_proxy = null;
                        /**
                         * ?? Why are we using om_real here - probably
                         * the remote SessionHelper has lost its connection too ...
                         */
                        om_service = getNewCoreService(on_type);
                        if (Proxy.isProxyClass(om_service.getClass())) {
                            ohandler_proxy = Proxy.getInvocationHandler(om_service);
                        }
                        /**
                         * TODO - come up with a better way to integrate
                         * LittleService, event listeners, and automatic retry
                         * on remote exception.
                         * This implementation assumes that every
                         * service has the same listener list as the SessionHelperProxy
                         */
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
    private Map<ServiceType<?>, LittleService> ov_cache = new HashMap<ServiceType<?>, LittleService>();

    /**
     * Return a dynamic-proxy wrapper around the
     * returned service handler that will attempt to reset
     * the remote-reference if a RemoteException gets thrown.
     */
    public <T extends LittleService> T getService(ServiceType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        /*..
        if (n_type.equals(ServiceType.SESSION_HELPER)) {
            return (T) this;
        }
               */
        T m_service = (T) ov_cache.get(n_type);
        if (null != m_service) {
            return m_service;
        }

        m_service = getNewCoreService( n_type );
        InvocationHandler handler_retry = new RemoteRetryInvocationHandler<T>(m_service, n_type);

        olog_generic.log(Level.FINE, "Setting up service " + n_type + " using object of class: " +
                m_service.getClass().toString());
        Class<T> class_service = n_type.getInterface();
        m_service = (T) Proxy.newProxyInstance(class_service.getClassLoader(),
                new Class[]{class_service},
                handler_retry);
        ov_cache.put(n_type, m_service);
        return m_service;
    }

    /**
     * Internal method always gets a new core service - does not go to cache,
     * or setup dynamic retry proxy, but does handle local retry
     * if internal remote SessionHelper stub has lost its connection.
     *
     * @return new non-proxied service with listeners registered.
     */
    private <T extends LittleService> T getNewCoreService(ServiceType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        T m_service = null;

        try {
            m_service = om_real.getService(n_type);
        } catch (RemoteException e) {
            om_real = om_session.getSessionHelper(ou_session);
            m_service = om_real.getService(n_type);
        }

        if (null == m_service) {
            throw new NullPointerException("What the frick?");
        }

        // Add listeners
        for ( ServiceListener listener : ovListener ) {
            m_service.addServiceListener( listener );
        }
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

    private transient List<ServiceListener> ovListener = new ArrayList<ServiceListener>();

    /**
     * Add listener to every LittleService in the cache
     *
     * @param listener
     */
    public void addServiceListener(ServiceListener listener) {
        if (!ovListener.contains(listener)) {
            ovListener.add(listener);
        }
        for ( LittleService service: ov_cache.values() ) {
            service.addServiceListener(listener);
        }
    }

    public void removeServiceListener(ServiceListener listener) {
        ovListener.remove( listener );
        for ( LittleService service: ov_cache.values() ) {
            service.removeServiceListener(listener);
        }
    }
}

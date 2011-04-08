/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client.internal;

import java.io.IOException;
import java.io.ObjectStreamException;
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
import littleware.asset.client.LittleServiceListener;
import littleware.base.*;
import littleware.security.auth.client.SessionHelperService;
import org.osgi.framework.BundleContext;

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

    private static final Logger log = Logger.getLogger(SessionHelperProxy.class.getName());
    private static final long serialVersionUID = -1391174273951630071L;
    private SessionHelper realHelper = null;
    private SessionManager sessionMgr = null;
    private UUID sessionId = null;
    private transient RemoteExceptionHandler remoteExceptionHelper;
    private transient List<LittleServiceListener> listenerList = new ArrayList<LittleServiceListener>();
    private Map<ServiceType<?>, LittleService> serviceCache = new HashMap<ServiceType<?>, LittleService>();
    private Maybe<BundleContext> maybeContext = Maybe.empty();

    private void initTransient() {
        remoteExceptionHelper = new RemoteExceptionHandler() {

            @Override
            public void handle(RemoteException e_remote) throws RemoteException {
                super.handle(e_remote);
                try {
                    realHelper = sessionMgr.getSessionHelper(sessionId);
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failure to re-establish session", ex);
                    throw e_remote;
                }
            }
        };
        listenerList = new ArrayList<LittleServiceListener>();
    }

    /**
     * Serialization support
     *
     * @param in
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        initTransient();
        in.defaultReadObject();
    }

    /**
     * Serialization support
     *
     * @param in
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    private void readObjectNoData() throws ObjectStreamException {
        initTransient();
    }

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
        realHelper = m_real;
        sessionMgr = m_session;
        sessionId = u_session;
        initTransient();
    }

    @Override
    public LittleSession getSession() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return realHelper.getSession();
            } catch (RemoteException e) {
                remoteExceptionHelper.handle(e);
            }
        }
    }

    @Override
    public void start(BundleContext ctx) throws Exception {
        this.maybeContext = Maybe.something(ctx);
        for (LittleService service : serviceCache.values()) {
            try {
                service.start(ctx);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to start service", ex);
            }
        }
    }

    @Override
    public void stop(BundleContext ctx) throws Exception {
        for (LittleService service : serviceCache.values()) {
            try {
                service.stop(ctx);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to stop service", ex);
            }
        }
    }

    /**
     * Retry factory for freakin' resetting remote references
     * to Service handlers.
     */
    private class RemoteRetryInvocationHandler<T extends LittleService> implements InvocationHandler {

        private final ServiceType<T> serviceType;
        private  T theService = null;
        /**
         * It's possible we're wrapping something that's already wrapped ... ugh.
         */
        private InvocationHandler proxyHandler = null;

        public RemoteRetryInvocationHandler(T m_service,
                ServiceType<T> n_type) {
            theService = m_service;
            serviceType = n_type;
            if (Proxy.isProxyClass(theService.getClass())) {
                proxyHandler = Proxy.getInvocationHandler(theService);
            }
        }

        @Override
        public Object invoke(Object proxy, Method method_call, Object[] v_args) throws Throwable {
            // Try to reset service reference on RemoteException
            final RemoteExceptionHandler retryHandler = new RemoteExceptionHandler() {

                @Override
                public void handle(RemoteException remoteEx) throws RemoteException {
                    // super throws exception after max retries
                    super.handle(remoteEx);
                    // try to reset the connection
                    try {
                        proxyHandler = null;
                        /**
                         * ?? Why are we using om_real here - probably
                         * the remote SessionHelper has lost its connection too ...
                         */
                        if ((null != theService) && maybeContext.isSet()) {
                            try {
                                final LittleService service = theService;
                                theService = null;
                                service.stop(maybeContext.get());
                            } catch (Exception ex) {
                                log.log(Level.WARNING, "Service shutdown failed", ex);
                            }
                        }
                        theService = getNewCoreService(serviceType);
                        if (Proxy.isProxyClass(theService.getClass())) {
                            proxyHandler = Proxy.getInvocationHandler(theService);
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
                        throw remoteEx;
                    }
                }
            };

            while (true) { // Retry on RemoteException
                try {
                    if (null != proxyHandler) {
                        // An RMI reference is itself a Proxy - cannot use Method.invoke on that
                        return proxyHandler.invoke(proxy, method_call, v_args);
                    } else {
                        return method_call.invoke(theService, v_args);
                    }
                } catch (RemoteException ex) {
                    retryHandler.handle(ex);
                } catch (NullPointerException ex) {
                    // running into issue with proxy throwing NullPointerException after laptop resume,
                    // so retry on NullPointerException like a RemoteException
                    retryHandler.handle( new RemoteException( "Service is null?", ex ) );
                } catch (IllegalAccessException e) {
                    throw new AssertionFailedException("Illegal access: " + e, e);
                } catch (InvocationTargetException e) {
                    final Throwable cause = e.getCause();

                    if (cause instanceof RemoteException) {
                        retryHandler.handle((RemoteException) cause);
                    } else if (cause instanceof Exception) {
                        throw (Exception) cause;
                    } else if (cause instanceof Error) {
                        throw (Error) cause;
                    } else {
                        throw new AssertionFailedException("Unexpected throwable error: " + e, e);
                    }
                }
            }
        }
    }

    /**
     * Return a dynamic-proxy wrapper around the
     * returned service handler that will attempt to reset
     * the remote-reference if a RemoteException gets thrown.
     */
    @Override
    public <T extends LittleService> T getService(ServiceType<T> n_type) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        /*..
        if (n_type.equals(ServiceType.SESSION_HELPER)) {
        return (T) this;
        }
         */
        T m_service = (T) serviceCache.get(n_type);
        if (null != m_service) {
            return m_service;
        }

        m_service = getNewCoreService(n_type);
        final InvocationHandler handler_retry = new RemoteRetryInvocationHandler<T>(m_service, n_type);

        log.log(Level.FINE, "Setting up service {0} using object of class: {1}", new Object[]{n_type, m_service.getClass().toString()});
        final Class<T> class_service = n_type.getInterface();
        m_service = (T) Proxy.newProxyInstance(class_service.getClassLoader(),
                new Class[]{class_service},
                handler_retry);
        serviceCache.put(n_type, m_service);
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
            m_service = realHelper.getService(n_type);
        } catch (RemoteException e) {
            final SessionHelper helper = sessionMgr.getSessionHelper(sessionId);
            if (helper instanceof SessionHelperProxy) {
                realHelper = ((SessionHelperProxy) helper).realHelper;
            } else {
                realHelper = helper;
            }
            m_service = realHelper.getService(n_type);
        }

        if (null == m_service) {
            throw new NullPointerException("What the frick?");
        }

        // Add listeners
        for (LittleServiceListener listener : listenerList) {
            m_service.addServiceListener(listener);
        }
        // Register with client side OSGi environment
        if (maybeContext.isSet()) {
            try {
                m_service.start(maybeContext.get());
            } catch (Exception ex) {
                throw new IllegalStateException("Client environment not setup propertly");
            }
        }
        return m_service;
    }

    @Override
    public SessionHelper createNewSession(String s_session_comment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return realHelper.createNewSession(s_session_comment);
            } catch (RemoteException e) {
                remoteExceptionHelper.handle(e);
            }
        }
    }

    @Override
    public String getServerVersion() throws RemoteException {
        while (true) {
            try {
                return realHelper.getServerVersion();
            } catch (RemoteException e) {
                remoteExceptionHelper.handle(e);
            }
        }
    }


    /**
     * Add listener to every LittleService in the cache
     *
     * @param listener
     */
    @Override
    public void addServiceListener(LittleServiceListener listener) {
        if (!listenerList.contains(listener)) {
            listenerList.add(listener);
        }
        for (LittleService service : serviceCache.values()) {
            service.addServiceListener(listener);
        }
    }

    @Override
    public void removeServiceListener(LittleServiceListener listener) {
        listenerList.remove(listener);
        for (LittleService service : serviceCache.values()) {
            service.removeServiceListener(listener);
        }
    }
}

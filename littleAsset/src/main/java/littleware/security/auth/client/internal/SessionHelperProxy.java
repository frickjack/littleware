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
    private SessionManagerProxy sessionMgr = null;
    private UUID sessionId = null;
    private transient RemoteExceptionHandler remoteExceptionHelper;
    private transient List<LittleServiceListener> listenerList = new ArrayList<LittleServiceListener>();
    private Map<ServiceType<?>, LittleService> serviceCache = new HashMap<ServiceType<?>, LittleService>();
    private Maybe<BundleContext> maybeContext = Maybe.empty();

    /**
     * The BundleContext doesn't get set until OSGi starts
     * SesionHelperProxy as a BundleActivator.
     * This method allows the child RetryProxyHandler's to gain
     * access to that context.
     */
    Maybe<BundleContext> getBundleContext() {
        return maybeContext;
    }

    private void initTransient() {
        remoteExceptionHelper = new RemoteExceptionHandler() {

            @Override
            public void handle(RemoteException remoteException) throws RemoteException {
                super.handle(remoteException);
                try {
                    SessionHelperProxy.this.realHelper = sessionMgr.getSessionHelper(sessionId).realHelper;
                } catch (Exception ex) {
                    log.log(Level.WARNING, "Failure to re-establish session", ex);
                    throw remoteException;
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
     * @param realHelper reference to helper to use
     * @param sessionMgr proxy-wrapped SessionManager for reconnects
     * @param sessionId ID of our SessionHelper session for reconnects
     */
    public SessionHelperProxy(SessionHelper realHelper,
            SessionManagerProxy sessionMgr,
            UUID sessionId) {
        this.realHelper = realHelper;
        this.sessionMgr = sessionMgr;
        this.sessionId = sessionId;
        initTransient();
    }

    @Override
    public LittleSession getSession() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return realHelper.getSession();
            } catch (RemoteException ex ) {
                remoteExceptionHelper.handle(ex );
            } catch ( NullPointerException ex ) {
                remoteExceptionHelper.handle( new RemoteException( "Unexpected exception", ex ) );
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
     * Return a dynamic-proxy wrapper around the
     * returned service handler that will attempt to reset
     * the remote-reference if a RemoteException gets thrown.
     */
    @Override
    public <T extends LittleService> T getService(ServiceType<T> serviceType) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        /*..
        if (n_type.equals(ServiceType.SESSION_HELPER)) {
        return (T) this;
        }
         */
        T theService = (T) serviceCache.get(serviceType);
        if (null != theService) {
            return theService;
        }

        theService = getNewCoreService(serviceType);
        final InvocationHandler handler_retry = new RetryProxyHandler<T>( theService, serviceType, this );

        log.log(Level.FINE, "Setting up service {0} using object of class: {1}", new Object[]{serviceType, theService.getClass().toString()});
        final Class<T> class_service = serviceType.getInterface();
        theService = (T) Proxy.newProxyInstance(class_service.getClassLoader(),
                new Class[]{class_service},
                handler_retry);
        serviceCache.put(serviceType, theService);
        return theService;
    }

    /**
     * Internal method always gets a new core service - does not go to cache,
     * or setup dynamic retry proxy, but does handle local retry
     * if internal remote SessionHelper stub has lost its connection.
     *
     * @return new non-proxied service with listeners registered.
     */
    <T extends LittleService> T getNewCoreService(ServiceType<T> serviceType) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        T serviceProvider = null;

        try {
            serviceProvider = realHelper.getService(serviceType);
        } catch (Exception ex) {
            log.log( Level.WARNING, "Proxy failed to retrieve service: " + serviceType, ex );
            realHelper = sessionMgr.getSessionHelper(sessionId).realHelper;
            serviceProvider = realHelper.getService(serviceType);
        }

        if (null == serviceProvider) {
            throw new NullPointerException("What the frick?");
        }

        // Add listeners
        for (LittleServiceListener listener : listenerList) {
            serviceProvider.addServiceListener(listener);
        }
        // Register with client side OSGi environment
        if (maybeContext.isSet()) {
            try {
                serviceProvider.start(maybeContext.get());
            } catch (Exception ex) {
                throw new IllegalStateException("Client environment not setup propertly");
            }
        }
        return serviceProvider;
    }

    @Override
    public SessionHelper createNewSession(String sesionComment)
            throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        while (true) {
            try {
                return realHelper.createNewSession(sesionComment);
            } catch (RemoteException ex ) {
                remoteExceptionHelper.handle(ex );
            } catch ( NullPointerException ex ) {
                remoteExceptionHelper.handle( new RemoteException( "Unexpected exception", ex ) );
            }
        }
    }

    @Override
    public String getServerVersion() throws RemoteException {
        while (true) {
            try {
                return realHelper.getServerVersion();
            } catch (RemoteException ex ) {
                remoteExceptionHelper.handle(ex );
            } catch ( NullPointerException ex ) {
                remoteExceptionHelper.handle( new RemoteException( "Unexpected exception", ex ) );
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

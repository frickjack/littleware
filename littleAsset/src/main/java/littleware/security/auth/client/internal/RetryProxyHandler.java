/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.client.internal;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.client.LittleService;
import littleware.base.AssertionFailedException;
import littleware.base.RemoteExceptionHandler;
import littleware.security.auth.ServiceType;
import org.osgi.framework.BundleContext;

/**
 * Retry factory for freakin' resetting remote references
 * to Service handlers.
 */
class RetryProxyHandler<T extends LittleService> implements InvocationHandler {
    private static final Logger log = Logger.getLogger( RetryProxyHandler.class.getName() );

    private final ServiceType<T> serviceType;
    private final SessionHelperProxy helper;
    private T theService = null;
    /**
     * It's possible we're wrapping something that's already wrapped ... ugh.
     */
    private InvocationHandler proxyHandler = null;

    public RetryProxyHandler(T theService,
            ServiceType<T> serviceType,
            SessionHelperProxy helper) {
        this.theService = theService;
        this.serviceType = serviceType;
        this.helper = helper;
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
                    // Clear the current proxy reference, then reset
                    proxyHandler = null;
                    if (null != theService) {
                        try {
                            final LittleService service = theService;
                            theService = null;
                            final BundleContext bundleContext = helper.getBundleContext().getOr(null);
                            if (null != bundleContext) {
                                service.stop(bundleContext);
                            }
                        } catch (Exception ex) {
                            log.log(Level.WARNING, "Service shutdown failed", ex);
                        }
                    }
                    theService = helper.getNewCoreService(serviceType);
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
                retryHandler.handle(new RemoteException("Service is null?", ex));
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

/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.net;

import java.net.URI;
import java.net.URISyntaxException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.base.AssertionFailedException;

/**
 * Retry factory for freakin' resetting remote references
 * to Service handlers.
 */
public class RemoteRetryHelper<T> extends RemoteExceptionHandler {

    private static final Logger log = Logger.getLogger(RemoteRetryHelper.class.getName());
    private final URI servicePath;
    private T    service = null;

    public RemoteRetryHelper(
            String pathIn) {
        try {
            this.servicePath = new URI( pathIn );
        } catch (URISyntaxException ex) {
            throw new IllegalArgumentException( "Invalid service path: " + pathIn, ex );
        }
        log.log( Level.FINE, "Service lookup path set for: {0}", servicePath );
    }

    public URI getServicePath() {
        return servicePath;
    }

    /**
     * Get the service - invoke lookupService the first time,
     * then cache result for subsequent calls
     */
    public T getLazy() {
        if ( null == service ) {
            try {
                service = lookupService();
            } catch (Exception ex) {
                throw new AssertionFailedException( "Failed service lookup", ex );
            } 
        }
        return service;
    }

    /**
     * Do a registry lookup, and return the SessionManager.
     * If the SessionManager is remote - then return a proxy
     * that auto-retries on RemoteException.  Also caches the value
     * for the next call to getLazy
     */
    public T lookupService() throws RemoteException, NotBoundException {
        while (true) {
            // Retry on RemoteException
            try {
                final Registry reg = LocateRegistry.getRegistry( servicePath.getHost(), servicePath.getPort(), LittleRemoteObject.getClientSockFactory() );
                service = (T) reg.lookup( servicePath.getPath().replaceAll( "^/+", "" ) );
                //service = (T) Naming.lookup(servicePath);  //"//" + s_host + ":" + i_port + "/littleware/SessionManager");
                return service;
            } catch (RemoteException ex) {
                handle(ex);
            } 
        }
    }

    /**
     * Subtypes should handle(ex) a RemoteException in a loop
     * that retrieves the service handle via getLazy:
     *
     * <pre>
     * while( true ) {
     *     try {
     *         final Service service = getLazy();
     *         service.doSomething();
     *     } catch ( RemoteException ex ) {
     *         handle( ex );
     *     }
     * }
     * </pre>
     */
    @Override
    public void handle( RemoteException ex ) throws RemoteException {
        super.handle( ex );
        service = null;
    }
}
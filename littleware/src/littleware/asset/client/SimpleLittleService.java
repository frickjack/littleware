/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import java.io.IOException;
import java.io.ObjectStreamException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceEvent;
import org.osgi.framework.ServiceListener;
import org.osgi.framework.ServiceReference;

/**
 * Simple implementation of LittleTool
 * intended as utility for LittleTool implementation classes.
 */
public class SimpleLittleService implements LittleService {

    private static final Logger log = Logger.getLogger(SimpleLittleService.class.getName());
    private static final long serialVersionUID = -1343921475014296291L;
    private transient List<LittleServiceListener> ovListener = new ArrayList<LittleServiceListener>();
    // Don't use final when object needs to be serializable
    private LittleService oxSource = this;

    /**
     * Serialization support
     *
     * @param in
     * @throws java.io.IOException
     * @throws java.lang.ClassNotFoundException
     */
    private void readObject(java.io.ObjectInputStream in) throws IOException, ClassNotFoundException {
        ovListener = new ArrayList<LittleServiceListener>();
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
        ovListener = new ArrayList<LittleServiceListener>();
    }

    /**
     * Constructor stashes the source-object that the various fire* methods
     * should construct events against.
     */
    public SimpleLittleService(LittleService xSource) {
        oxSource = xSource;
    }

    /** Constructor uses this as the source */
    public SimpleLittleService() {
        oxSource = this;
    }

    @Override
    public void addServiceListener(LittleServiceListener listener) {
        if (!ovListener.contains(listener)) {
            ovListener.add(listener);
        }
    }

    @Override
    public void removeServiceListener(LittleServiceListener listener) {
        ovListener.remove(listener);
    }

    /**
     * Invoke notify() on each LittleServiceListener registered with this object.
     *
     * @param event to notify the listeners of
     */
    public void fireServiceEvent(LittleServiceEvent event) {
        if (event.getSource() != oxSource) {
            throw new IllegalArgumentException("source mismatch");
        }

        for (LittleServiceListener listener : ovListener) {
            listener.receiveServiceEvent(event);
        }
    }

    protected LittleService getSource() {
        return oxSource;
    }
    private ClientCache cache = new NullClientCache();

    /**
     * Access the client-side cache if available
     */
    protected ClientCache getCache() {
        return cache;
    }
    private static int cacheCount = 0;

    /**
     * Little hook just for testing ...
     */
    public static int getCacheCount() {
        return cacheCount;
    }

    /** Loads the ClientCache from the execution context */
    @Override
    public void start(final BundleContext ctx) throws Exception {
        ctx.registerService(LittleService.class.getName(), this, new Properties());
        final ServiceListener listener = new ServiceListener() {

            @Override
            public void serviceChanged(ServiceEvent event) {
                switch (event.getType()) {
                    case ServiceEvent.REGISTERED: {
                        log.log(Level.FINE, "Listener processing service registration");
                        final ServiceReference ref = event.getServiceReference();
                        final ClientCache service = (ClientCache) ctx.getService(ref);
                        if (null != service) {
                            cache = service;
                            if (!(service instanceof NullClientCache)) {
                                ++cacheCount;
                            }
                        }
                    }
                    break;
                }
            }
        };
        final String filter = "(objectclass=" + ClientCache.class.getName() + ")";
        ctx.addServiceListener(listener, filter);
        final ServiceReference[] osgiRefs = ctx.getServiceReferences(null, filter);
        if (null != osgiRefs) {
            for (ServiceReference ref : osgiRefs) {
                listener.serviceChanged(new ServiceEvent(ServiceEvent.REGISTERED, ref));
            }
        }
    }

    /** NOOP */
    @Override
    public void stop(BundleContext arg0) throws Exception {
    }
}


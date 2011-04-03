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

import org.osgi.framework.BundleActivator;

/**
 * Interface marks a littleware service provider smart proxy on
 * the client side.  Wraps a remote stub, and fires ServiceEvents
 * to service listeners like a client-side cache that wants to
 * be informed and updated whenever a littleware service
 * modifies or loads data from the Asset repository.
 * The LittleService also implements the BundleActivator interface.
 * The client-side OSGi environment invokes start/stop
 * when the LittleService enters the client environment
 * post deserialization.
 */
public interface LittleService extends java.io.Serializable, BundleActivator {

    /**
     * Register a listener for service events.
     * Noop if listener is already registered as a listener.
     *
     * @param listener to add
     */
    public void addServiceListener(LittleServiceListener listener);

    /**
     * Remove the given listener.
     *
     * @param listen_action to remove
     */
    public void removeServiceListener(LittleServiceListener listener);
}

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
import java.util.logging.Logger;


/**
 * Simple implementation of LittleTool
 * intended as utility for LittleTool implementation classes.
 */
public class SimpleLittleService implements LittleService {

    private static final Logger olog_generic = Logger.getLogger(SimpleLittleService.class.getName());
    private static final long serialVersionUID = -1343921475014296291L;


    private transient List<ServiceListener> ovListener = new ArrayList<ServiceListener>();
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
        ovListener = new ArrayList<ServiceListener>();
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
        ovListener = new ArrayList<ServiceListener>();
    }

    /**
     * Constructor stashes the source-object that the various fire* methods
     * should construct events against.
     */
    public SimpleLittleService(LittleService xSource) {
        oxSource = xSource;
    }
    /** Constructor uses this as the source */
    public SimpleLittleService () {
        oxSource = this;
    }

    public void addServiceListener(ServiceListener listener ) {
        if (!ovListener.contains(listener)) {
            ovListener.add(listener);
        }
    }

    public void removeServiceListener(ServiceListener listener) {
        ovListener.remove(listener);
    }



    /**
     * Invoke notify() on each ServiceListener registered with this object.
     *
     * @param event to notify the listeners of
     */
    public void fireServiceEvent(ServiceEvent event) {
        if (event.getSource() != oxSource) {
            throw new IllegalArgumentException("source mismatch");
        }

        for (ServiceListener listener : ovListener) {
            listener.receiveServiceEvent(event);
        }
    }

    protected LittleService getSource() {
        return oxSource;
    }
}


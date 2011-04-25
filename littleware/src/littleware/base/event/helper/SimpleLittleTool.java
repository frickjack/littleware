/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.event.helper;

import littleware.base.event.LittleListener;
import littleware.base.event.LittleTool;
import littleware.base.event.LittleEvent;
import com.google.common.collect.ImmutableList;
import java.util.*;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;


/** 
 * Simple implementation of LittleTool
 * intended as utility for LittleTool implementation classes.
 */
public class SimpleLittleTool extends PropertyChangeSupport implements LittleTool {

    private static final Logger log = Logger.getLogger(SimpleLittleTool.class.getName());
    private static final long serialVersionUID = 6774179690353597372L;
    private List<LittleListener> listenerList = new ArrayList<LittleListener>();
    private final Object source;


    /**
     * Constructor stashes the source-object that the various fire* methods
     * should construct events against.
     */
    public SimpleLittleTool(Object x_source) {
        super(x_source);
        source = x_source;
    }

    @Override
    public synchronized void addLittleListener(LittleListener listen_little) {
        if (!listenerList.contains(listen_little)) {
            listenerList.add(listen_little);
        }
    }

    @Override
    public synchronized void removeLittleListener(LittleListener listen_little) {
        listenerList.remove(listen_little);
    }

    /**
     * Runnable to throw onto the swing-event dispatch thread
     * to notify registered LittleListener listeners.
     */
    private static class DispatchHandler implements Runnable {
        private final List<LittleListener> listenerList;
        private final LittleEvent oevent_little;

        /** Stash away the event to dispatch */
        public DispatchHandler(LittleEvent event_little, List<LittleListener> listenerList ) {
            oevent_little = event_little;
            this.listenerList = ImmutableList.copyOf( listenerList );
        }

        /**
         * Invoke notify() on each LittleListener
         */
        @Override
        public void run() {
            for (LittleListener listen_little : listenerList ) {
                listen_little.receiveLittleEvent(oevent_little);
            }
        }
    }

    /**
     * Invoke notify() on each LittleListener registered with this object.
     * Does this asynchronously by pushing an event onto the
     * swing-event dispatch thread if not invoked from the deispatch thread.
     *
     * @param event_little to notify the listeners of
     */
    public void fireLittleEvent(LittleEvent event_little) {
        if (event_little.getSource() != source) {
            throw new IllegalArgumentException("source mismatch");
        }

        final Runnable run_dispatch;
        synchronized (this ) {
            run_dispatch = new DispatchHandler(event_little, listenerList );
        }
        run_dispatch.run();
    }

    /**
     * SimpleLittleTool may be a delegate for a source bean implementing LittleTool.
     * That source bean should be the source of events fired by SimpleLittleTool and
     * its subtypes.
     * Return the stashed reference to that source bean.
     */
    protected Object getSourceBean() {
        return source;
    }
}


package littleware.apps.swingclient;

import java.util.*;
import javax.swing.SwingUtilities;
import java.beans.PropertyChangeSupport;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.AssertionFailedException;

/** 
 * Simple implementation of LittleTool
 * intended as utility for LittleTool implementation classes.
 */
public class SimpleLittleTool extends PropertyChangeSupport implements LittleTool {

    private static final Logger olog_generic = Logger.getLogger(SimpleLittleTool.class.getName());
    /**
     * Test whether the SwingDispatchThread is available.
     * Swing dispatch thread is not available is some environments 
     * like J2EE app server.
     */
    private final static boolean ob_swing_enabled;

    static {
        boolean b_swing = false;
        try {
            SwingUtilities.isEventDispatchThread();
            b_swing = true;
        } catch (Throwable e) {
            olog_generic.log(Level.INFO, "Swing not available for SimpleLittleTool event dispatch");
        }
        ob_swing_enabled = b_swing;
    }
    private List<LittleListener> ov_listener = new ArrayList<LittleListener>();
    private final Object ox_source;

    /**
     * Constructor stashes the source-object that the various fire* methods
     * should construct events against.
     */
    public SimpleLittleTool(Object x_source) {
        super(x_source);
        ox_source = x_source;
    }

    public void addLittleListener(LittleListener listen_little) {
        if (!ov_listener.contains(listen_little)) {
            ov_listener.add(listen_little);
        }
    }

    public void removeLittleListener(LittleListener listen_little) {
        ov_listener.remove(listen_little);
    }

    /**
     * Runnable to throw onto the swing-event dispatch thread
     * to notify registered LittleListener listeners.
     */
    private class DispatchHandler implements Runnable {

        private LittleEvent oevent_little = null;

        /** Stash away the event to dispatch */
        public DispatchHandler(LittleEvent event_little) {
            oevent_little = event_little;
        }

        /**
         * Invoke notify() on each LittleListener
         */
        public void run() {
            for (LittleListener listen_little : ov_listener) {
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
        if (event_little.getSource() != ox_source) {
            throw new IllegalArgumentException("source mismatch");
        }

        final Runnable run_dispatch = new DispatchHandler(event_little);
        if ((!ob_swing_enabled) || SwingUtilities.isEventDispatchThread()) {
            run_dispatch.run();
        } else {
            SwingUtilities.invokeLater(run_dispatch);
        }
    }

    /**
     * SimpleLittleTool may be a delegate for a source bean implementing LittleTool.
     * That source bean should be the source of events fired by SimpleLittleTool and
     * its subtypes.
     * Return the stashed reference to that source bean.
     */
    protected Object getSourceBean() {
        return ox_source;
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


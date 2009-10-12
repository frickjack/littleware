/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.client;

import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.lang.ref.WeakReference;
import javax.swing.SwingUtilities;

/**
 * Provide some SwingUtilities-like functions.
 */
public class SwingAdapter {

    private static final SwingAdapter singleton = new SwingAdapter();

    public static SwingAdapter get() {
        return singleton;
    }

    /**
     * Use weak reference trick to avoid issues with
     * defunct listeners preventing garbage collection.
     */
    private static class LittleWrapper extends WeakReference implements LittleListener {
        private final LittleTool listenTo;

        public LittleWrapper(LittleListener listener, LittleTool listenTo ) {
            super( listener );
            if (listener instanceof LittleWrapper) {
                throw new IllegalArgumentException("listener already wrapped");
            }
            this.listenTo = listenTo;
        }

        @Override
        public void receiveLittleEvent(final LittleEvent event_little) {
            final LittleListener listener = (LittleListener) super.get();
            if ( null == listener ) {
                listenTo.removeLittleListener(this);
                return;
            }
            if (SwingUtilities.isEventDispatchThread()) {
                listener.receiveLittleEvent(event_little);
            } else {
                SwingUtilities.invokeLater(new Runnable() {

                    @Override
                    public void run() {
                        listener.receiveLittleEvent(event_little);
                    }
                });
            }
        }
    }

    private static class PropertyWrapper extends WeakReference implements PropertyChangeListener {
        private final PropertyChangeSupport listenTo;
        
        public PropertyWrapper( PropertyChangeListener listener, PropertyChangeSupport listenTo ) {
            super( listener );
            if ( listener instanceof PropertyWrapper ) {
                throw new IllegalArgumentException( "listener already wrapped" );
            }
            this.listenTo = listenTo;
        }

        @Override
        public void propertyChange(final PropertyChangeEvent event) {
            final PropertyChangeListener listener = (PropertyChangeListener) super.get();
            if ( null == listener ) {
                listenTo.removePropertyChangeListener(listener);
                return;
            }
            if (SwingUtilities.isEventDispatchThread()) {
                listener.propertyChange(event);
            } else {
                SwingUtilities.invokeLater(new Runnable() {
                    @Override
                    public void run() {
                        listener.propertyChange( event );
                    }
                });
            }
        }
    }


    /**
     * Wrap the given listener in an adapter that ensures that
     * the listener receives its events on the dispatch thread,
     * extends WeakReference for garbage collection, and
     * self adds to the given PropertyChangeSupport
     *
     * @param listener
     * @return listener wrapped if necessary
     */
    public void dispatchWrap(LittleListener listener, LittleTool listenTo ) {
        if (listener instanceof LittleWrapper) {
            listenTo.addLittleListener(  listener );
        } else {
            listenTo.addLittleListener( new LittleWrapper(listener, listenTo ) );
        }
    }


    /**
     * Wrap the given listener in an adapter that ensures that
     * the listener receives its events on the dispatch thread,
     * extends WeakReference for garbage collection, and
     * self adds to the given PropertyChangeSupport
     *
     * @param listener
     * @param listenTo to add to
     * @return listener wrapped if necessary
     */
    public void dispatchWrap(PropertyChangeListener listener, PropertyChangeSupport listenTo ) {
        if (listener instanceof PropertyWrapper) {
            listenTo.addPropertyChangeListener( listener );
        } else {
            listenTo.addPropertyChangeListener( new PropertyWrapper(listener, listenTo ) );
        }
    }

}

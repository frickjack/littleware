/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.client;

import java.beans.PropertyChangeListener;


/** 
 * Interface marks a UI element that allows registration of LittleListeners
 * that receive notification of UI triggered events via LittleEvent type objects.
 * Also includes registration of PropertyChangeListeners that receive
 * PropertyChangeEvents as a result of invoking setter methods on LittleTool beans.
 * It is possible for a UI session to result in both LittleEvent and PropertyChangeEvent
 * firing.  For example - user clicks on a button to navigate in a browser -
 * causing the browser to throw a NavRequestEvent (subtype of LittleEvent).
 * The BrowserController receives the NavRequestEvent, and invokes setAssetModel() on
 * the browser to set the Browser to view a new model - the call to setAssetModel()
 * causes a PropertyChangeEvent to fire.
 * In general a controller should listen for LittleEvents fired due to
 * user interaction with the UI, and interdependent UI components should listen
 * for PropertyChangeEvents resulting when controllers set properities
 * on the UI components in response to the user action.
 */
public interface LittleTool {
	/**
	 * Register a listener for LoginOkEvent and LoginFailedEvent
	 * events triggered when the GUI calls SessionManager.login()
	 * and SessionManager.getSessionHelper ().
	 * Noop if listen_action is already registered as a listener.
	 *
	 * @param listen_action to add
	 */
	public void	addLittleListener( LittleListener listen_action ) ;
	
	/**
	 * Remove the given listener.
	 *
	 * @param listen_action to remove
	 */
	public void     removeLittleListener( LittleListener listen_action ) ;
    
    /**
     * Allow observers to listen for property changes 
     *
     * @param listen_props listener that wants informed when a setter gets invoked on this object
     */
    public void addPropertyChangeListener( PropertyChangeListener listen_props );
    
    /**
     * Allow observers to stop listening for changes
     *
     * @param listen_props to stop sending events to
     */
    public void removePropertyChangeListener( PropertyChangeListener listen_props );    
}

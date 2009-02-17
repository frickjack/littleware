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

/**
 * Interface marks a littleware service provider smart proxy on
 * the client side.  Wraps a remote stub, and fires ServiceEvents
 * to service listeners like a client-side cache that wants to
 * be informed and updated whenever a littleware service
 * modifies or loads data from the Asset repository.
 */
public interface LittleService extends java.io.Serializable {
	/**
	 * Register a listener for LoginOkEvent and LoginFailedEvent
	 * events triggered when the GUI calls SessionManager.login()
	 * and SessionManager.getSessionHelper ().
	 * Noop if listen_action is already registered as a listener.
	 *
	 * @param listen_action to add
	 */
	public void	addServiceListener( ServiceListener listener ) ;

	/**
	 * Remove the given listener.
	 *
	 * @param listen_action to remove
	 */
	public void     removeServiceListener( ServiceListener listener );
}

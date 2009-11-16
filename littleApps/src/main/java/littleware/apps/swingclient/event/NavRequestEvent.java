/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.event;

import java.util.UUID;
import littleware.base.feedback.LittleEvent;

/**
 * Event triggered to indicate a user request to navigate
 * to an asset with the given ID.
 * Extends littleEvent - getOperation() returns "navigate_2_asset",
 * and getResult returns getDestinationId
 */
public class NavRequestEvent extends LittleEvent {
    private static final String   OS_OPERATION = "navigate_to_asset";
    
    public enum NavMode {
        GENERIC,     // whatever the default is
        NEW_WINDOW,  // user indications nav to new window
        NEW_TAB,     
        OTHER
    };
    
    private NavMode  on_mode = NavMode.GENERIC;
	
	/**
     * Setup the NavRequestEvent
	 *
	 * @param x_source of the event
     * @param u_destination id of the asset the user wants to navigate to
     * @param n_mode hint on how the user wants to navigation to go
	 */
	public NavRequestEvent ( Object x_source, UUID u_destination, NavMode n_mode ) {
		super ( x_source, OS_OPERATION, u_destination );
        on_mode = n_mode;
	}
	
	

	/**
     * Get the UUID of the naviagtion-destination
	 */
	public UUID getDestination () { return (UUID) getResult (); }
    
    /**
     * Get the NavMode
     */
    public NavMode getNavMode () { return on_mode; }
}


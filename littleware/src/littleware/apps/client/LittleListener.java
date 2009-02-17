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

/**
 * Interface for handlers/controllers of LittleEvent type events
 * thrown by LittleToo UI elements.
 */
public interface LittleListener extends java.util.EventListener {
	/**
	 * Notify this listener of the occurrence of a LittleEvent type event
	 *
	 * @param event_little that took place
	 */
	public void receiveLittleEvent ( LittleEvent event_little );
}


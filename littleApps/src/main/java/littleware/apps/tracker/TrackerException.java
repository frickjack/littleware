/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import littleware.asset.AssetException;


/**
 * Base Asset exception
 */
public class TrackerException extends AssetException {
    
	/** Goofy default constructor */
	public TrackerException () {
		super ( "Tracker system exception" );
	}
	
	/** Constructor with user-supplied message */
	public TrackerException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public TrackerException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}


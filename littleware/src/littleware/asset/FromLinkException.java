/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

/**
 * May not save an asset linking FROM an asset of type AssetType.LINK
 */
public class FromLinkException extends AssetException {
    
	/** Goofy default constructor */
	public FromLinkException () {
		super ( "may not link FROM asset of type AssetType.LINK" );
	}
	
	/** Constructor with user-supplied message */
	public FromLinkException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public FromLinkException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}


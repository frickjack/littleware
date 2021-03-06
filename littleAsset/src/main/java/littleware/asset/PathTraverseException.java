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
 * Base AssetPath traverse exception
 */
public class PathTraverseException extends AssetException {
    
	/** Goofy default constructor */
	public PathTraverseException () {
		super ( "Exception traversing an AssetPath" );
	}
	
	/** Constructor with user-supplied message */
	public PathTraverseException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public PathTraverseException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}


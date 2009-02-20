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
 * Exception thrown when trying to update an asset
 * so that it links FROM an asset under a different home,
 * or is create the asset on a different server that the home
 * exists on.
 */
public class HomeIdException extends AssetException {
	
	/** Goofy default constructor */
	public HomeIdException () {
		super ( "Asset must have same Home as FROM asset, and be on the HOME server" );
	}
	
	/** Constructor with user-supplied method */
	public HomeIdException ( String s_message ) {
		super ( s_message );
	}
}


/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset;


/**
 * Attempt to save out of date asset data
 */
public class AssetSyncException extends AssetException {
    
	/** Goofy default constructor */
	public AssetSyncException () {
		super ( "Attempt to save out of date data" );
	}
	
	/** Constructor with user-supplied message */
	public AssetSyncException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public AssetSyncException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}


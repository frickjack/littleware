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

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


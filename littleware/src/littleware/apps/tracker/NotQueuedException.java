package littleware.apps.tracker;


/**
 * Tried to queue the same task twice
 */
public class NotQueuedException extends TrackerException {
    
	/** Goofy default constructor */
	public NotQueuedException () {
		super ( "Tracker system exception" );
	}
	
	/** Constructor with user-supplied message */
	public NotQueuedException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public NotQueuedException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


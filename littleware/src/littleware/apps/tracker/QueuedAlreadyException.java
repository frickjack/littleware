package littleware.apps.tracker;



/**
 * Tried to queue the same task twice
 */
public class QueuedAlreadyException extends TrackerException {
    
	/** Goofy default constructor */
	public QueuedAlreadyException () {
		super ( "Tracker system exception" );
	}
	
	/** Constructor with user-supplied message */
	public QueuedAlreadyException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public QueuedAlreadyException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


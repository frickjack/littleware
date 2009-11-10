package littleware.apps.tracker;


/**
 * Operation not consistent with some TaskStatus
 */
public class TaskStatusException extends TrackerException {
    
	/** Goofy default constructor */
	public TaskStatusException () {
		super ( "Inconsistent TaskStatus" );
	}
	
	/** Constructor with user-supplied message */
	public TaskStatusException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public TaskStatusException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


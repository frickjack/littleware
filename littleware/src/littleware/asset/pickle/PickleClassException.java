package littleware.asset.pickle;

import littleware.base.BaseException;

/**
 * Thrown when attempting to load a PickleMaker
 * for a class that does not have a valid
 * handler registered
 */
public class PickleClassException extends BaseException {
    /** Default constructor */
    public PickleClassException () {
	    super ( "Pickle class mismatch" );
    }
	
    /** Constructor with message */
    public PickleClassException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public PickleClassException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }
		
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


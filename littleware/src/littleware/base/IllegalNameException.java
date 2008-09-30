package littleware.base;


/**
 * Thing with requested name already exists
 */
public class IllegalNameException extends BaseException {
    /** Default constructor */
    public IllegalNameException () {
	    super ( "IllegalName exception" );
    }
	
    /** Constructor with message */
    public IllegalNameException ( String s_message ) {
	    super ( s_message );
    }

	/** Constructor with message and cause */
    public IllegalNameException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


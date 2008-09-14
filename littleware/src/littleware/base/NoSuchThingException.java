package littleware.base;


/**
 * Thing with requested name already exists
 */
public class NoSuchThingException extends BaseException {
    /** Default constructor */
    public NoSuchThingException () {
	    super ( "NoSuchThing exception" );
    }
	
    /** Constructor with message */
    public NoSuchThingException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public NoSuchThingException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


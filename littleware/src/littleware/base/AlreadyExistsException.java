package littleware.base;


/**
 * Thing with requested name already exists
 */
public class AlreadyExistsException extends BaseException {
    /** Default constructor */
    public AlreadyExistsException () {
	    super ( "AlreadyExistsexception" );
    }
	
    /** Constructor with message */
    public AlreadyExistsException ( String s_message ) {
	    super ( s_message );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


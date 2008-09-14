package littleware.base;


/**
 * Failure to parse blobs of string data 
 */
public class ParseException extends BaseException {
    /** Default constructor */
    public ParseException () {
	    super ( "Parse exception" );
    }
	
    /** Constructor with message */
    public ParseException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public ParseException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


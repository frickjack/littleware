package littleware.base;


/**
 * Data supplied to some method exceeds a size limit.
 */
public class TooBigException extends ParseException {
    /** Default constructor */
    public TooBigException () {
	    super ( "Data too big" );
    }
	
    /** Constructor with message */
    public TooBigException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public TooBigException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


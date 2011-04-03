package littleware.security.auth;


/**
 * Throw when the user session has expired
 */
public class SessionExpiredException extends RuntimeException {
    /** Default constructor */
    public SessionExpiredException () {
	    super ( "SessionExpired exception" );
    }
	
    /** Constructor with message */
    public SessionExpiredException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public SessionExpiredException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


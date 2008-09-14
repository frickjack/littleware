package littleware.security;


/**
 * Supplied principal is not an authenticated user certificate
 */
public class NotAuthenticatedException extends ManagerException {
    /** Default constructor */
    public NotAuthenticatedException () {
	    super ( "NotAuthenticated exception" );
    }
	
    /** Constructor with message */
    public NotAuthenticatedException ( String s_message ) {
	    super ( s_message );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


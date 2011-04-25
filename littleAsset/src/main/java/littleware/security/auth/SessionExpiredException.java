/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

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


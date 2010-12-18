package littleware.security;

import java.security.GeneralSecurityException;

/**
 * Group-manipulation/access support exception
 */
public abstract class ManagerException extends GeneralSecurityException {
    /** Default constructor */
    public ManagerException () {
	    super ( "Manager manipulation exception" );
    }

    /** Constructor with message */
    public ManagerException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public ManagerException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


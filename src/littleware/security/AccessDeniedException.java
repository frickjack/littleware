package littleware.security;


/**
 * Supplied principal does not have permission to carry out requested action
 */
public class AccessDeniedException extends ManagerException {
    /** Default constructor */
    public AccessDeniedException () {
	    super ( "AccessDenied exception" );
    }
	
    /** Constructor with message */
    public AccessDeniedException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public AccessDeniedException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


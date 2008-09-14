package littleware.security;


/**
 * Quota violation exception
 */
public class QuotaException extends ManagerException {
    /** Default constructor */
    public QuotaException () {
	    super ( "Quota exceeded" );
    }
	
    /** Constructor with message */
    public QuotaException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public QuotaException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.security;

/**
 * Generic security access exception
 *
 * $Header: /Volumes/pasquini/Code/cvsRoot/littleware/src/littleware/security/AccessException.java,v 1.4 2007/01/24 04:12:31 pasquini Exp $
 */
public class AccessException extends SecurityException {
    /** Default constructor */
    public AccessException () {
		super ( "Exception in littleware.base package" );
    }

    /** Sweet P says hello */
    public AccessException ( String s_message ) {
		super ( s_message );
    }

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.com


package littleware.base;


/**
 * Base class for unchecked exceptions in the littleware packages
 */
public abstract class BaseRuntimeException extends RuntimeException {
    /** Default constructor */
    public BaseRuntimeException () {
        super ( "Exception in littleware.base package" );
    }
    
    /** With message */
    public BaseRuntimeException ( String s_message ) {
		super ( s_message );
    }
    
	/** Exception cascading */
	public BaseRuntimeException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


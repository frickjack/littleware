package littleware.base;


/**
 * Exception thrown when multiple instances of Singleton created,
 * or on attempt to access uninitialized Singleton.
 */
public class SingletonException extends BaseRuntimeException {
    /** Default constructor */
    public SingletonException () {
        super ( "Exception in littleware.base package" );
    }
    
    /** With message */
    public SingletonException ( String s_message ) {
		super ( s_message );
    }
    
	/** Exception cascading */
	public SingletonException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


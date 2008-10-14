package littleware.base;

/**
 * Exception for the littleware.base.Factory interface
 */
public class FactoryException extends BaseRuntimeException {
    /** Default constructor */
    public FactoryException () {
	    super ( "Factory exception" );
    }

    /** Constructor with message */
    public FactoryException ( String s_message ) {
		super ( s_message );
    }
	
	/** Constructor with message and cause */
    public FactoryException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


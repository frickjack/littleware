package littleware.base;

/**
 * Exception thrown on failure to interact with the database
 * in the expected way.
 */
public class DataAccessException extends BaseException {
    /** Default constructor */
    public DataAccessException () {
		super ( "DataAccessException" );
    }
	
    /** Constructor with message */
    public DataAccessException ( String s_message ) {
		super ( s_message );
    }
	
	/** Exception chaining */
	public DataAccessException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


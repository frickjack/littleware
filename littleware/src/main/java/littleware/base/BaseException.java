package littleware.base;

import java.io.*;

/**
 * Base class for exceptions in the littleware packages
 */
public abstract class BaseException extends Exception {
    /** Default constructor */
    public BaseException () {
        super ( "Exception in littleware.base package" );
    }

    /** With message */
    public BaseException ( String s_message ) {
		super ( s_message );
    }
    
	/** Exception cascading */
	public BaseException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
    
    /**
     * Little utility takes an exception, prints the stack track to a string, 
     * and returns that string.
     */
     public static String getStackTrace ( Throwable e ) {
        try {
            StringWriter io_string = new StringWriter ();
			PrintWriter  io_print = new PrintWriter ( io_string );
			
			e.printStackTrace ( io_print );
			for ( Throwable e_cause = e.getCause ();
				  null != e_cause;
				  e_cause = e_cause.getCause ()
				  ) {
				io_print.println ( "... caused by: " );
				e_cause.printStackTrace ( io_print );
			}
            return io_string.toString ();
        } catch ( Exception e2 ) {
            return "Failed to retrieve stack trace for: " + e + ", caught: " + e2;
        }
     }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


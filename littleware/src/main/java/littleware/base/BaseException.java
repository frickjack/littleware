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
    public BaseException ( String message ) {
		super ( message );
    }
    
	/** Exception cascading */
	public BaseException ( String message, Throwable causedBy ) {
		super ( message, causedBy );
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
			for ( Throwable causedBy = e.getCause ();
				  null != causedBy;
				  causedBy = causedBy.getCause ()
				  ) {
				io_print.println ( "... caused by: " );
				causedBy.printStackTrace ( io_print );
			}
            return io_string.toString ();
        } catch ( Exception e2 ) {
            return "Failed to retrieve stack trace for: " + e + ", caught: " + e2;
        }
     }
}

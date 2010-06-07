package littleware.web.applet;



/**
 * Exception for missing/bad applet parameter condition
 */
public class AppletParameterException extends LittleAppletException {
    /** Default constructor */
    public AppletParameterException () {
	    super ( "Littleware applet exception" );
    }
    
    /** Constructor with message */
    public AppletParameterException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public AppletParameterException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


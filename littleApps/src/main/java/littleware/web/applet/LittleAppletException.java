package littleware.web.applet;

import littleware.base.BaseException;


/**
 * Base class for littleware.web.applet originating exceptions.
 */
public class LittleAppletException extends BaseException {
    /** Default constructor */
    public LittleAppletException () {
	    super ( "Littleware applet exception" );
    }
    
    /** Constructor with message */
    public LittleAppletException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public LittleAppletException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


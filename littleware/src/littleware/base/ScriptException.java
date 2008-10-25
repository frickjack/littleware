package littleware.base;


/**
 * Some problem with ScriptRunner script evaluation or whatever
 */
public class ScriptException extends BaseException {
    /** Default constructor */
    public ScriptException () {
	    super ( "NoSuchThing exception" );
    }
	
    /** Constructor with message */
    public ScriptException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public ScriptException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


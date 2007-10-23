package littleware.base;

/**
 * Exception for the littleware.base.Command interface
 */
public class CommandException extends BaseException {
    /** Default constructor */
    public CommandException () {
		super ( "Command exception" );
    }
	
    /** Constructor with message */
    public CommandException ( String s_message ) {
		super ( s_message );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


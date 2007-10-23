package littleware.browser;

/**
 * Exception thrown on failure to parse a browser command-string.
 */
public class BrowserParseException extends BrowserException {
	/** Call through to super class */
	public BrowserParseException ( String s_message ) {
		super ( s_message );
	}
	
	/** Call through to the super class */
	public BrowserParseException () {
		super ();
	}

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


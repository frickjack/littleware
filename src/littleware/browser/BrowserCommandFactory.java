package littleware.browser;

/**
 * Interface for browser-command factory.
 */
public class BrowserCommandFactory {
	/**
	 * Parse a given command string into a BrowserCommand
	 * that's ready to be issued against a BrowserModel.
	 *
	 * @param s_command to parse
	 * @param x_browser_model to setup commands against (pull environment from, etc.)
	 * @exception BrowserParserException on failure to construct command
	 */
	public BrowserCommand parseCommand ( String s_command, BrowserModel x_browser_model ) throws BrowserParseException;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


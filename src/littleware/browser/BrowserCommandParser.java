package littleware.browser;

/**
 * Simple parser interface constructs
 * BrowserCommand objects from command strings.
 * May be implemented as a collection of BrowserCommandFactories.
 */
public interface BrowserCommandParser extends BrowserCommandFactory {

	/**
	 * Parse a given command string into a BrowserCommand
	 * that's ready to be issued against a BrowserModel.
	 *
	 * @param s_command to parse
	 * @param x_browser_model to setup commands against (pull environment from, etc.)
	 * @exception BrowserParserException on failure to construct command
	 */
	public BrowserCommand parseCommand ( String s_command, BrowserModel x_browser_model ) throws BrowserParseException;
	
	/**
	 * Same as above, but use implicit internal BrowserModel reference
	 *
	 * @param s_command to parse
	 * @exception BrowserParserException on failure to construct command
	 */	
	public BrowserCommand parseCommand ( String s_command ) throws BrowserParseException;
	
	/**
	 * Get a read-only instance of the command dictionary.
	 */
	public SortedMap<String,BrowserCommandFactory> getCommandDictionary ();
	
	/**
	 * Optional operation to allow extention of a BrowserCommandParser
	 * implementation with new commands.
	 *
	 * @param s_prefix that uniquely identifies the command - must be all alpha-numeric
	 * @param x_command_factory that can parse the command-line for the given command.
	 * @exception AlreadyExistsException if the specified prefix is already registered
	 * @exception IllegalNameException if the specified prefix is illegal
	 */
	public void addCommand ( String s_prefix, BrowserCommandFactory x_command_factory )
		throws AlreadyExistsException, IllegalNameException;

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


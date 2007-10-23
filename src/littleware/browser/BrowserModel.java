package littleware.browser;

import java.util.*;
import javax.swing.tree.*;

import littleware.asset.*;
import littleware.base.*;


/**
 * Asset browser controller - manages updates to a browser data-model.  
 * Different browser views may register as observers of
 * subtypes of this frickjack.
 */
public abstract class BrowserModel extends Observable {
	/**
	 * Data-type passed to notifyObserver
	 */
	public enum EventType {
		CWD_HISTORY,
		COMMAND_HISTORY,
		ENVIRONMENT,
		ASSET_TREE,
		CLIPBOARD,
		BUFFERS,
		SELECTIONS
	};
	
	/**
	 * Execute the given command, and add it to
	 * the history.  Notify observers of history update.
	 */
	public void issueCommand ( //BrowserCommand x_command 
							   ) throws BrowserException, GeneralSecurityException, AssetException;
	
	
	/**
	 * Get an unmodifiable view of the command history
	 */
	public List<BrowserCommand> getHistory ();
	
	/**
	 * Get an unmodifiable view of the command-environment
	 */
	public Map<String,String> getEnvironment ();
	
	/**
	 * Set given environment, and notify observers
	 */
	public void setEnvironment ( String s_key, String s_value ) throws IllegalNameException;
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


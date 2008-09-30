package littleware.asset;

/**
 * Request for non-existent AssetType
 */
public class NoSuchTypeException extends AssetException {
    
	/** Goofy default constructor */
	public NoSuchTypeException () {
		super ( "No such AssetType" );
	}
	
	/** Constructor with user-supplied message */
	public NoSuchTypeException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public NoSuchTypeException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


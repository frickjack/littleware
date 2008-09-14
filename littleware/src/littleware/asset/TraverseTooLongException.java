package littleware.asset;

/**
 * Traversing too many assets along AssetPath
 */
public class TraverseTooLongException extends PathTraverseException {
    
	/** Goofy default constructor */
	public TraverseTooLongException () {
		super ( "Too many assets along AssetPath" );
	}
	
	/** Constructor with user-supplied message */
	public TraverseTooLongException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public TraverseTooLongException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


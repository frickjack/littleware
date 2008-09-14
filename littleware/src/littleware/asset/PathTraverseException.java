package littleware.asset;

/**
 * Base AssetPath traverse exception
 */
public class PathTraverseException extends AssetException {
    
	/** Goofy default constructor */
	public PathTraverseException () {
		super ( "Exception traversing an AssetPath" );
	}
	
	/** Constructor with user-supplied message */
	public PathTraverseException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public PathTraverseException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.asset;

/**
 * Traversing too many AssetType.LINKs at a segment along an AssetPath
 */
public class LinkLimitException extends PathTraverseException {
    
	/** Goofy default constructor */
	public LinkLimitException () {
		super ( "Too many links between AssetPath segments" );
	}
	
	/** Constructor with user-supplied message */
	public LinkLimitException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public LinkLimitException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


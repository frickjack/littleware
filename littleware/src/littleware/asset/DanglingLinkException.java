package littleware.asset;

/**
* Traversing too many AssetType.LINKs at a segment along an AssetPath
 */
public class DanglingLinkException extends PathTraverseException {
    
	/** Goofy default constructor */
	public DanglingLinkException () {
		super ( "AssetType.LINK with unresolved TO asset" );
	}
	
	/** Constructor with user-supplied message */
	public DanglingLinkException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public DanglingLinkException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


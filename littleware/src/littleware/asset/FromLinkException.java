package littleware.asset;

/**
 * May not save an asset linking FROM an asset of type AssetType.LINK
 */
public class FromLinkException extends AssetException {
    
	/** Goofy default constructor */
	public FromLinkException () {
		super ( "may not link FROM asset of type AssetType.LINK" );
	}
	
	/** Constructor with user-supplied message */
	public FromLinkException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public FromLinkException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


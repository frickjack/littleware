package littleware.asset;

/**
 * Exception thrown when trying to update an asset
 * so that it links FROM an asset under a different home,
 * or is create the asset on a different server that the home
 * exists on.
 */
public class HomeIdException extends AssetException {
	
	/** Goofy default constructor */
	public HomeIdException () {
		super ( "Asset must have same Home as FROM asset, and be on the HOME server" );
	}
	
	/** Constructor with user-supplied method */
	public HomeIdException ( String s_message ) {
		super ( s_message );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


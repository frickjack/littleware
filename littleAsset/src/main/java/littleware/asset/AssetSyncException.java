package littleware.asset;



/**
 * Attempt to save out of date asset data
 */
public class AssetSyncException extends AssetException {
    
	/** Goofy default constructor */
	public AssetSyncException () {
		super ( "Attempt to save out of date data" );
	}
	
	/** Constructor with user-supplied message */
	public AssetSyncException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public AssetSyncException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


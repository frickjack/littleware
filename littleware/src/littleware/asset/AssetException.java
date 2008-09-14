package littleware.asset;

import littleware.base.BaseException;


/**
 * Base Asset exception
 */
public abstract class AssetException extends BaseException {

	/** Goofy default constructor */
	public AssetException () {
		super ( "Asset system exception" );
	}
	
	/** Constructor with user-supplied message */
	public AssetException ( String s_message ) {
		super ( s_message );
	}
	
	/** Propagating constructor */
	public AssetException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


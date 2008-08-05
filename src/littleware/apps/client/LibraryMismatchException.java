package littleware.apps.client;

import littleware.base.BaseRuntimeException;

/**
 * Attempt to switch a view between AssetModels belonging
 * to different AssetModelLibrary.
 * We restrict that - since too many things need an AssetModelLibrary
 * injected into it - yet we still don't want to make the
 * AssetModelLibrary as singleton.
 * Most apps will only have one AssetModelLibrary anyway -
 * just a few weirdos that want to mainitain multiple sessions
 * each with their own library within the same application.
 */
public class LibraryMismatchException extends BaseRuntimeException {
	/** Default constructor */
	public LibraryMismatchException () {
		super ( "Assertion failed" );
	}
	
	/** With message */
	public LibraryMismatchException ( String s_message ) {
		super ( s_message );
	}
	
	/** Chaining exceptions */
	public LibraryMismatchException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
    
}
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


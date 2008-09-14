package littleware.apps.filebucket;

import littleware.base.BaseException;

/** 
 * Exception baseclass for misc problems accessing
 * asset file-bucket data.
 */
public class BucketException extends BaseException {
    /** Default constructor */
    public BucketException () {
        super ( "Exception in littleware.apps.filebucket package" );
    }
    
    /** With message */
    public BucketException ( String s_message ) {
		super ( s_message );
    }
    
	/** Exception cascading */
	public BucketException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


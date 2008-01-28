package littleware.apps.filebucket;

import littleware.base.BaseException;

/** 
 * Exception baseclass for misc problems accessing
 * asset file-bucket data.
 */
public class IllegalBucketPathException extends BucketException {
    /** Default constructor */
    public IllegalBucketPathException () {
        super ( "Illegal pathname requested for file in a bucket" );
    }
    
    /** With message */
    public IllegalBucketPathException ( String s_message ) {
		super ( s_message );
    }
    
	/** Exception cascading */
	public IllegalBucketPathException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


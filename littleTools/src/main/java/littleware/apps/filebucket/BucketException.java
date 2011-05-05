/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

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
/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

/**
 * Exception thrown on failure to interact with the database
 * in the expected way.
 */
public class DataAccessException extends BaseException {
    private static final long serialVersionUID = -6189364069023559650L;
    /** Default constructor */
    public DataAccessException () {
		super ( "DataAccessException" );
    }
	
    /** Constructor with message */
    public DataAccessException ( String s_message ) {
		super ( s_message );
    }
	
	/** Exception chaining */
	public DataAccessException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
	}
}


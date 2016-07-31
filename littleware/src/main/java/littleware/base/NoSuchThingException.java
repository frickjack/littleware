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
 * Thing with requested name already exists
 */
public class NoSuchThingException extends BaseException {
    private static final long serialVersionUID = 924747789617037240L;
    /** Default constructor */
    public NoSuchThingException () {
	    super ( "NoSuchThing exception" );
    }
	
    /** Constructor with message */
    public NoSuchThingException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public NoSuchThingException ( String s_message, Throwable e_cause ) {
		super ( s_message, e_cause );
    }
	
}

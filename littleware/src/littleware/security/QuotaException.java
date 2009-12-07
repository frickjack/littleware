/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security;


/**
 * Quota violation exception
 */
public class QuotaException extends ManagerException {
    /** Default constructor */
    public QuotaException () {
	    super ( "Quota exceeded" );
    }
	
    /** Constructor with message */
    public QuotaException ( String s_message ) {
	    super ( s_message );
    }
	
	/** Constructor with message and cause */
    public QuotaException ( String s_message, Throwable e_cause ) {
	    super ( s_message, e_cause );
    }
	
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


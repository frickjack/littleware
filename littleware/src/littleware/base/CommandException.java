/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

/**
 * Exception for the littleware.base.Command interface
 */
public class CommandException extends BaseException {
    private static final long serialVersionUID = -2855414034809102866L;
    /** Default constructor */
    public CommandException () {
		super ( "Command exception" );
    }
	
    /** Constructor with message */
    public CommandException ( String s_message ) {
		super ( s_message );
    }
}




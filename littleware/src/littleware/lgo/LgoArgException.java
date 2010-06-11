/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.lgo;

import littleware.lgo.LgoException;

/**
 * Specialization of LgoException to throw when a command does not execute
 * because it was given invalid arguments or input.
 */
public class LgoArgException extends LgoException {
    public LgoArgException() {}
    public LgoArgException ( String sMessage ) {
        super( sMessage );
    }
    public LgoArgException( String sMessage, Throwable throwCause ) {
        super( sMessage, throwCause );
    }
}

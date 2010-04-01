/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base;

/**
 * Exception thrown by attempt to lazy-load data after
 * some core data set has already been loaded.
 */
public class LazyLoadException extends BaseRuntimeException {

    public LazyLoadException(){}
    
    public LazyLoadException( String message, Throwable cause ) {
        super( message, cause );
    }
}
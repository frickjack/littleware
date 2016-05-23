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
 * Thrown on attempt to load more data than a system maximum allows
 */
public class TooMuchDataException extends BaseException {
    private static final long serialVersionUID = -2328338423877096212L;

    /**
     * Creates a new instance of <code>TooMuchDataException</code> without detail message.
     */
    public TooMuchDataException() {
    }


    /**
     * Constructs an instance of <code>TooMuchDataException</code> with the specified detail message.
     * @param msg the detail message.
     */
    public TooMuchDataException(String msg) {
        super(msg);
    }
    
    public TooMuchDataException( String msg, Throwable cause ) {
        super( msg, cause );
    }
}

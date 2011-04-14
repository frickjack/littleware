/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.validate;

import java.util.concurrent.Callable;

/**
 * Thrown on failed validation
 */
public class ValidationException extends RuntimeException {
    public ValidationException(){}
    public ValidationException( String message ) {
        super( message );
    }
    public ValidationException( String message, Throwable cause ) {
        super( message, cause );
    }

    /**
     * Convenience factory method - throws ValidationException if ! mustBeTrue
     *
     * @param mustBeTrue test
     * @param message exception message
     */
    public static void validate( boolean mustBeTrue, String message ) {
        if ( ! mustBeTrue ) {
            throw new ValidationException( message );
        }
    }

    public static void validate( boolean mustBeTrue, Callable<String> messageThunk ) {
        if ( ! mustBeTrue ) {
            String message;
            try {
                message = messageThunk.call();
            } catch ( Exception ex ) {
                message = ex.getMessage();
            }
            throw new ValidationException( message );
        }
    }
}

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
 * Base implementation for a compound validator
 */
public abstract class AbstractValidator implements Validator {
    /**
     * Utility wrapps validator.validate() in try {} catch {},
     * returns Maybe( exception.getMessage() ) if exception caught,
     * otherwise Maybe.empty() if validation ok.
     */
    public static Maybe<String> check( Validator validator ) {
        try {
            validator.validate();
            return Maybe.empty();
        } catch ( Exception ex ) {
            return Maybe.something( ex.getMessage() );
        }
    }
    
    /**
     * Utility throws new ValidationException( message ) if ! test
     */
    public static void assume( boolean test, String message ) throws ValidationException {
        if ( ! test ) {
            throw new ValidationException( message );
        }
    }
}

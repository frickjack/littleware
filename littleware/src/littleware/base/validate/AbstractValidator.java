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

import java.util.Collection;
import littleware.base.Whatever;


/**
 * Base implementation for a compound validator
 */
public abstract class AbstractValidator implements Validator {
    @Override
    public final void validate() throws ValidationException {
        final Collection<String> errors = checkIfValid();
        if ( ! errors.isEmpty() ) {
            final StringBuilder sb = new StringBuilder();
            for( String message : errors ) {
                sb.append( message ).append( Whatever.NEWLINE );
            }
            throw new ValidationException( sb.toString() );
        }
    }

    protected ValidatorUtil.Helper buildErrorTracker() {
        return ValidatorUtil.helper();
    }
}

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
     * Just call validate and throw exception on failure
     */
    @Override
    public final void validateOrFail() {
        if ( ! validate() ) {
            throw new ValidationException();
        }
    }
}

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
 * Standard interface for validation test
 */
public interface Validator {

    /**
     * @return true if validation succeeds and ready to call build()
     */
    public boolean validate();

    /**
     * if ( ! validate ) { throw ValidationException; }
     *
     * @exception ValidateException on validation failure
     */
    public void validateOrFail();
}
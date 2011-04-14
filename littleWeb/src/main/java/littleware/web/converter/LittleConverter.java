/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.converter;

/**
 * Unit test friendly converter interface
 */
public interface LittleConverter<T> {
    /**
     * Type unsafe version of getAsString
     * @param value
     * @return getAsString(value)
     */
    public String pickle( Object value );
    public String getAsString( T value );
    public T getAsObject( String value );
}

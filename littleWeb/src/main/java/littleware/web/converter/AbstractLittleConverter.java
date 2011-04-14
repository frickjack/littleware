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
 * Abstract converter with default implementation of toString converter
 */
public abstract class AbstractLittleConverter<T> implements LittleConverter<T> {
    @Override
    public final String pickle( Object value ) {
        return getAsString( (T) value );
    }
    
    @Override
    public String getAsString(T value) {
        return value.toString();
    }

}

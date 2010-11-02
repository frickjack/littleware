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

import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;

/**
 * Simple LittleConverter wrapper
 */
public class AbstractJsfConverter implements Converter {
    private final LittleConverter<?> converter;

    public AbstractJsfConverter( LittleConverter<?> converter ) {
        this.converter = converter;
    }


    public Object getAsObject(FacesContext fc, UIComponent uic, String value) {
        return converter.getAsObject( value );
    }

    public String getAsString(FacesContext fc, UIComponent uic, Object value ) {
        return converter.pickle( value );
    }

}

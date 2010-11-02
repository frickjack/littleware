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

import java.util.UUID;
import javax.faces.convert.FacesConverter;


@FacesConverter( forClass=UUID.class, value="littleware.web.converter.UUID")
public class JsfUuidConverter extends AbstractJsfConverter {
    public JsfUuidConverter() {
        super( new UuidConverter() );
    }
}

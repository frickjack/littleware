/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.client;

import java.util.EventObject;

/**
 * Base class for LittleService observer events
 */
public class ServiceEvent extends EventObject {
    private static final long serialVersionUID = 8882466619235817165L;

    public ServiceEvent( LittleService source ) {
        super( source );
    }
}

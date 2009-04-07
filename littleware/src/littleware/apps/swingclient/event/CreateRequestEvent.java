/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.event;

import littleware.apps.client.*;

/**
 * Event triggered to indicate a user request to create an asset
 */
public class CreateRequestEvent extends LittleEvent {
    private static final String   OS_OPERATION = "CreateRequestEvent";
    private static final long serialVersionUID = -5621375774508626054L;
    
    private final AssetModel  oamodel_view;
	
	/**
     * Setup the CreateRequestEvent
	 *
	 * @param x_source of the event
     * @param amodel_view AssetModel the client is viewing at the time of the request - may be null
	 */
	public CreateRequestEvent ( Object x_source, AssetModel amodel_view ) {
		super ( x_source, OS_OPERATION );
        oamodel_view = amodel_view;
	}
	
    /**
     * Get the AssetModel that the client was looking at when
     * the CREATE request was triggered - may be null
     */
	public AssetModel getAssetModel () { return oamodel_view; }
}



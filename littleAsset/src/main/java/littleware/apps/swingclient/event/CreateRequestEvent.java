/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient.event;

import littleware.asset.client.AssetRef;
import littleware.apps.client.*;
import littleware.base.event.LittleEvent;

/**
 * Event triggered to indicate a user request to create an asset
 */
public class CreateRequestEvent extends LittleEvent {

    private static final String OS_OPERATION = "CreateRequestEvent";
    private static final long serialVersionUID = -5621375774508626054L;
    private final AssetRef viewModel;

    /**
     * Setup the CreateRequestEvent
     *
     * @param source of the event
     * @param viewModel AssetRef the client is viewing at the time of the request - may be null
     */
    public CreateRequestEvent(Object source, AssetRef viewModel) {
        super(source);
        this.viewModel = viewModel;
    }

    /**
     * Get the AssetRef that the client was looking at when
     * the CREATE request was triggered - may be null
     */
    public AssetRef getAssetModel() {
        return viewModel;
    }
}

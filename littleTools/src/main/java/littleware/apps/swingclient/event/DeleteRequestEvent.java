/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
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
 * Event triggered to indicate a user request to delete an asset
 */
public class DeleteRequestEvent extends LittleEvent {

    private static final String OS_OPERATION = "DeleteRequestEvent";
    private final AssetRef oamodel_delete;

    /**
     * Setup the DeleteRequestEvent
     *
     * @param source of the event
     * @param amodel_delete AssetRef the client wants to delete
     */
    public DeleteRequestEvent(Object source, AssetRef amodel_delete) {
        super(source);
        oamodel_delete = amodel_delete;
    }

    /**
     * Get the AssetRef that the client wants to delete
     */
    public AssetRef getAssetModel() {
        return oamodel_delete;
    }
}

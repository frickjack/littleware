/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient.event;

import littleware.apps.client.*;
import littleware.base.event.LittleEvent;

/**
 * Event triggered to indicate a user request to delete an asset
 */
public class DeleteRequestEvent extends LittleEvent {

    private static final String OS_OPERATION = "DeleteRequestEvent";
    private final AssetModel oamodel_delete;

    /**
     * Setup the DeleteRequestEvent
     *
     * @param source of the event
     * @param amodel_delete AssetModel the client wants to delete
     */
    public DeleteRequestEvent(Object source, AssetModel amodel_delete) {
        super(source);
        oamodel_delete = amodel_delete;
    }

    /**
     * Get the AssetModel that the client wants to delete
     */
    public AssetModel getAssetModel() {
        return oamodel_delete;
    }
}

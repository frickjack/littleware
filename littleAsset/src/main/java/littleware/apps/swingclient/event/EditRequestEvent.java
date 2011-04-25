/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient.event;

import littleware.apps.client.*;
import littleware.base.event.LittleEvent;

/**
 * Event triggered to indicate a user request to edit an asset
 */
public class EditRequestEvent extends LittleEvent {

    private static final String OS_OPERATION = "EditRequestEvent";
    private final AssetModel oamodel_edit;

    /**
     * Setup the EditRequestEvent
     *
     * @param x_source of the event
     * @param amodel_edit that the client wants to edit
     */
    public EditRequestEvent(Object x_source, AssetModel amodel_edit) {
        super(x_source);
        oamodel_edit = amodel_edit;
    }

    /**
     * Get the AssetModel that the client wants to edit
     */
    public AssetModel getAssetModel() {
        return oamodel_edit;
    }
}

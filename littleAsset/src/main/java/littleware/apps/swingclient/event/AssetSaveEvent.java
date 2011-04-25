/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingclient.event;

import littleware.apps.client.*;
import littleware.base.event.LittleEvent;

/**
 * Event lets listeners on some object (probably an AssetModelLibrary)
 * know that that the Asset associated with a given AssetModel
 * has been saved to the Asset repository.
 */
public class AssetSaveEvent extends LittleEvent {
    private final AssetModel savedModel;

    /**
     * Setup the AssetSaveEvent
     *
     * @param source of the event - the editor or whatever
     * @param savedModel  AssetModel whose Asset has been saved
     */
    public AssetSaveEvent(Object source, AssetModel savedModel ) {
        super(source);
        this.savedModel = savedModel ;
    }

    public AssetModel getSavedModel() { return savedModel; }
}

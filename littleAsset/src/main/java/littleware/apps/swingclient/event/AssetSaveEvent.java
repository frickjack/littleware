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
 * Event lets listeners on some object (probably an AssetModelLibrary)
 * know that that the Asset associated with a given AssetRef
 * has been saved to the Asset repository.
 */
public class AssetSaveEvent extends LittleEvent {
    private final AssetRef savedModel;

    /**
     * Setup the AssetSaveEvent
     *
     * @param source of the event - the editor or whatever
     * @param savedModel  AssetRef whose Asset has been saved
     */
    public AssetSaveEvent(Object source, AssetRef savedModel ) {
        super(source);
        this.savedModel = savedModel ;
    }

    public AssetRef getSavedModel() { return savedModel; }
}

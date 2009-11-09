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
 * Event lets listeners on some object (probably an AssetModelLibrary)
 * know that that the Asset associated with a given AssetModel
 * has been saved to the Asset repository.
 */
public class AssetSaveEvent extends LittleEvent {
    
	/**
     * Setup the AssetSaveEvent
	 *
	 * @param x_source of the event - the editor or whatever
     * @param model_saved AssetModel whose Asset has been saved
	 */
	public AssetSaveEvent ( Object x_source, AssetModel model_saved ) {
		super ( x_source, "AssetSaveEvent", model_saved );
	}
}
    

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
    
    
// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com
    
    
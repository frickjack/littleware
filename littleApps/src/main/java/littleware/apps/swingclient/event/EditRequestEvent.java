package littleware.apps.swingclient.event;

import littleware.asset.Asset;
import littleware.apps.client.*;
/**
 * Event triggered to indicate a user request to edit an asset
 */
public class EditRequestEvent extends LittleEvent {
    private static final String   OS_OPERATION = "EditRequestEvent";
    
    private final AssetModel  oamodel_edit;
	
	/**
     * Setup the EditRequestEvent
	 *
	 * @param x_source of the event
     * @param amodel_edit that the client wants to edit
	 */
	public EditRequestEvent ( Object x_source, AssetModel amodel_edit ) {
		super ( x_source, OS_OPERATION );
        oamodel_edit = amodel_edit;
	}
	
    /**
     * Get the AssetModel that the client wants to edit
     */
	public AssetModel getAssetModel () { return oamodel_edit; }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


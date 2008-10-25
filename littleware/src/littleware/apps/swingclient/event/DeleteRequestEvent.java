package littleware.apps.swingclient.event;

import littleware.asset.Asset;
import littleware.apps.client.*;

/**
 * Event triggered to indicate a user request to delete an asset
 */
public class DeleteRequestEvent extends LittleEvent {
    private static final String   OS_OPERATION = "DeleteRequestEvent";
    
    private final AssetModel  oamodel_delete;
	
	/**
     * Setup the DeleteRequestEvent
	 *
	 * @param x_source of the event
     * @param amodel_delete AssetModel the client wants to delete
	 */
	public DeleteRequestEvent ( Object x_source, AssetModel amodel_delete ) {
		super ( x_source, OS_OPERATION );
        oamodel_delete = amodel_delete;
	}
	
    /**
     * Get the AssetModel that the client wants to delete
     */
	public AssetModel getAssetModel () { return oamodel_delete; }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.apps.swingclient.event;

import littleware.asset.Asset;
import littleware.apps.swingclient.LittleEvent;
import littleware.apps.swingclient.AssetModel;
import littleware.apps.swingclient.AssetModelLibrary;

/**
 * Event triggered to indicate a user request to create an asset
 */
public class CreateRequestEvent extends LittleEvent {
    private static final String   OS_OPERATION = "CreateRequestEvent";
    
    private final AssetModel  oamodel_view;
	
	/**
     * Setup the CreateRequestEvent
	 *
	 * @param x_source of the event
     * @param amodel_view AssetModel the client is viewing at the time of the request - may be null
	 */
	public CreateRequestEvent ( Object x_source, AssetModel amodel_view ) {
		super ( x_source, OS_OPERATION );
        oamodel_view = amodel_view;
	}
	
    /**
     * Get the AssetModel that the client was looking at when
     * the CREATE request was triggered - may be null
     */
	public AssetModel getAssetModel () { return oamodel_view; }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


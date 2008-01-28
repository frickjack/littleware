package littleware.apps.swingclient.event;

import littleware.asset.Asset;
import littleware.apps.swingclient.LittleEvent;
import littleware.apps.swingclient.AssetModelLibrary;

/**
 * Event triggered to indicate the user has selected some
 * single asset via some UI control.  Intended that
 * the purpose of the selection is implicit in whatever
 * control is the source.
 */
public class SelectAssetEvent extends LittleEvent {
    public static final String   OS_OPERATION = "SelectAssetEvent";
    
	/**
     * Setup the SelectAssetEvent
	 *
	 * @param x_source of the event
     * @param a_selected Asset selected in UI
	 */
	public SelectAssetEvent ( Object x_source, Asset a_selected
                              ) {
		super ( x_source, OS_OPERATION, a_selected );
	}
	
    /**
     * Get the Asset that has been selected
     */
	public Asset getSelectedAsset () { return (Asset) getResult (); }
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


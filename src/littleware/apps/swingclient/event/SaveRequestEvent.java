package littleware.apps.swingclient.event;

import littleware.asset.Asset;
import littleware.apps.swingclient.LittleEvent;
import littleware.apps.swingclient.AssetModelLibrary;

/**
 * Event triggered to indicate a user request to save an edited
 * version of an Asset to the repository.
 */
public class SaveRequestEvent extends LittleEvent {
    private static final String   OS_OPERATION = "SaveRequestEvent";

    private final AssetModelLibrary  olib_asset;
    private final String             os_update;
	
	/**
     * Setup the SaveRequestEvent
	 *
	 * @param x_source of the event
     * @param a_changed Asset to change - available via getChangedAsset()
     * @param lib_asset model library to update if the controller saves a_changed
     * @param s_update_comment to apply to the save
	 */
	public SaveRequestEvent ( Object x_source, Asset a_changed, 
                              AssetModelLibrary lib_asset,
                              String s_update_comment
                              ) {
		super ( x_source, OS_OPERATION, a_changed );
        olib_asset = lib_asset;
        os_update = s_update_comment;
	}
	
    /**
     * Get the Asset that has been requested to be saved
     */
	public Asset getChangedAsset () { return (Asset) getResult (); }
    
    /**
     * Get the update-comment to apply with the save.
     */
    public String getUpdateComment () { return os_update; }
    
    /**
     * Get the AssetModelLibrary this app is working with
     */
    public AssetModelLibrary getModelLibrary () { return olib_asset; }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


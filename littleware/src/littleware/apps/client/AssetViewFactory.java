package littleware.apps.client;

import com.google.inject.ImplementedBy;

/**
 * Factory generates a view for a given AssetModel
 * based on the Asset-type and the factory type.
 */
public interface AssetViewFactory {
    

    /**
     * Create a view appropriate for the 
     * given AssetModel&apos;s asset&apos;s type
     * 
     * @param model_asset to view
     * @return widget viewing the given model - subtype of JComponent
     *              for SWING based apps
     */
    public AssetView createView ( AssetModel model_asset );
    
    /**
     * Return true if the given view would be returned by this factory
     * to view the given model.
     * Allows a client to reuse an already created view
     * when navigating to a new AssetModel.
     *
     * @param view_check that the caller already has created from this factory
     *            via a previous call to createView
     * @param model_asset that the caller wants to view
     * @return true if view_check is the preferred viewer for model_asset, false otherwise
     */
    public boolean checkView ( AssetView view_check, AssetModel model_asset );
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


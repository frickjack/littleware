package littleware.apps.client;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeEvent;


/**
 * An AssetView underlies a UI component viewing 
 * data associated with a single main data asset.
 * An AssetView implementation listens for change-events on
 * the AssetModel it is observing, and fires its own
 * LittleEvents (especially NavRequestEvents) based 
 * on user interactions with the view.
 * A View listener is generally a Controller managing
 * some portion of an application UI.
 * It is hoped that adhering to an interface and
 * event-passing design allows an application to change
 * between AssetView implementations when new/better
 * views become available.
 */
public interface AssetView extends LittleTool {
    public enum Property {
        assetModel 
    };
    
    
    /**
     * Get the model this view is observing
     */
    public AssetModel getAssetModel ();
    
    /**
     * Set the model associated with this view.
     * This view registers as a listener of the new model,
     * and unregisters from the old model if any.
     *
     * @exception LibraryMismatchException if model_asset does not
     *             belong to the same AssetModelLibrary as the
     *             current AssetModel assigned to this view.
     */
    public void setAssetModel ( AssetModel model_asset );

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


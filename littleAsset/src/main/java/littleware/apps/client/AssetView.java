/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.client;

import littleware.base.event.LittleTool;
import littleware.base.feedback.Feedback;


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
     * @throws LibraryMismatchException if model_asset does not
     *             belong to the same AssetModelLibrary as the
     *             current AssetModel assigned to this view.
     */
    public void setAssetModel ( AssetModel model_asset );

    /**
     * Property associates a feedback mechanism with a view so that
     * the view may communicate information back to the user
     * when carrying out long running or whatever operations.
     */
    public Feedback getFeedback();
    public void  setFeedback( Feedback feedback );
}


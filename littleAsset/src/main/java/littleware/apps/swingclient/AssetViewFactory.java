/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient;

import littleware.asset.client.AssetRef;
import com.google.inject.Provider;
import littleware.asset.AssetType;

/**
 * Factory generates a view for a given AssetRef
 * based on the Asset-type and the factory type.
 */
public interface AssetViewFactory {

    /**
     * Register a new provider - usually at OSGi startup time
     *
     * @param atype to register
     * @param provider editor factory
     * @param classOfView that a view for atype must be an instance of
     *                     to pass the checkView() test
     */
    public void registerProvider( AssetType atype,
            Provider<? extends AssetView> provider,
            Class<? extends AssetView> classOfView
            );

    /**
     * Create a view appropriate for the 
     * given AssetRef&apos;s asset&apos;s type
     * 
     * @param model_asset to view
     * @return widget viewing the given model - subtype of JComponent
     *              for SWING based apps
     */
    public AssetView createView ( AssetRef model_asset );
    
    /**
     * Return true if the given view would be returned by this factory
     * to view the given model.
     * Allows a client to reuse an already created view
     * when navigating to a new AssetRef.
     *
     * @param view_check that the caller already has created from this factory
     *            via a previous call to createView
     * @param model_asset that the caller wants to view
     * @return true if view_check is the preferred viewer for model_asset, false otherwise
     */
    public boolean checkView ( AssetView view_check, AssetRef model_asset );
    
}


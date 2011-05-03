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

/**
 * Specialization of AssetViewFactory for editors.
 */
public interface AssetEditorFactory extends AssetViewFactory {
    
    
    /**
     * Narrow the createView return-type.
     * 
     * @param model_asset to view
     * @return widget viewing the given model - subtype of JComponent
     *              for SWING based apps
     */
    @Override
    public AssetEditor createView ( AssetRef model_asset );
        
}


package littleware.apps.swingclient;

import java.util.UUID;

import littleware.base.Cache;

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
    public AssetEditor createView ( AssetModel model_asset );
        
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


package littleware.apps.swingclient;

import com.google.inject.Inject;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.security.SecurityAssetType;

/**
 * AssetViewFactory supplies custom editors for different types of asset.
 * The editors allow the user to modify an in-memory copy of an asset,
 * that only gets comitted back to the AssetModelLibrary and littleware repository
 * when the user clicks a SAVE button - which triggers a LittleSaveEvent to
 * the LittleListeners registered with the view.
 */
public class EditorAssetViewFactory implements AssetEditorFactory {
    private final IconLibrary         olib_icon;
    private final AssetSearchManager  om_search;
    private final AssetManager        om_asset;
    private final AssetViewFactory    ofactory_view;
    
    /**
     * Setup the factory with an AssetManager, AssetSearchManager, and IconLibrary 
     * required to construct different asset-editor views.
     *
     * @param factory_view source of read-only views for browsers launched by different editors
     */
    @Inject
    public EditorAssetViewFactory ( AssetManager m_asset, AssetSearchManager m_search, 
                                    IconLibrary lib_icon,
                                    AssetViewFactory factory_view
                                    ) {
        olib_icon = lib_icon;
        om_asset = m_asset;
        om_search = m_search;
        ofactory_view = factory_view;
    }
    
    public AssetEditor createView ( AssetModel model_asset ) {
        AssetType n_asset = model_asset.getAsset ().getAssetType ();
        
        if ( n_asset.equals ( SecurityAssetType.GROUP ) ) {
            return new JGroupEditor ( model_asset, om_asset, om_search, olib_icon, ofactory_view );
        } else if ( n_asset.equals ( SecurityAssetType.ACL ) ) {
            return new JAclEditor ( model_asset, om_asset, om_search, olib_icon, ofactory_view );
        } else {
            //throw new UnsupportedOperationException ( "Not yet implemented for asset type: " + n_asset );
            return new JGenericAssetEditor ( model_asset, om_asset, om_search, olib_icon, ofactory_view ) {
                @Override
                public void eventFromModel ( LittleEvent evt_ignore ) {}
            };
        }
    }
    
    public boolean checkView ( AssetView view_check, AssetModel model_asset ) {
        AssetType n_asset = model_asset.getAsset ().getAssetType ();
        
        if ( n_asset.equals ( SecurityAssetType.GROUP ) ) {
            return (view_check instanceof JGroupEditor);
        }
        return (view_check instanceof JGenericAssetEditor);
    }
    
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


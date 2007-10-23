package littleware.apps.swingclient;

import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.security.SecurityAssetType;
import littleware.apps.tracker.TrackerAssetType;
import littleware.apps.tracker.swing.*;

/**
 * Simple AssetViewFactory implementation that just
 * hard-codes the view-type to return for a given asset-type.
 */
public class SimpleAssetViewFactory implements AssetViewFactory {
    private static final Logger       olog_generic = Logger.getLogger ( "littleware.apps.swingclient.SimpleAssetViewFactory" );
    private final IconLibrary         olib_icon;
    private final AssetSearchManager  om_search;
    
    /**
     * Setup the factory with an icon library and an AssetSearchManager
     */
    public SimpleAssetViewFactory ( AssetSearchManager m_search, IconLibrary lib_icon ) {
        olib_icon = lib_icon;
        om_search = m_search;
    }
    
    public AssetView createView ( AssetModel model_asset ) {
        AssetType n_asset = model_asset.getAsset ().getAssetType ();
        
        if ( n_asset.equals ( SecurityAssetType.GROUP ) ) {
            return new JGroupView ( model_asset, om_search, olib_icon );
        } else if ( n_asset.equals ( SecurityAssetType.ACL ) ) {
            return new JAclView ( model_asset, om_search, olib_icon );
        } else if ( n_asset.equals ( TrackerAssetType.QUEUE ) ) {
            try {
                return new JQView ( model_asset, om_search, olib_icon );
            } catch ( Exception e ) {
                olog_generic.log ( Level.WARNING, "Failure to allocate JQView, caught: " + e );
            }
        } 
        return new JGenericAssetView ( model_asset, om_search, olib_icon );
    }
    
    public boolean checkView ( AssetView view_check, AssetModel model_asset ) {
        AssetType n_asset = model_asset.getAsset ().getAssetType ();
        
        if ( n_asset.equals ( SecurityAssetType.GROUP ) ) {
            return (view_check instanceof JGroupView);
        }
        if ( n_asset.equals ( SecurityAssetType.ACL ) ) {
            return (view_check instanceof JAclView);
        }
        return (view_check instanceof JGenericAssetView);
    }

}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


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

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.security.SecurityAssetType;
import littleware.apps.tracker.TrackerAssetType;
import littleware.apps.tracker.swing.JQView;

/**
 * Simple AssetViewFactory implementation that just
 * hard-codes the view-type to return for a given asset-type.
 */
public class SimpleAssetViewFactory implements AssetViewFactory {
    private static final Logger       olog_generic = Logger.getLogger ( SimpleAssetViewFactory.class.getName() );

    private final Provider<JGenericAssetView> oprovide_generic;
    private final Provider<JGroupView> oprovide_group;
    private final Provider<JAclView> oprovide_acl;
    private final Provider<JQView> oprovide_queue;
    
    /**
     * Setup the factory with an icon library and an AssetSearchManager
     */
    @Inject
    public SimpleAssetViewFactory ( Provider<JGenericAssetView> provide_generic,
            Provider<JGroupView> provide_group,
            Provider<JAclView> provide_acl,
            Provider<JQView>   provide_queue
            ) {
        oprovide_generic = provide_generic;
        oprovide_group = provide_group;
        oprovide_acl = provide_acl;
        oprovide_queue = provide_queue;
    }
    
    public AssetView createView ( AssetModel model_asset ) {
        AssetType n_asset = model_asset.getAsset ().getAssetType ();
        AssetView view_result = null;
        
        if ( n_asset.equals ( SecurityAssetType.GROUP ) ) {
            view_result = oprovide_group.get();
        } else if ( n_asset.equals ( SecurityAssetType.ACL ) ) {
            view_result = oprovide_acl.get();
        } else if ( n_asset.equals ( TrackerAssetType.QUEUE ) ) {
            view_result = oprovide_queue.get();
        } else {
            view_result = oprovide_generic.get ();
        }
        view_result.setAssetModel( model_asset );
        return view_result;
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


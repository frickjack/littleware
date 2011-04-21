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
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;
//import littleware.apps.tracker.TrackerAssetType;
//import littleware.apps.tracker.swing.JQView;

/**
 * Simple AssetViewFactory implementation that just
 * hard-codes the view-type to return for a given asset-type.
 */
public class SimpleAssetViewFactory implements AssetViewFactory {
    private static final Logger       olog_generic = Logger.getLogger ( SimpleAssetViewFactory.class.getName() );

    private final Map<AssetType,Provider<? extends AssetView>> omapRegistry =
            new HashMap<AssetType,Provider<? extends AssetView>>();
    private final Map<AssetType,Class<? extends AssetView>> omapClassCheck =
            new HashMap<AssetType,Class<? extends AssetView>>();

    @Override
    public final void registerProvider( AssetType atype,
            Provider<? extends AssetView> provider,
            Class<? extends AssetView> classOfEditor
            ) {
        omapRegistry.put(atype, provider);
        omapClassCheck.put( atype, classOfEditor);
    }

    /**
     * Construct an empty factory
     */
    protected SimpleAssetViewFactory () {}

    /**
     * Setup the factory with an icon library and an AssetSearchManager
     */
    @Inject
    public SimpleAssetViewFactory ( Provider<JGenericAssetView> provide_generic,
            Provider<JGroupView> provide_group,
            Provider<JAclView> provide_acl
            //,Provider<JQView>   provide_queue
            ) {
        registerProvider( GenericAsset.GENERIC, provide_generic, JGenericAssetView.class );
        registerProvider( LittleGroup.GROUP_TYPE, provide_group, JGroupView.class );
        registerProvider( LittleAcl.ACL_TYPE, provide_acl, JAclView.class );
        //registerProvider( Queue.QUEUE_TYPE, provide_queue, JQView.class );
    }
    
    @Override
    public AssetView createView ( AssetModel model_asset ) {
        AssetType n_asset = model_asset.getAsset ().getAssetType ();
        final AssetView view;
        if ( omapRegistry.containsKey(n_asset ) ) {
            view = omapRegistry.get( n_asset ).get();
        } else {
            view = omapRegistry.get( GenericAsset.GENERIC ).get();
        }
        view.setAssetModel(model_asset);
        return view;
    }

    @Override
    public boolean checkView ( AssetView view_check, AssetModel model_asset ) {
        AssetType n_asset = model_asset.getAsset ().getAssetType ();

        if ( omapClassCheck.containsKey( n_asset ) ) {
            return omapClassCheck.get( n_asset ).isInstance( view_check );
        } else {
            return omapClassCheck.get( GenericAsset.GENERIC ).isInstance( view_check );
        }
    }

}


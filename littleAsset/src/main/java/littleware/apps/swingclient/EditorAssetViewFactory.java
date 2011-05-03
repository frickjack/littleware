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
import com.google.inject.Inject;

import com.google.inject.Provider;
import com.google.inject.Singleton;
import littleware.apps.client.*;
import littleware.asset.*;
import littleware.security.LittleAcl;
import littleware.security.LittleGroup;

/**
 * AssetViewFactory supplies custom editors for different types of asset.
 * The editors allow the user to modify an in-memory copy of an asset,
 * that only gets comitted back to the AssetModelLibrary and littleware repository
 * when the user clicks a SAVE button - which triggers a LittleSaveEvent to
 * the LittleListeners registered with the view.
 */
@Singleton
public class EditorAssetViewFactory extends SimpleAssetViewFactory implements AssetEditorFactory {

    /**
     * Setup the factory with an AssetManager, AssetSearchManager, and IconLibrary 
     * required to construct different asset-editor views.
     */
    @Inject
    public EditorAssetViewFactory ( Provider<JGroupEditor> provideGroupEditor,
            Provider<JAclEditor> provideAclEditor,
            Provider<JGenericAssetEditor> provideGenericEditor
                                    ) {
        registerProvider( LittleGroup.GROUP_TYPE, provideGroupEditor, JGroupEditor.class );

        registerProvider( LittleAcl.ACL_TYPE, provideAclEditor
                                    , JAclEditor.class );
        registerProvider( GenericAsset.GENERIC, provideGenericEditor,
                            JGenericAssetEditor.class );
    }
    
    @Override
    public AssetEditor createView ( AssetRef model_asset ) {
        return (AssetEditor) super.createView( model_asset );
    }
    
}

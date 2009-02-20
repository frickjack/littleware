/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.wizard;

import javax.swing.*;
import java.util.List;

import com.nexes.wizard.*;

import littleware.apps.client.*;
import littleware.apps.swingclient.*;
import littleware.asset.AssetType;

/**
 * WizardController plugin for the AssetTypeSelector control
 */
public class AssetTypePanelDescriptor extends WizardPanelDescriptor {
    private final AssetTypeSelector  oselector_bean;
    private final AssetEditor        oeditor_parent;

    /**
     * Constructor sets up the state needed by the wizard
     *
     * @param x_id for th wizard to identify this panel by
     * @param wcomp_display for the wizard to display when this panel is active -
     *            should include a view of the selector_bean
     * @param selector_bean for this controller to access selected-type data through
     * @param editor_parent to getLocalAsset on - updates localAsset AssetType in aboutToHidePanel()
     */
    public AssetTypePanelDescriptor ( Object x_id, 
                                      JComponent wcomp_display,
                                      AssetTypeSelector selector_bean,
                                      AssetEditor editor_parent
                                      ) {
        super ( x_id, wcomp_display );
        oselector_bean = selector_bean;
        oeditor_parent = editor_parent;
    }
    
    /**
     * Handle to get at the selector-bean so clients can set the
     * available asset types and the currently selected asset type.
     */
    public AssetTypeSelector getSelector () {
        return oselector_bean;
    }
    
    /**
     * Sync the display with the underlying AssetEditor localAsset
     */
    @Override
    public void aboutToDisplayPanel () {
        AssetType       n_active = oeditor_parent.getLocalAsset ().getAssetType ();
        int             i_index = getSelector ().getIndexOf ( n_active );
        
        if ( i_index < 0 ) {
            List<AssetType> v_options = getSelector ().getAssetTypeOptions ();
            if ( v_options.isEmpty () ) {
                v_options.add ( n_active );
                getSelector ().setAssetTypeOptions ( v_options );
            } else {
                oeditor_parent.changeLocalAsset ().setAssetType ( v_options.get( 0 ) );
            }
            i_index = 0;
        }
        getSelector ().setSelectedAssetTypeIndex ( i_index );
    }
    
    /** Set the focus */
    @Override
    public void displayingPanel () {
        getPanelComponent ().requestFocus ();
    }
    
    /**
     * Sync the editor local asset with the panel selected assetType.
     */
    @Override
    public void aboutToHidePanel () {
        if ( ! oeditor_parent.getLocalAsset ().getAssetType ().equals ( getSelector ().getSelectedAssetType () ) ) {
            oeditor_parent.changeLocalAsset ().setAssetType ( getSelector ().getSelectedAssetType () );
        }
    }
    
}


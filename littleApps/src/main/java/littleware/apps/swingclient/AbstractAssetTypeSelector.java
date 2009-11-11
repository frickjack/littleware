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

import java.util.List;
import java.util.ArrayList;

import littleware.apps.client.*;
import littleware.asset.AssetType;


/**
 * Base implementation of AssetTypeSelector interface.
 */
public class AbstractAssetTypeSelector extends SimpleLittleTool implements AssetTypeSelector {
    private List<AssetType>  ov_options = new ArrayList<AssetType> ();
    private int                    oi_selected = 0;
    
    
    
    /**
     * Constructor takes the bean to use as the source of any thrown events
     */
    public AbstractAssetTypeSelector ( Object x_sourcebean ) {
        super ( x_sourcebean );
    }

    
    @Override
    public List<AssetType>  getAssetTypeOptions () {
        return ov_options;
    }
    
    @Override
    public void setAssetTypeOptions ( List<AssetType> v_options ) {
        List<AssetType>  v_old = ov_options;
        ov_options = v_options;
        firePropertyChange( 
                               AssetTypeSelector.Property.assetTypeOptions.toString (), 
                               v_old, 
                               ov_options 
                                 );
        setSelectedAssetTypeIndex ( 0 );
    }
    

    @Override
    public int getSelectedAssetTypeIndex () {
        return oi_selected;
    }
    
    @Override
    public void setSelectedAssetTypeIndex ( int i_selected ) {
        int i_old = oi_selected;
        if ( (i_selected < 0) || ov_options.isEmpty () ) {
            oi_selected = 0;
        } else if ( i_selected >= ov_options.size () ) {
            oi_selected = ov_options.size () - 1;
        } else {
            oi_selected = i_selected;
        }
        firePropertyChange( 
                              AssetTypeSelector.Property.selectedAssetType.toString (), 
                              i_old, oi_selected 
                            );
    }
    
    @Override
    public int getIndexOf ( AssetType n_look4 ) {
        int i_index = 0;
        
        for ( AssetType n_type : ov_options ) {
            if ( n_look4.equals ( n_type ) ) {
                return i_index;
            }
            ++i_index;
        }
        
        return -1;
    }

    @Override
    public AssetType getSelectedAssetType () {
        return ov_options.get ( getSelectedAssetTypeIndex () );
    }

 }


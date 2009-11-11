package littleware.apps.swingclient;

import java.util.List;
import java.beans.PropertyChangeListener;

import littleware.apps.client.LittleTool;
import littleware.asset.AssetType;


/**
 * Interface for data model underlying a UI component
 * for selecting the AssetType to assign to some Asset.
 */
public interface AssetTypeSelector extends LittleTool {
    /**
     * Property enumeration convenience for PropertyChangeListeners.
     */
    public enum Property {
        assetTypeOptions,
        selectedAssetType;
    }
    
    
    /**
     * Get the ordered list of asset types available for selection
     */
    public List<AssetType>  getAssetTypeOptions ();
    
    /**
     * Set the ordered list of asset types, and setSelectedAssetType( 0 ).
     * Fire a propertyChangeEvent with Property.AssetTypeOptions property.
     */
    public void setAssetTypeOptions ( List<AssetType> v_options );
    
    /**
     * Get the currently selected option in the UI
     */
    public int getSelectedAssetTypeIndex ();
    
    /**
     * Convenience method 
     */
    public AssetType getSelectedAssetType ();
    
    /**
     * Set the selected asset type - fire PropertyChangeEvent
     * with Property.SelectedAssetType.
     *
     * @param i_selected index into option list - autoclamped to valid range
     */
    public void setSelectedAssetTypeIndex ( int i_selected );
    
    /**
     * Get the index of the given AssetType in this selector&apos;s option list,
     * or return -1 if not in the list
     *
     * @param n_look4 type to search for in the option list
     * @return index of n_look4 in the option list, or -1 if not in the list
     */
    public int getIndexOf ( AssetType n_look4 );
        
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


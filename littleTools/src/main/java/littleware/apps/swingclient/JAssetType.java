package littleware.apps.swingclient;

import java.beans.PropertyChangeSupport;
import javax.swing.*;

import littleware.asset.AssetType;
import littleware.asset.GenericAsset;

/**
 * JLabel specialization for displaying asset-type name.
 */
public class JAssetType extends JLabel implements ListCellRenderer {
    private final   PropertyChangeSupport  oprop_support = new PropertyChangeSupport ( this );
    private final   IconLibrary            olib_icon;
    private AssetType                      on_type = GenericAsset.GENERIC;
    
    /**
     * Constructor stashes away icon library
     */
    public JAssetType ( IconLibrary lib_icon ) {
        olib_icon = lib_icon;
    }
    
    
    /**
     * This is the only method defined by ListCellRenderer.
     * We just reconfigure the JLabel each time we're called. 
     *
     * @param x_value should be an AssetType
     */
    public JComponent getListCellRendererComponent(
                                                  JList   wlist_assets,
                                                  Object  x_value,            
                                                  int     i_index,            
                                                  boolean b_selected,      
                                                  boolean b_hasfocus)    
    {
        setAssetType( (AssetType) x_value );
        if (b_selected) {
            setBackground( wlist_assets.getSelectionBackground() );
            setForeground( wlist_assets.getSelectionForeground() );
        }
        else {
            setBackground( wlist_assets.getBackground() );
            setForeground( wlist_assets.getForeground() );
        }
        setEnabled(wlist_assets.isEnabled());
        setFont(wlist_assets.getFont());
        setOpaque(true);
        return this;
    }

    /**
     * Get the active asset type
     */
    public AssetType getAssetType () {
        return on_type;
    }
    
    /**
     * Set the active asset type, and fire PropertyChangeEvent to listenre
     */
    public void setAssetType ( AssetType n_type ) {
        on_type = n_type;
        this.setText ( n_type.toString () );
        this.setIcon ( olib_icon.lookupIcon ( n_type ) );
    }
        
}

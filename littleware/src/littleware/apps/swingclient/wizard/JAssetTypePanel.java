package littleware.apps.swingclient.wizard;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.*;

import littleware.apps.swingclient.*;


/** 
 * Simple wizard panel for selecting an asset type
 */
public class JAssetTypePanel extends JPanel {
    private static final long serialVersionUID = -7891643355703793674L;

    private final JAssetTypeSelector  owselector_atype;
    
    private boolean                   ob_gui_built = false;
    
    /**
     * Internal util populate this panel with its components
     */
    private void buildUI () {
        if ( ob_gui_built ) {
            return;
        }
        ob_gui_built = true;
        
        final JLabel             wlabel_instruct = new JLabel ( "<html><p>Pick a type for your asset.</p></html>" );
        final GridBagConstraints grid_control = new GridBagConstraints ();
        
        grid_control.gridx = 0;
        grid_control.gridy = 0;
        grid_control.gridheight = 1;
        grid_control.gridwidth = 1;
        //grid_control.fill = GridBagConstraints.HORIZONTAL;
        
        this.add ( wlabel_instruct, grid_control );
        grid_control.gridy += 1;
        this.add ( owselector_atype, grid_control );
    }
    
    
    /**
     * Construct the panel - give it the IconLibrary in use
     */
    public JAssetTypePanel ( IconLibrary lib_icon ) {
        super( new GridBagLayout () );
        owselector_atype = new JAssetTypeSelector ( lib_icon );
        buildUI ();
    }
    
    
    /**
     * Get the selector this wizard panel presents
     */
    public AssetTypeSelector getAssetTypeSelector () {
        return owselector_atype;
    }
}

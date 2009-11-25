package littleware.apps.swingclient.wizard;

import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import javax.swing.*;
import javax.swing.text.JTextComponent;

import littleware.apps.swingclient.*;


/** 
 * Simple wizard panel for specifying some text.
 * Change this to be a subtype of JLabelAndThingPanel later.
 */
public class JTextFieldPanel extends JPanel {
    private final String           os_label;
    private final JTextComponent   owtext_info;
    
    private boolean                   ob_gui_built = false;
    
    /**
     * Internal util populate this panel with its components
     */
    private void buildUI () {
        if ( ob_gui_built ) {
            return;
        }
        ob_gui_built = true;
        
        final JLabel             wlabel_instruct = new JLabel ( os_label );
        final GridBagConstraints grid_control = new GridBagConstraints ();
        
        grid_control.gridx = 0;
        grid_control.gridy = 0;
        grid_control.gridheight = 1;
        grid_control.gridwidth = 1;
        //grid_control.fill = GridBagConstraints.HORIZONTAL;
        
        this.add ( wlabel_instruct,
                   grid_control
                   );
        grid_control.gridy += 1;
        this.add ( owtext_info, grid_control );
    }
    
    
    /**
     * Construct the panel 
     *
     * @param s_short_instruction short instruction to populate a JLabel with
     * @param i_rows in text area - use JTextField if i_rows <=1, otherwise JTextArea
     * @param i_columns in text area
     */
    public JTextFieldPanel ( String s_short_instruction, int i_rows, int i_columns ) {
        super( new GridBagLayout () );
        os_label = s_short_instruction;
        if ( i_rows <= 1 ) {
            owtext_info = new JTextField ( i_columns );
        } else {
            owtext_info = new JTextArea ( i_rows, i_columns );
        }
        buildUI ();
    }
    
    
    /**
     * Get the text-field contents
     */
    public String getText () {
        return owtext_info.getText ();
    }
    
    /** Set the text-field contents */
    public void setText ( String s_text ) {
        owtext_info.setText ( s_text );
    }
    
    /**
     * Forwards focus request to text-input field
     */
    public void requestFocus () {
        owtext_info.requestFocus ();
    }
    
}

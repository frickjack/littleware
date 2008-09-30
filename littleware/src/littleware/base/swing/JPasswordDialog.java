package littleware.base.swing;

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.*;


/**
 * Just a stupid little dialog for prompting a user
 * for username/password.
 */
public class JPasswordDialog extends JDialog {  
    private static final Logger  olog = Logger.getLogger ( "littleware.base.swing.JPasswordDialog" );

    private final JTextField ow_user = new JTextField( 20 );
    private final JTextField ow_password = new JPasswordField( 20 );
    
    /**
     * Inject initial name and password property values
     */
    public JPasswordDialog ( String s_name, String s_password )
    {
        getContentPane ().setLayout ( new BorderLayout () );
        
        JLabel      wlabel_instruct = new JLabel ( "<html><body><p>Authenticate</p></body></html>" );
        JPanel      wpanel_buttons = new JPanel ();
        JButton     wbutton_ok = new JButton ( "Ok" );
        JButton     wbutton_cancel = new JButton ( "Cancel" );
        
        wbutton_ok.addActionListener ( new ActionListener () {
            public void actionPerformed ( ActionEvent event_button ) {
                ob_ok = true;
                JPasswordDialog.this.dispose ();
            }
        }
                                           );
        wbutton_cancel.addActionListener ( new ActionListener () {
            public void actionPerformed ( ActionEvent event_button ) {
                JPasswordDialog.this.dispose ();
            }
        }
            );
        wpanel_buttons.add ( wbutton_ok );
        wpanel_buttons.add ( wbutton_cancel );

        this.addWindowListener ( new WindowAdapter () {
            @Override
            public void windowClosing ( WindowEvent evt_win ) {
                JPasswordDialog.this.dispose();
            }
        }
            );

        JPanel      wpanel_form = new JPanel ();
        wpanel_form.setLayout( new GridBagLayout() );
        GridBagConstraints gcontrol = new GridBagConstraints();
        gcontrol.insets = new Insets( 2,2,2,2 );
        gcontrol.gridx = 0; gcontrol.gridy = 0;
        gcontrol.gridwidth = 1; gcontrol.gridheight = 1;
        gcontrol.anchor = gcontrol.EAST;
        wpanel_form.add( new JLabel( "User:" ), gcontrol );
        gcontrol.anchor = gcontrol.WEST;
        gcontrol.gridx += 1;
        wpanel_form.add( ow_user, gcontrol );
        gcontrol.anchor = gcontrol.EAST;
        gcontrol.gridx = 0; gcontrol.gridy += 1;
        wpanel_form.add( new JLabel( "Password:" ), gcontrol );
        gcontrol.anchor = gcontrol.WEST;
        gcontrol.gridx += 1;
        wpanel_form.add( ow_password, gcontrol );
        
        getContentPane ().add ( wlabel_instruct, BorderLayout.NORTH );
        getContentPane ().add ( wpanel_form, BorderLayout.CENTER );
        getContentPane ().add ( wpanel_buttons, BorderLayout.SOUTH );
    }

    
    /**
     * userName property
     */
    public String getUserName() { return ow_user.getText (); }
    public void setUserName( String s_name ) { ow_user.setText( s_name ); }
    /**
     * Password property 
     */
    public String getPassword() { return ow_password.getText (); }
    public void setPassword( String s_password ) { ow_password.setText( s_password ); }
    
    private boolean ob_ok = false;
    
    /**
     * Pack the dialog, and pop it up, return the post-test
     * getLastResult() value.
     * If not run on the dispatch-thread, then 
     * blocks the calling thread to wait for the 
     * dialog to run on the dispatch thread.
     *
     * @return true if user click ok, false otherwise
     */
    public boolean showDialog() {
        if ( ! isVisible () ) {
            ob_ok = false;
            Runnable  run_dialog = new Runnable () {
                public void run () {
                    setModal(true);
                    pack();
                    setVisible( true );                
                }
            };
            
            if ( SwingUtilities.isEventDispatchThread () ) {
                run_dialog.run ();
            } else {        
                try {
                    SwingUtilities.invokeAndWait ( run_dialog );
                } catch ( RuntimeException e ) {
                    throw e;
                } catch ( Exception e ) {
                    throw new RuntimeException ( "Failed to build UI", e );
                }
            }
        }
        return ob_ok;
    }
    

}



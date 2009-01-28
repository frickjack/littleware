package littleware.base.swing;

import java.awt.*;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.concurrent.Callable;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.*;


/**
 * Just a stupid little dialog for prompting a user
 * for username/password.
 */
public class JPasswordDialog extends JDialog implements CallbackHandler {  
    private static final Logger  olog = Logger.getLogger ( "littleware.base.swing.JPasswordDialog" );

    private final JTextField ow_user = new JTextField( 20 );
    private final JTextField ow_password = new JPasswordField( 20 );
    private final JLabel     owlabel_instruct = new JLabel ( "<html><body><p>Authenticate</p></body></html>" );    
    /**
     * Inject initial name and password property values
     */
    public JPasswordDialog ( String s_name, String s_password )
    {
        getContentPane ().setLayout ( new BorderLayout () );
        ow_user.setText( s_name );
        ow_password.setText( s_password );
        
        JPanel      wpanel_buttons = new JPanel ();
        final JButton     wbutton_ok = new JButton ( "Ok" );
        final JButton     wbutton_cancel = new JButton ( "Cancel" );
        
        KeyListener klisten = new KeyListener() {
            public void keyTyped(KeyEvent e) {}
            public void keyPressed(KeyEvent e) {}
            public void keyReleased(KeyEvent ev) {
                //olog.log( Level.FINE, "Got key: " + ev.getKeyCode () );
                if ( (ev.getKeyCode() == 10) || (ev.getKeyCode() == 13) ) {
                    if ( ev.getSource() != wbutton_cancel ) {
                        ob_ok = true;
                    }
                    JPasswordDialog.this.dispose ();                    
                }
            }
        };
        
        ow_password.addKeyListener( klisten );
        wbutton_cancel.addKeyListener( klisten );
        wbutton_ok.addKeyListener ( klisten );
        wbutton_ok.addActionListener ( new ActionListener () {
            public void actionPerformed ( ActionEvent event_button ) {
                ob_ok = true;
                JPasswordDialog.this.dispose ();
            }

            public void keyTyped(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyPressed(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void keyReleased(KeyEvent e) {
                throw new UnsupportedOperationException("Not supported yet.");
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
        gcontrol.anchor = GridBagConstraints.EAST;
        wpanel_form.add( new JLabel( "User:" ), gcontrol );
        gcontrol.anchor = GridBagConstraints.WEST;
        gcontrol.gridx += 1;
        wpanel_form.add( ow_user, gcontrol );
        gcontrol.anchor = GridBagConstraints.EAST;
        gcontrol.gridx = 0; gcontrol.gridy += 1;
        wpanel_form.add( new JLabel( "Password:" ), gcontrol );
        gcontrol.anchor = GridBagConstraints.WEST;
        gcontrol.gridx += 1;
        wpanel_form.add( ow_password, gcontrol );
        
        getContentPane ().add ( owlabel_instruct, BorderLayout.NORTH );
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
    
    
    private Callable<Boolean>  ocall = null;
    
    /**
     * Little message attached to the dialog.
     * Can set error/whatever.  Maps to a JLabel,
     * so can use HTML.
     */
    public String getMessage () { return owlabel_instruct.getText(); }
    public void setMessage( String s_message ) { 
        owlabel_instruct.setText( s_message );
    }
    
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

    /**
     * Implement LoginModule CallbackHandler interface 
     * 
     * @param v_callbacks from a LoginModule or whatever to handle
     * @throws java.io.IOException if user cancels out of dialog
     * @throws javax.security.auth.callback.UnsupportedCallbackException for unexpected callback
     */
    public void handle(Callback[] v_callbacks) throws IOException, UnsupportedCallbackException {
       for ( Callback callback : v_callbacks ) {
           if ( callback instanceof NameCallback) {
               NameCallback ncb = (NameCallback) callback;
               if ( ncb.getDefaultName () != null ) {
                   setUserName( ncb.getDefaultName() );
                   break;
               }
           }
       }
       if ( showDialog () ) {
           for ( Callback callback : v_callbacks ) {
                if ( callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(getUserName());
                } else if ( callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword( getPassword().toCharArray() );
                } else if ( callback instanceof TextOutputCallback ) {
                    owlabel_instruct.setText( "<html>Authenticate: " + 
                            ((TextOutputCallback) callback).getMessage () +
                            "</html>"
                            );
                } else {
                    throw new UnsupportedCallbackException(callback, "Unsupported callback");
                }
            }
       } else {
           throw new IOException( "User canceled out" );
       }
    }    
    
    
}



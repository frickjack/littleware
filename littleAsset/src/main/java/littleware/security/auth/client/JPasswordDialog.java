/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.auth.client;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.util.logging.Logger;

import javax.security.auth.callback.Callback;
import javax.security.auth.callback.CallbackHandler;
import javax.security.auth.callback.NameCallback;
import javax.security.auth.callback.PasswordCallback;
import javax.security.auth.callback.TextOutputCallback;
import javax.security.auth.callback.UnsupportedCallbackException;
import javax.swing.*;
import littleware.base.swing.GridBagWrap;


/**
 * Just a stupid little dialog for prompting a user
 * for username/password.
 */
public class JPasswordDialog extends JDialog implements CallbackHandler {  
    private static final Logger  log = Logger.getLogger ( "littleware.base.swing.JPasswordDialog" );

    private final JTextField jtextUser = new JTextField( 20 );
    private final JTextField jtextPassword = new JPasswordField( 20 );
    private final JLabel     jlabelInstruct = new JLabel ( "<html><body><p>Authenticate</p></body></html>" );

    /**
     * Inject initial name and password property values
     */
    public JPasswordDialog ( String name, String password, String message )
    {
        getContentPane ().setLayout ( new BorderLayout () );
        setMessage( message );
        jtextUser.setText( name );
        jtextPassword.setText( password );
        
        final JPanel      jpanelButtons = new JPanel ();
        final JButton     jbuttonOk = new JButton ( "Ok" );
        final JButton     jbuttonCancel = new JButton ( "Cancel" );

        // Allow "return" key to trigger data-collection
        final KeyListener klisten = new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}
            @Override
            public void keyPressed(KeyEvent e) {}
            @Override
            public void keyReleased(KeyEvent ev) {
                //olog.log( Level.FINE, "Got key: " + ev.getKeyCode () );
                if ( (ev.getKeyCode() == 10) || (ev.getKeyCode() == 13) ) {
                    if ( ev.getSource() != jbuttonCancel ) {
                        showDialogResult = true;
                    }
                    JPasswordDialog.this.dispose ();                    
                }
            }
        };
        
        jtextPassword.addKeyListener( klisten );
        jbuttonCancel.addKeyListener( klisten );
        jbuttonOk.addKeyListener ( klisten );
        jbuttonOk.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed ( ActionEvent event_button ) {
                showDialogResult = true;
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
        jbuttonCancel.addActionListener ( new ActionListener () {
            @Override
            public void actionPerformed ( ActionEvent event_button ) {
                JPasswordDialog.this.dispose ();
            }
        }
            );
        jpanelButtons.add ( jbuttonOk );
        jpanelButtons.add ( jbuttonCancel );

        this.addWindowListener ( new WindowAdapter () {
            @Override
            public void windowClosing ( WindowEvent evt_win ) {
                JPasswordDialog.this.dispose();
            }
        }
            );

        final JPanel      jpanel = new JPanel ();
        final GridBagWrap gb = GridBagWrap.wrap( jpanel );
        gb.add( new JLabel( "User:" ) ).nextCol().add( jtextUser ).newRow();
        gb.add( new JLabel( "Password:" ) ).nextCol().add( jtextPassword ).newRow();
        
        getContentPane ().add ( jlabelInstruct, BorderLayout.NORTH );
        getContentPane ().add ( jpanel, BorderLayout.CENTER );
        getContentPane ().add ( jpanelButtons, BorderLayout.SOUTH );
    }

    
    /**
     * userName property
     */
    public String getUserName() { return jtextUser.getText (); }
    public void setUserName( String s_name ) { jtextUser.setText( s_name ); }
    /**
     * Password property 
     */
    public String getPassword() { return jtextPassword.getText (); }
    public void setPassword( String s_password ) { jtextPassword.setText( s_password ); }
    
    
    /**
     * Little message attached to the dialog.
     * Can set error/whatever.  Maps to a JLabel,
     * so can use HTML.
     */
    public String getMessage () { return jlabelInstruct.getText(); }
    public void setMessage( String s_message ) { 
        jlabelInstruct.setText( s_message );
    }
    
    private boolean showDialogResult = false;
    
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
            showDialogResult = false;
            final Runnable  run_dialog = new Runnable () {
                @Override
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
        return showDialogResult;
    }

    /**
     * Implement LoginModule CallbackHandler interface 
     * 
     * @param callbackList from a LoginModule or whatever to handle
     * @throws java.io.IOException if user cancels out of dialog
     * @throws javax.security.auth.callback.UnsupportedCallbackException for unexpected callback
     */
    @Override
    public void handle(Callback[] callbackList) throws IOException, UnsupportedCallbackException {
       for ( Callback callback : callbackList ) {
           if ( callback instanceof NameCallback) {
               final NameCallback ncb = (NameCallback) callback;
               if ( getUserName().isEmpty() && (ncb.getDefaultName () != null) ) {
                   setUserName( ncb.getDefaultName() );
                   break;
               }
           }
       }
       if ( showDialog () ) {
           for ( Callback callback : callbackList ) {
                if ( callback instanceof NameCallback) {
                    ((NameCallback) callback).setName(getUserName());
                } else if ( callback instanceof PasswordCallback) {
                    ((PasswordCallback) callback).setPassword( getPassword().toCharArray() );
                } else if ( callback instanceof TextOutputCallback ) {
                    jlabelInstruct.setText( "<html>Authenticate: " +
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



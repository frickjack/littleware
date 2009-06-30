/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo;

import com.google.inject.*;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import littleware.apps.client.AssetModelLibrary;
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.client.UiFeedback;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.ExtendedAssetViewController;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.AssertionFailedException;
import littleware.base.PropertiesGuice;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;


/**
 * Launch a Swing browser, and return the whatever
 */
public class LgoBrowserCommand extends AbstractLgoCommand<String,UUID> {
    private final static  Logger  olog = Logger.getLogger( LgoBrowserCommand.class.getName () );
    private  JAssetBrowser                obrowser       = null;
    private  ExtendedAssetViewController  ocontrol;
    private  JSimpleAssetToolbar          otoolbar = null;
    private JFrame                        owframe = null;
    private final AssetSearchManager      osearch;
    private final AssetModelLibrary       olib;
    private final AssetPathFactory        opathFactory;
    
    /**
     * Inject the browser that this command launches,
     * and the controller to attach to it.
     * Inject providers - Swing widgets will be allocated
     * and initialized on the Swing dispatch thread;
     * the LgoBrowserCommand may be allocated
     * and bootstrap on an lgo shell worker thread.
     * 
     * @param provideBrowser to launch on run
     * @param control to view browser
     * @param provideToolbar to connect to the browser
     */
    @Inject
    public LgoBrowserCommand( 
            final Provider<JAssetBrowser> provideBrowser,
            final Provider<ExtendedAssetViewController> provideControl,
            final Provider<JSimpleAssetToolbar>    provideToolbar,
            AssetSearchManager     search,
            AssetModelLibrary      lib,
            AssetPathFactory       pathFactory
            ) {
        super( LgoBrowserCommand.class.getName() );
        final Runnable runner = new Runnable () {
            @Override
            public void run() {
                obrowser = provideBrowser.get();
                otoolbar = provideToolbar.get();
                ocontrol = provideControl.get();
            }
        };
        if ( SwingUtilities.isEventDispatchThread() ) {
            runner.run();
        } else {
            try {
                SwingUtilities.invokeAndWait(runner);
            } catch (Exception ex) {
                throw new AssertionFailedException( "Failed to init LgoBrowserCommand", ex );
            }
        }
        osearch = search;
        olib = lib;
        opathFactory = pathFactory;
    }
    
    private UUID  ou_start = UUIDFactory.parseUUID( "00000000000000000000000000000000");
    
    /**
     * Property sets the asset to start browsing at.
     */
    public UUID getStart() {
        return ou_start;
    }
    public void setStart( UUID u_start ) {
        ou_start = u_start;
    }
    
    /**
     * Start browsing 
     * 
     * @param s_start_path reset start UUID to asset at other end of s_start_path
     * @return where the user stops browsing
     */
    @Override
    public synchronized UUID runSafe( UiFeedback feedback, String sPathIn ) {
        String sStartPath = sPathIn;
        if ( Whatever.empty(sStartPath) && (! getArgs().isEmpty() ) ) {
            sStartPath = getArgs().get(0);
        }
        if ( null != sStartPath ) {
            try {
                final Asset a_start = osearch.getAssetAtPath( opathFactory.createPath( sStartPath ) ).get();
                olib.syncAsset( a_start );
                setStart( a_start.getObjectId () );
            } catch ( Exception e ) {
                feedback.log( Level.WARNING, "Unable to load asset at path: " + sStartPath + ", caught: " + e );
            }
        }
        if ( SwingUtilities.isEventDispatchThread() ) {
            throw new IllegalStateException( "Cannot launch browser-command from Swing dispatch thread" );
        } else {
            SwingUtilities.invokeLater( new Runnable () {
                @Override
                public void run () {
                    createGUI ();
                }
            }
            );
            try {
                wait(); // browser frame notifies us on close
            } catch (InterruptedException ex) {
            }
        }
        return ouResult;
    }
    
    private UUID ouResult = ou_start;
    
    /**
     * Ste the result to the current browser view,
     * and notify the launching thread that the browse is done.
     */
    private synchronized void windowClosed () {
        if ( null != obrowser.getAssetModel() ) {
            ouResult = obrowser.getAssetModel().getAsset().getObjectId();
        }
        notifyAll();
        // cleanup in case the app wants to exit or tries to reuse this command
        // (which it shouldn't!)
        owframe.dispose();
        owframe = null;
    }


    /**
     * Allow subtypes to specialize the menu
     *
     * @return menubar to attach to the browser frame
     */
    protected JMenuBar  buildMenuBar () {
        final JMenuBar  jBar = new JMenuBar ();
        final JMenu     jMenuFile = new JMenu( "File" );

        final JMenuItem jMenItemQuit = new JMenuItem( new AbstractAction( "Exit" ) {

            @Override
            public void actionPerformed(ActionEvent e) {
                windowClosed();
            }

        });
        jMenItemQuit.setMnemonic( KeyEvent.VK_X );
        jMenuFile.add( jMenItemQuit );
        jMenuFile.setMnemonic( KeyEvent.VK_F );
        jBar.add( jMenuFile );

        return jBar;
    }

    private synchronized void createGUI () {
        if ( null == owframe ) {
            ocontrol.setControlView( obrowser );
            otoolbar.setConnectedView( obrowser );
            otoolbar.getButton ( JSimpleAssetToolbar.ButtonId.CREATE ).setEnabled ( true );
            otoolbar.getButton ( JSimpleAssetToolbar.ButtonId.EDIT ).setEnabled ( true );
            otoolbar.getButton ( JSimpleAssetToolbar.ButtonId.DELETE ).setEnabled ( true );
            otoolbar.addLittleListener( ocontrol );
            owframe = new JFrame( "Asset Browser" );
            owframe.setLayout ( new BorderLayout () );
            owframe.add ( otoolbar, BorderLayout.NORTH );
            owframe.add ( obrowser, BorderLayout.CENTER );
            final JButton jButtonSelect = new JButton( "Select" );
            jButtonSelect.addActionListener( new ActionListener() {

                @Override
                public void actionPerformed(ActionEvent e) {
                    windowClosed();
                }
            
            }
            );
            final JPanel  jPanelButton = new JPanel( new FlowLayout( FlowLayout.RIGHT ));
            jPanelButton.add( jButtonSelect );
            owframe.add( jPanelButton, BorderLayout.PAGE_END );
            owframe.setJMenuBar(buildMenuBar() );
            owframe.pack ();
            //owframe.setDefaultCloseOperation( WindowConstants. );
            owframe.addWindowListener(new WindowAdapter() {

                @Override
                public void windowClosing(WindowEvent ev) {
                    if (null != owframe) {
                        owframe.removeWindowListener(this);
                        LgoBrowserCommand.this.windowClosed();
                    }
                }

                @Override
                public void windowClosed(WindowEvent e) {
                    if (null != owframe) {
                        owframe.removeWindowListener(this);
                        LgoBrowserCommand.this.windowClosed();
                    }
                }
            });
        }
            
        if ( null != getStart() ) {
            try {
                obrowser.setAssetModel( olib.retrieveAssetModel( getStart(), osearch ) );
            } catch ( Exception e ) {
                olog.log( Level.INFO, "Failed to load initial asset model " + getStart () +
                        ", caught: " + e, e );
            }
        }
        if ( ! owframe.isVisible() ) {
            owframe.setVisible( true );
        }

    }

    @Override
    public LgoBrowserCommand clone() {
        return (LgoBrowserCommand) super.clone();
    }
    
    /**
     * Launch a browser with the session
     * loaded from littleware.properties.
     * 
     * @param v_args command-line args
     * @TODO setup standard feedback mechanism in StandardSwingGuice
     */
    public static void main( String[] v_args ) {
        try {
            Injector     injector = Guice.createInjector( new Module[] {
                            new EzModule(),
                            new littleware.apps.swingclient.StandardSwingGuice(),
                            new littleware.apps.client.StandardClientGuice(),
                            new littleware.apps.misc.StandardMiscGuice(),
                            new littleware.security.auth.ClientServiceGuice(),
                            new PropertiesGuice( littleware.base.PropertiesLoader.get().loadProperties() )
                        }
            );
            UiFeedback        feedback = new LoggerUiFeedback();
            LgoBrowserCommand command = injector.getInstance( LgoBrowserCommand.class );
            command.runDynamic( feedback, "/littleware.home/" );
        } catch ( final Exception ex ) {
            olog.log( Level.SEVERE, "Failed command, caught: " + ex, ex );
            try {
                final Runnable runner = new Runnable() {

                    @Override
                    public void run() {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Launch failed", JOptionPane.ERROR_MESSAGE);
                    }
                };
                if ( SwingUtilities.isEventDispatchThread() ) {
                    runner.run();
                } else {
                    olog.log( Level.INFO, "Waiting for user input ..." );
                    SwingUtilities.invokeAndWait( runner );
                }
            } catch (Exception ex1) {
                olog.log(Level.SEVERE, null, ex1);
            } 
            System.exit( 1 );
        }
    }    
}

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
import littleware.base.PropertiesGuice;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;


/**
 * Launch a Swing browser, and return the whatever
 */
public class LgoBrowserCommand extends AbstractLgoCommand<String,UUID> {
    private final static  Logger  olog = Logger.getLogger( LgoBrowserCommand.class.getName () );
    private final JAssetBrowser                obrowser;
    private final ExtendedAssetViewController  ocontrol;
    private final JSimpleAssetToolbar          otoolbar;
    private JFrame                       owframe = null;
    private final AssetSearchManager           osearch;
    private final AssetModelLibrary            olib;
    
    /**
     * Inject the browser that this command launches,
     * and the controller to attach to it.
     * 
     * @param browser to launch on run
     * @param control to view browser
     * @param toolbar to connect to the browser
     */
    @Inject
    public LgoBrowserCommand( JAssetBrowser browser,
            ExtendedAssetViewController control,
            JSimpleAssetToolbar    toolbar,
            AssetSearchManager     search,
            AssetModelLibrary      lib
            ) {
        super( LgoBrowserCommand.class.getName() );
        obrowser = browser;
        ocontrol = control;
        otoolbar = toolbar;        
        osearch = search;
        olib = lib;
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
    public UUID runSafe( UiFeedback feedback, String sPathIn ) {
        String sStartPath = sPathIn;
        if ( Whatever.empty(sStartPath) && (! getArgs().isEmpty() ) ) {
            sStartPath = getArgs().get(0);
        }
        if ( null != sStartPath ) {
            try {
                Asset a_start = osearch.getAssetAtPath( AssetPathFactory.getFactory ().createPath( sStartPath ) );
                olib.syncAsset( a_start );
                setStart( a_start.getObjectId () );
            } catch ( Exception e ) {
                feedback.log( Level.WARNING, "Unable to load asset at path: " + sStartPath + ", caught: " + e );
            }
        }
        if ( SwingUtilities.isEventDispatchThread() ) {
            createGUI ();
        } else {
            SwingUtilities.invokeLater( new Runnable () {
                @Override
                public void run () {
                    createGUI ();
                }
            }
            );
        }
        return null;
    }
    
    /**
     * Little hook by which main() can tell the command
     * to exit when closed.  May make this a public property
     * in the future if it turns out to be a generally
     * needed thing.
     */
    private boolean ob_exit_on_close = false;
    
    private void createGUI () {
        if ( null == owframe ) {
            ocontrol.setControlView( obrowser );
            otoolbar.setConnectedView( obrowser );
            otoolbar.getButton ( JSimpleAssetToolbar.ButtonId.CREATE ).setEnabled ( true );
            otoolbar.getButton ( JSimpleAssetToolbar.ButtonId.EDIT ).setEnabled ( true );
            otoolbar.getButton ( JSimpleAssetToolbar.ButtonId.DELETE ).setEnabled ( true );
            otoolbar.addLittleListener( ocontrol );
            owframe = new JFrame( "Asset Browser" );
            owframe.setLayout ( new BorderLayout () );
            owframe.add ( otoolbar, BorderLayout.PAGE_START );
            owframe.add ( obrowser, BorderLayout.CENTER );
            owframe.pack (); 
            
            if ( null != getStart() ) {
                try {
                    obrowser.setAssetModel( olib.retrieveAssetModel( getStart(), osearch ) );
                } catch ( Exception e ) {
                    olog.log( Level.INFO, "Failed to load initial asset model " + getStart () +
                            ", caught: " + e, e );
                }
            }
            if ( ob_exit_on_close ) {
                owframe.setDefaultCloseOperation( WindowConstants.EXIT_ON_CLOSE );
            }
            if ( ! owframe.isVisible() ) {
                owframe.setVisible( true );
            }
        }    
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
            command.ob_exit_on_close = true;
            command.runDynamic( feedback, "/littleware.home/" );
        } catch ( final Exception ex ) {
            olog.log( Level.SEVERE, "Failed command, caught: " + ex, ex );
            try {
                SwingUtilities.invokeAndWait(new Runnable() {

                    public void run() {
                        JOptionPane.showMessageDialog(null, "Error: " + ex.getMessage(), "Launch failed", JOptionPane.ERROR_MESSAGE);
                    }
                });
            } catch (Exception ex1) {
                olog.log(Level.SEVERE, null, ex1);
            } 
            System.exit( 1 );
        }
    }    
}

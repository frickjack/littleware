/*
 * Copyright 2008 Reuben Pasquini
 * All Rights Reserved
 */

package littleware.apps.lgo;

import java.awt.BorderLayout;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.swing.*;

import com.google.inject.Inject;

import littleware.apps.client.AssetModel;
import littleware.apps.client.AssetModelLibrary;
import littleware.apps.swingclient.*;
import littleware.apps.swingclient.controller.ExtendedAssetViewController;
import littleware.asset.Asset;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.base.UUIDFactory;


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
    public UUID runSafe( String s_start_path ) {        
        if ( null != s_start_path ) {
            try {
                Asset a_start = osearch.getAssetAtPath( AssetPathFactory.getFactory ().createPath( s_start_path ) );
                olib.syncAsset( a_start );
                setStart( a_start.getObjectId () );
            } catch ( Exception e ) {
                olog.log( Level.WARNING, "Unable to load asset at path: " + s_start_path + ", caught: " + e,
                        e
                        );
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
        if ( ! owframe.isVisible() ) {
            owframe.setVisible( true );
        }
    }    
}}

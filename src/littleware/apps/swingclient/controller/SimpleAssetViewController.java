package littleware.apps.swingclient.controller;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.swingclient.LittleListener;
import littleware.apps.swingclient.LittleEvent;
import littleware.apps.swingclient.AssetModel;
import littleware.apps.swingclient.AssetModelLibrary;
import littleware.apps.swingclient.AssetView;
import littleware.apps.swingclient.event.NavRequestEvent;
import littleware.asset.AssetRetriever;

/** 
 * Simple controller watches for NavRequestEvents,
 * then invokes setAssetModel on the constructor-supplied AssetView.
 * Popup error dialog on failure to navigate.
 */
public class SimpleAssetViewController implements LittleListener {
    private static final Logger      olog_generic = Logger.getLogger ( "littleware.apps.swingclient.controller.SimpleAssetViewController" );
    private final AssetView          oview_control;
    private final AssetRetriever     om_retriever;
    private final AssetModelLibrary  olib_asset;
    
    
    /**
     * Constructor injects needed data
     *
     * @param view_control to setAssetModel() against for NavRequestEvents
     * @param m_retriever to resolve NavRequest UUIDs 
     * @param lib_asset to stash retrieved assets in
     */
    public SimpleAssetViewController ( AssetView  view_control,
                                       AssetRetriever m_retriever,
                                       AssetModelLibrary  lib_asset
                                       ) {
        oview_control = view_control;
        om_retriever = m_retriever;
        olib_asset = lib_asset;
    }

    /**
     * Resolve NavRequestEvents.  Client registers this controller
     * as a listener on LitttleTool that should control the views navigation.
     */
    public void receiveLittleEvent ( LittleEvent event_little ) {
        if ( event_little instanceof NavRequestEvent ) {
            //event_little.setSource ( JGenericAssetView.this );
            NavRequestEvent event_nav = (NavRequestEvent) event_little;
            final UUID      u_destination = event_nav.getDestination ();
            
            if ( (null == u_destination)
                 || (
                     (null != oview_control.getAssetModel ())
                     && u_destination.equals ( oview_control.getAssetModel ().getAsset ().getObjectId () )
                     )
                 ) {
                // No need to navigate
                olog_generic.log ( Level.FINE, "Ignoring null NavRequest" );
                return;
            }
            
            try {
                AssetModel model_new = olib_asset.retrieveAssetModel ( u_destination, om_retriever );
                olog_generic.log ( Level.FINE, "Navigationg to " + model_new.getAsset () );
                oview_control.setAssetModel( model_new );
            } catch ( RuntimeException e ) {
                throw e;
            } catch ( final Exception e ) {
                olog_generic.log ( Level.INFO, "Failure to navigate to " + u_destination + 
                                       ", caught: " + e 
                                       );
                SwingUtilities.invokeLater ( new Runnable () {
                    public void run () {
                        JOptionPane.showMessageDialog(null, "Could not navigate to " + u_destination +
                                              ", caught: " + e, "alert", 
                                              JOptionPane.ERROR_MESSAGE
                                              );
                    }
                }
                                             );
            }
        }
    }
    
}



// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.controller;

import com.google.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.apps.swingclient.event.NavRequestEvent;
import littleware.apps.swingclient.event.RefreshRequestEvent;
import littleware.asset.AssetRetriever;

/** 
 * Simple controller watches for events (NavRequestEvent, RefreshRequestEvent),
 * then invokes setAssetModel on the constructor-supplied AssetView.
 * Popup error dialog on failure to navigate.
 */
public class SimpleAssetViewController implements LittleListener {
    private static final Logger      olog_generic = Logger.getLogger ( "littleware.apps.swingclient.controller.SimpleAssetViewController" );
    private AssetView          oview_control = null;
    private final AssetRetriever     om_retriever;
    private final AssetModelLibrary  olib_asset;
    
    
    /**
     * Constructor injects needed data
     *
     * @param m_retriever to resolve NavRequest UUIDs 
     * @param lib_asset to stash retrieved assets in
     */
    @Inject
    public SimpleAssetViewController ( 
                                       AssetRetriever m_retriever,
                                       AssetModelLibrary  lib_asset
                                       ) {      
        om_retriever = m_retriever;
        olib_asset = lib_asset;
    }
    
    /**
     * Property tracks the AssetView that this controller
     * manipulates in response to events - especially
     * view.setAssetModel on NavRequestEvent.
     * Automatically registers this controller as a LittleListener
     * on view too, and unregisters on pervious view if any.
     */
    public void setControlView( AssetView view ) {
        if ( null != oview_control ) {
            oview_control.removeLittleListener( this );
        }
        oview_control = view;
        oview_control.addLittleListener(this);
    }
    public AssetView getControlView() {
        return oview_control;
    }

    /**
     * Resolve NavRequestEvents.  Client registers this controller
     * as a listener on LitttleTool that should control the views navigation.
     */
    @Override
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
                    @Override
                    public void run () {
                        JOptionPane.showMessageDialog(null, "Could not navigate to " + u_destination +
                                              ", caught: " + e, "alert", 
                                              JOptionPane.ERROR_MESSAGE
                                              );
                    }
                }
                                             );
            }
        } else if ( event_little instanceof RefreshRequestEvent ) {
            try {
                olog_generic.log( Level.FINE, "REFRESH!" );
                final UUID uAsset = oview_control.getAssetModel().getAsset().getObjectId();
                olib_asset.syncAsset(
                        om_retriever.getAsset( uAsset ).get()
                        );
                final List<UUID> vChildren = new ArrayList<UUID>();
                vChildren.addAll(om_retriever.getAssetIdsFrom( uAsset, null ).values());
                if ( ! vChildren.isEmpty() ) {
                    olib_asset.syncAsset(
                            om_retriever.getAssets( vChildren )
                            );
                }
            } catch ( final Exception ex ) {
                SwingUtilities.invokeLater(new Runnable () {
                    @Override
                    public void run () {
                        JOptionPane.showMessageDialog(null,
                                "Could not refresh view, caught: " + ex, "alert",
                                              JOptionPane.ERROR_MESSAGE
                                              );
                    }
                }
                                             );
            }
        }
    }
    
}




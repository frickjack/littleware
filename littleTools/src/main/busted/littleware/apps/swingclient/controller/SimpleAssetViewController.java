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

import littleware.apps.swingclient.AssetView;
import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetLibrary;
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
import littleware.asset.internal.RemoteAssetRetriever;
import littleware.asset.client.spi.ClientCache;
import littleware.base.Maybe;
import littleware.base.event.LittleEvent;
import littleware.base.event.LittleListener;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.NullFeedback;


/** 
 * Simple controller watches for events (NavRequestEvent, RefreshRequestEvent),
 * then invokes setAssetModel on the constructor-supplied AssetView.
 * Popup error dialog on failure to navigate.
 */
public class SimpleAssetViewController implements LittleListener {
    private static final Logger      olog_generic = Logger.getLogger ( "littleware.apps.swingclient.controller.SimpleAssetViewController" );
    private AssetView          oview_control = null;
    private final RemoteAssetRetriever     om_retriever;
    private final AssetLibrary  olib_asset;
    private final ClientCache oclientCache;
    private Feedback ofeedback = new NullFeedback();
    
    
    /**
     * Constructor injects needed data
     *
     * @param m_retriever to resolve NavRequest UUIDs 
     * @param lib_asset to stash retrieved assets in
     */
    @Inject
    public SimpleAssetViewController ( 
                                       RemoteAssetRetriever m_retriever,
                                       AssetLibrary  lib_asset,
                                       ClientCache        clientCache
                                       ) {      
        om_retriever = m_retriever;
        olib_asset = lib_asset;
        oclientCache = clientCache;
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
     * Feedback object controller should send info to show
     * progress
     *
     * @param feedback
     */
    public void setFeedback( Feedback feedback ) {
        ofeedback = feedback;
    }
    public Feedback getFeedback() {
        return ofeedback;
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
                     && u_destination.equals ( oview_control.getAssetModel ().getAsset ().getId () )
                     )
                 ) {
                // No need to navigate
                olog_generic.log ( Level.FINE, "Ignoring null NavRequest" );
                return;
            }
            final Runnable runPopup =new Runnable () {
                    @Override
                    public void run () {
                        JOptionPane.showMessageDialog(null, "Could not navigate to " + u_destination, "alert",
                                              JOptionPane.ERROR_MESSAGE
                                              );
                    }
                };

            try {
                final Option<AssetRef> maybe = olib_asset.retrieveAssetModel ( u_destination, om_retriever );
                if ( maybe.isSet() ) {
                    if ( olog_generic.isLoggable(Level.FINE)) {
                        olog_generic.log ( Level.FINE, "Navigationg to " + maybe.get().getAsset () );
                    }
                    oview_control.setAssetModel( maybe.get() );
                } else {
                    olog_generic.log( Level.INFO, "No data for nav asset: " + u_destination );
                    SwingUtilities.invokeLater( runPopup );
                }
            } catch ( RuntimeException e ) {
                throw e;
            } catch ( final Exception ex ) {
                olog_generic.log ( Level.INFO, "Failure to navigate to " + u_destination, ex );
                SwingUtilities.invokeLater ( runPopup );
            }
        } else if ( event_little instanceof RefreshRequestEvent ) {
            try {
                olog_generic.log( Level.FINE, "REFRESH!" );
                oclientCache.getCache().clear();
                final UUID uAsset = oview_control.getAssetModel().getAsset().getId();
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



/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.swingclient.controller;

import littleware.base.feedback.LittleEvent;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.beans.PropertyChangeEvent;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.WindowConstants;

import com.nexes.wizard.Wizard;
import java.beans.PropertyChangeListener;
import javax.swing.BoxLayout;
import javax.swing.JDialog;
import littleware.apps.client.*;
import littleware.apps.swingclient.DeleteAssetStrategy;
import littleware.apps.swingclient.IconLibrary;
import littleware.apps.swingclient.JDeleteAssetBuilder;
import littleware.apps.swingclient.event.*;
import littleware.apps.swingclient.wizard.CreateAssetWizard;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.client.ClientCache;
import littleware.security.auth.LittleSession;



/** 
 * Simple controller watches for NavRequestEvents,
 * then invokes setAssetModel on the constructor-supplied AssetView.
 * Popup error dialog on failure to navigate.
 */
public class ExtendedAssetViewController extends SimpleAssetViewController {
    private static final Logger      olog_generic = Logger.getLogger ( "littleware.apps.swingclient.controller.ExtendedAssetViewController" );
    private final AssetManager       om_asset;
    private final AssetSearchManager om_search;
    private final AssetModelLibrary  olib_asset;
    private final IconLibrary        olib_icon;
    private final LittleSession      oa_session;
    private final AssetEditorFactory ofactory_edit;
    private final AssetViewFactory   ofactory_view;
    private final Provider<CreateAssetWizard> oprovideWizard;
    private final JDeleteAssetBuilder buildDelete;

    
    
    /**
     * Constructor injects needed data
     *
     * @param view_control to setAssetModel() against for NavRequestEvents
     * @param m_retriever to resolve NavRequest UUIDs 
     * @param lib_asset to stash retrieved assets in
     * @param factory_edit to create asset-editor controls with
     * @param factory_view for view-only editor child-browsers
     * @param lib_icon source of icons
     * @param a_session under which this controller is operating
     */
    @Inject
    public ExtendedAssetViewController ( 
                                         AssetSearchManager m_search,
                                         AssetManager       m_asset,
                                         AssetModelLibrary  lib_asset,
                                         AssetEditorFactory factory_edit,
                                         AssetViewFactory   factory_view,
                                         IconLibrary        lib_icon,
                                         LittleSession      a_session,
                                         Provider<CreateAssetWizard> provideWizard,
                                         ClientCache cache,
                                         JDeleteAssetBuilder buildDelete
                                       ) {
        super( m_search, lib_asset, cache );
                
        om_asset = m_asset;
        om_search = m_search;
        olib_asset = lib_asset;
        ofactory_edit = factory_edit;
        ofactory_view = factory_view;
        olib_icon = lib_icon;
        oa_session = a_session;
        oprovideWizard = provideWizard;
        this.buildDelete = buildDelete;
    }
        
    /**
     * Resolve NavRequestEvents.  Client registers this controller
     * as a listener on LitttleTool that should control the views navigation.
     */
    @Override
    public void receiveLittleEvent ( LittleEvent event_little ) {        
        if ( 
                    (event_little instanceof CreateRequestEvent)
                    || (event_little instanceof EditRequestEvent)
                    ) {
            AssetModel amodel_edit = null;
            
            if ( event_little instanceof CreateRequestEvent ) {
                final CreateRequestEvent event_create = (CreateRequestEvent) event_little;
                final AssetModel         amodel_view = event_create.getAssetModel ();
                final AssetBuilder       assetBuilder = AssetType.GENERIC.create ().
                        ownerId ( oa_session.getOwnerId () ).
                        name ( "username.new_asset" );

                int                       i_create_result = Wizard.ERROR_RETURN_CODE;
                final   CreateAssetWizard wizard_create = oprovideWizard.get();
                try {
                    // Give the asset local changes not in the AssetModelLibrary
                    if ( null != amodel_view ) {
                        assetBuilder.parent( amodel_view.getAsset() );
                    }
                    wizard_create.setAssetModel(olib_asset.syncAsset( assetBuilder.build() ) );
                    i_create_result = wizard_create.showModalDialog ();
                    
                    if ( Wizard.FINISH_RETURN_CODE != i_create_result ) {
                        // new-asset creation canceled for some reason - 
                        olib_asset.remove ( assetBuilder.getId () );
                        amodel_edit = null;
                    } else {
                        // Note - asset-model may be replaced with a new object
                        // as side-effect of create process (remove then add)
                        wizard_create.saveLocalChanges(om_asset, "new asset create");
                        amodel_edit = wizard_create.getAssetModel();
                    }
                        
                } catch ( Exception e ) {
                    olog_generic.log ( Level.WARNING, "Asset-create caught unexpected", e );
                    olib_asset.remove ( assetBuilder.getId () );
                    JOptionPane.showMessageDialog(null, "Could not create new asset, caught: " + e,
                                                  "alert", 
                                                  JOptionPane.ERROR_MESSAGE
                                                  );  
                    olib_asset.remove ( assetBuilder.getId () );
                    amodel_edit = null;
                }
            } else {
                amodel_edit = ((EditRequestEvent) event_little).getAssetModel ();
            }
            if ( null != amodel_edit ) {
                final AssetEditor edit_asset = ofactory_edit.createView ( amodel_edit );
                final JFrame      wframe_edit = new JFrame ();
                wframe_edit.getContentPane().setLayout(
                        new BoxLayout( wframe_edit.getContentPane(), BoxLayout.Y_AXIS )
                        );
                
                edit_asset.addLittleListener ( this );
                //((JComponent) edit_asset).setPreferredSize( new Dimension( 700, 700 ) );
                wframe_edit.setDefaultCloseOperation ( WindowConstants.DISPOSE_ON_CLOSE );
                wframe_edit.getContentPane().add ( (JComponent) edit_asset );
                wframe_edit.pack ();
                wframe_edit.setVisible ( true );
            }
        } else if ( event_little instanceof DeleteRequestEvent ) {
            final DeleteRequestEvent  event_delete = (DeleteRequestEvent) event_little;
            final AssetModel          amodel_delete = event_delete.getAssetModel ();
            final Asset               a_delete = amodel_delete.getAsset ();
            final JDeleteAssetBuilder.JDeletePanel delete = buildDelete.build( a_delete.getId() );
            final JDialog             dialog = new JDialog();
            dialog.setTitle( "Delete Asset Subtree" );
            dialog.add(delete);
            dialog.pack();
            dialog.setModal( false );
            delete.addPropertyChangeListener( new PropertyChangeListener() {

                @Override
                public void propertyChange(PropertyChangeEvent evt) {
                    if( evt.getPropertyName().equals( "state" )
                            && evt.getNewValue().equals( DeleteAssetStrategy.State.Dismissed )
                            ) {
                        dialog.dispose();
                    }
                }
            } );
            dialog.setDefaultCloseOperation( WindowConstants.DISPOSE_ON_CLOSE );
            dialog.setVisible( true );
            delete.launch();
        } else if ( event_little instanceof SaveRequestEvent ) {
            // Save request from spawned editor
            SaveRequestEvent event_save = (SaveRequestEvent) event_little;
            try {
                event_save.doSave ( om_asset );
            } catch ( Exception e ) {
                olog_generic.log ( Level.WARNING, "Asset-save caught unexpected", e );
                JOptionPane.showMessageDialog(null, "Could not save asset, caught: " + e,
                                              "alert", 
                                              JOptionPane.ERROR_MESSAGE
                                              );
            }                
        } else {
            super.receiveLittleEvent ( event_little );
        }
    }
}


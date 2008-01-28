package littleware.apps.swingclient.controller;

import com.google.inject.Inject;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;

import com.nexes.wizard.Wizard;
import littleware.apps.swingclient.LittleListener;
import littleware.apps.swingclient.LittleEvent;
import littleware.apps.swingclient.AssetEditor;
import littleware.apps.swingclient.AssetEditorFactory;
import littleware.apps.swingclient.AssetModel;
import littleware.apps.swingclient.AssetModelLibrary;
import littleware.apps.swingclient.AssetView;
import littleware.apps.swingclient.AssetViewFactory;
import littleware.apps.swingclient.IconLibrary;
import littleware.apps.swingclient.event.*;
import littleware.apps.swingclient.wizard.CreateAssetWizard;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.security.auth.LittleSession;


/** 
 * Simple controller watches for NavRequestEvents,
 * then invokes setAssetModel on the constructor-supplied AssetView.
 * Popup error dialog on failure to navigate.
 */
public class ExtendedAssetViewController extends SimpleAssetViewController {
    private static final Logger      olog_generic = Logger.getLogger ( "littleware.apps.swingclient.controller.ExtendedAssetViewController" );
    private final AssetView          oview_control;
    private final AssetManager       om_asset;
    private final AssetSearchManager om_search;
    private final AssetModelLibrary  olib_asset;
    private final IconLibrary        olib_icon;
    private final LittleSession      oa_session;
    private final AssetEditorFactory ofactory_edit;
    private final AssetViewFactory   ofactory_view;

    
    
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
    public ExtendedAssetViewController ( AssetView          view_control,
                                         AssetSearchManager m_search,
                                         AssetManager       m_asset,
                                         AssetModelLibrary  lib_asset,
                                         AssetEditorFactory factory_edit,
                                         AssetViewFactory   factory_view,
                                         IconLibrary        lib_icon,
                                         LittleSession      a_session
                                       ) {
        super( view_control, m_search, lib_asset );
        
        oview_control = view_control;
        om_asset = m_asset;
        om_search = m_search;
        olib_asset = lib_asset;
        ofactory_edit = factory_edit;
        ofactory_view = factory_view;
        olib_icon = lib_icon;
        oa_session = a_session;
    }
        
    /**
     * Resolve NavRequestEvents.  Client registers this controller
     * as a listener on LitttleTool that should control the views navigation.
     */
    public void receiveLittleEvent ( LittleEvent event_little ) {        
        if ( event_little instanceof NavRequestEvent ) {
            super.receiveLittleEvent ( event_little );
        } else if ( 
                    (event_little instanceof CreateRequestEvent)
                    || (event_little instanceof EditRequestEvent)
                    ) {
            AssetModel amodel_edit = null;
            
            if ( event_little instanceof CreateRequestEvent ) {
                CreateRequestEvent event_create = (CreateRequestEvent) event_little;
                AssetModel         amodel_view = event_create.getAssetModel ();
                Asset              a_new = AssetType.GENERIC.create ();
                
                a_new.setName ( "username.new_asset" );
                if ( null != amodel_view ) {
                    Asset a_from = amodel_view.getAsset ();
                    
                    a_new.setFromId ( a_from.getObjectId () );
                    a_new.setHomeId ( a_from.getHomeId () );
                    a_new.setOwnerId ( oa_session.getOwnerId () );
                    a_new.setAclId ( a_from.getAclId () );
                }

                final   AssetModel        amodel_new = olib_asset.syncAsset( a_new );
                int                       i_create_result = Wizard.ERROR_RETURN_CODE;
                final   CreateAssetWizard wizard_create;
                try {
                    wizard_create = new CreateAssetWizard ( 
                                                                     om_asset,
                                                                     om_search,
                                                                     olib_asset,
                                                                     olib_icon,
                                                                     ofactory_view,
                                                                     null,
                                                                     amodel_new
                                                                     );  
                    i_create_result = wizard_create.showModalDialog ();
                    
                    if ( Wizard.FINISH_RETURN_CODE != i_create_result ) {
                        // new-asset creation canceled for some reason - 
                        olib_asset.remove ( a_new.getObjectId () );
                        amodel_edit = null;
                    } else {
                        wizard_create.saveLocalChanges ( om_asset, "new asset create" );
                        amodel_edit = amodel_new;
                    }
                        
                } catch ( Exception e ) {
                    olog_generic.log ( Level.WARNING, "Asset-create caught unexpected: " + e );
                    olib_asset.remove ( a_new.getObjectId () );
                    JOptionPane.showMessageDialog(null, "Could not create new asset, caught: " + e,
                                                  "alert", 
                                                  JOptionPane.ERROR_MESSAGE
                                                  );  
                    olib_asset.remove ( a_new.getObjectId () );
                    amodel_edit = null;
                }
            } else {
                amodel_edit = ((EditRequestEvent) event_little).getAssetModel ();
            }
            if ( null != amodel_edit ) {
                final AssetEditor edit_asset = ofactory_edit.createView ( amodel_edit );
                final JFrame      wframe_edit = new JFrame ();
                
                edit_asset.addLittleListener ( this );
                wframe_edit.setDefaultCloseOperation ( WindowConstants.DISPOSE_ON_CLOSE );
                wframe_edit.getContentPane().add ( (JComponent) edit_asset );
                wframe_edit.pack ();
                wframe_edit.setVisible ( true );
            }
        } else if ( event_little instanceof DeleteRequestEvent ) {
            DeleteRequestEvent  event_delete = (DeleteRequestEvent) event_little;
            AssetModel          amodel_delete = event_delete.getAssetModel ();
            Asset               a_delete = amodel_delete.getAsset ();
            
            String s_reason = JOptionPane.showInputDialog ( null,
                       "Enter a reason for the delete, or Cancel",
                       "no reason for delete"
                       );
            
            if ( null != s_reason ) {
                try {
                    om_asset.deleteAsset ( a_delete.getObjectId (), s_reason );
                    olib_asset.assetDeleted ( a_delete.getObjectId () );

                    if ( a_delete.getObjectId ().equals ( oview_control.getAssetModel ().getAsset ().getObjectId () ) ) {
                        /**
                         * ... then move the view to something that has not been deleted
                         */
                        UUID u_view = a_delete.getFromId ();
                        if ( null == u_view ) {
                            u_view = a_delete.getHomeId ();
                        }
                        NavRequestEvent event_nav = new NavRequestEvent ( event_little.getSource (),
                                                                          u_view, 
                                                                          NavRequestEvent.NavMode.GENERIC
                                                                          );
                        super.receiveLittleEvent ( event_nav );
                    }
                } catch ( Exception e ) {
                    olog_generic.log ( Level.WARNING, "Asset-delete caught unexpected: " + e );
                    JOptionPane.showMessageDialog(null, "Could not delete asset, caught: " + e,
                                                  "alert", 
                                                  JOptionPane.ERROR_MESSAGE
                                                  );                                        
                }
            }
            
            
        } else if ( event_little instanceof SaveRequestEvent ) {
            // Save request from spawned editor
            SaveRequestEvent event_save = (SaveRequestEvent) event_little;
            try {
                event_save.doSave ( om_asset );
            } catch ( Exception e ) {
                olog_generic.log ( Level.WARNING, "Asset-save caught unexpected: " + e );
                JOptionPane.showMessageDialog(null, "Could not save asset, caught: " + e,
                                              "alert", 
                                              JOptionPane.ERROR_MESSAGE
                                              );
            }                
        }
    }
}


// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


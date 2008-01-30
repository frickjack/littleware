package littleware.apps.swingclient.controller;

import com.google.inject.Inject;
import java.awt.Cursor;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Collections;
import java.util.UUID;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.JDialog;

import com.nexes.wizard.Wizard;

import littleware.apps.swingclient.*;
import littleware.apps.swingclient.event.CreateRequestEvent;
import littleware.apps.swingclient.event.EditRequestEvent;
import littleware.apps.swingclient.event.DeleteRequestEvent;
import littleware.apps.swingclient.event.NavRequestEvent;
import littleware.apps.swingclient.wizard.CreateAssetWizard;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetManager;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.security.LittleGroup;
import littleware.security.SecurityAssetType;

/** 
 * Little controller that allows the manipulation of
 * a JGroupsUnderParentView via a JSimpleAssetToolbar.
 * Just extends the SimpleAssetViewController to allow
 * Group edit and creation.
 */
public class GroupFolderTool extends SimpleAssetViewController implements PropertyChangeListener {
    private final static Logger            olog_generic = Logger.getLogger ( "littleware.apps.swingclient.controller.GroupFolderTool" );
    
    private final  JSimpleAssetToolbar     owtbar_control;
    private final  AssetSearchManager      om_search;
    private final  AssetManager            om_asset;
    private final  AssetModelLibrary       olib_asset;
    private final  AssetViewFactory        ofactory_view;
    private final  IconLibrary             olib_icon;
    
    /**
     * Inject the dependencies this thing needs.
     * Clients may prefer to use the create() static method.
     *
     * @param lib_asset AssetModelLibrary source of our asset models
     * @param m_asset for creating new groups
     * @param m_search for looking up data
     * @param lib_icon source of icons
     * @param factory_view to supply to child browsers as necessary
     * @param wtbar_control to listen on - also listen on its connected view
     */
    @Inject
    public GroupFolderTool ( AssetModelLibrary  lib_asset,
                            AssetManager        m_asset,
                            AssetSearchManager  m_search,
                            IconLibrary         lib_icon,
                            AssetViewFactory    factory_view,
                            JSimpleAssetToolbar wtbar_control
                                                       )
    {
        super ( m_search, lib_asset );
        olib_asset = lib_asset;
        olib_icon = lib_icon;
        om_asset = m_asset;
        om_search = m_search;
        ofactory_view = factory_view;
        owtbar_control = wtbar_control;
        setControlView( wtbar_control.getConnectedView () );
        owtbar_control.addLittleListener ( this );
        owtbar_control.getConnectedView ().addLittleListener ( this );
        owtbar_control.getConnectedView ().addPropertyChangeListener ( this );
        owtbar_control.getButton ( JSimpleAssetToolbar.ButtonId.CREATE ).setEnabled ( true );

        if ( ! (wtbar_control.getConnectedView () instanceof JGroupsUnderParentView) ) {
            throw new AssertionFailedException ( "Toolbar must control instanceof JGroupsUnderParentView" );
        }
    }
    
    /**
     * Factory utility takes care of allocating and initializing
     * a JSimpleAssetToolbar wired up with a JGroupsUnderParentView.
     *
     * @param amodel_parent to view groups under
     * @param m_asset for create/edit
     * @param m_search for navigation
     * @param lib_icon icon source
     */
    public static GroupFolderTool create ( AssetModel          amodel_parent,
                                           AssetManager        m_asset,
                                           AssetSearchManager  m_search, 
                                           AssetViewFactory    factory_view,                                           
                                           IconLibrary         lib_icon
                                           ) 
    {
        JGroupsUnderParentView               wgroups_main = new JGroupsUnderParentView ( amodel_parent.getLibrary (), lib_icon, m_search );
        List<JSimpleAssetToolbar.ButtonId>   v_buttons = new ArrayList<JSimpleAssetToolbar.ButtonId> ();
        v_buttons.add ( JSimpleAssetToolbar.ButtonId.CREATE );
        v_buttons.add ( JSimpleAssetToolbar.ButtonId.EDIT );
        v_buttons.add ( JSimpleAssetToolbar.ButtonId.DELETE );
        JSimpleAssetToolbar                  wtbar_control = new JSimpleAssetToolbar ( wgroups_main, amodel_parent.getLibrary (),
                                                                                       lib_icon, m_search, "GroupFolderToolbar",
                                                                                       v_buttons
                                                                                     );
        wgroups_main.setAssetModel ( amodel_parent );

        return new GroupFolderTool ( amodel_parent.getLibrary (),
                                     m_asset, m_search,
                                     lib_icon, factory_view, wtbar_control
                                     );
    }
    
    
    
    /**
     * Get the toolbar this controller is listening on.
     * The JGroupsUnderParent component is available via
     * getToolbar ().getConnectedView ().
     */
    public JSimpleAssetToolbar     getToolbar () {
        return owtbar_control;
    }
    
    /**
     * Convenience method - same as getToolbar ().getConnectedView () with a cast.
     */
    public JGroupsUnderParentView getGroupsView () {
        return (JGroupsUnderParentView) getToolbar ().getConnectedView ();
    }
    
    public void propertyChange ( PropertyChangeEvent evt_prop ) {
        JGroupsUnderParentView  groupview = (JGroupsUnderParentView) owtbar_control.getConnectedView ();
        if ( evt_prop.getSource () == groupview ) {
            LittleGroup  group_selected = groupview.getSelectedGroup ();

            if ( null != group_selected ) {
                owtbar_control.getButton ( JSimpleAssetToolbar.ButtonId.EDIT ).setEnabled ( true );
                owtbar_control.getButton ( JSimpleAssetToolbar.ButtonId.DELETE ).setEnabled ( true );
            } else {
                owtbar_control.getButton ( JSimpleAssetToolbar.ButtonId.EDIT ).setEnabled ( false );
                owtbar_control.getButton ( JSimpleAssetToolbar.ButtonId.DELETE ).setEnabled ( false );
            }
        } 
    }
    
    @Override
    public void receiveLittleEvent ( LittleEvent evt_little ) {
        super.receiveLittleEvent ( evt_little );
        if ( evt_little instanceof CreateRequestEvent ) {
            createNewGroup ( ((CreateRequestEvent) evt_little).getAssetModel () );
        } else if ( evt_little instanceof EditRequestEvent ) {
            editSelectedGroup ();
        } else if ( evt_little instanceof DeleteRequestEvent ) {
            deleteSelectedGroup ();
        }
    }
    
    /**
     * Internal handler for CreateRequestEvent
     */
    private void createNewGroup ( AssetModel amodel_view ) {
        LittleGroup           group_new = SecurityAssetType.GROUP.create ();
        AssetModel            amodel_group = olib_asset.syncAsset ( group_new );
        
        CreateAssetWizard     wizard_create = new CreateAssetWizard ( om_asset, om_search, olib_asset,
                                                              olib_icon, ofactory_view,
                                                              Collections.singletonList ( (AssetType) SecurityAssetType.GROUP ),
                                                              amodel_group
                                                         );
        /**
         * Setup the local editor asset, so that all the changes
         * are visible after the post-edit resync with the AssetModelLibrary.
         */
        wizard_create.getLocalAsset ().setName ( "new_group" );
        wizard_create.getLocalAsset ().setHomeId ( amodel_view.getAsset ().getHomeId () );
        wizard_create.getLocalAsset ().setFromId ( amodel_view.getAsset ().getObjectId () );
        wizard_create.getLocalAsset ().setAclId ( amodel_view.getAsset ().getAclId () );
        
        if ( Wizard.FINISH_RETURN_CODE == wizard_create.showModalDialog () ) {
            try {
                olib_asset.syncAsset ( om_asset.saveAsset ( wizard_create.getLocalAsset (), "New group via GroupFolderTool" ) );
                // Force view to update
                editGroup ( amodel_group );
            } catch ( Exception e ) {
                olog_generic.log ( Level.INFO, "Caught unexpected: " + e  + ", " +
                                   BaseException.getStackTrace ( e )
                                   );
                JOptionPane.showMessageDialog( null, "Failed to save new group " + group_new.getName () +
                                               ", caught: " + e,
                                               "alert", JOptionPane.ERROR_MESSAGE
                                               );
                olib_asset.remove ( group_new.getObjectId () );
            }
        } else {
            olib_asset.remove ( group_new.getObjectId () );
        }
    }
    
    
    /**
     * Internal utility sets up edit UI for currently selected group
     */
    private void editSelectedGroup () {
        JGroupsUnderParentView  wgroups_view = (JGroupsUnderParentView) owtbar_control.getConnectedView ();
        LittleGroup             group_selected = wgroups_view.getSelectedGroup ();
        if ( null == group_selected ) {
            return;
        }
        AssetModel amodel_group = olib_asset.syncAsset ( group_selected );
        editGroup ( amodel_group );
    }
    
    /**
     * Internal utility setup UI controls to edit the given group 
     *
     * @param amodel_group AssetModel referencing a group to edit
     */
    private void editGroup ( AssetModel amodel_group ) {
        if ( ! (amodel_group.getAsset () instanceof LittleGroup) ) {
            return;
        }
        try {
            JGroupEditor  wedit_group = new JGroupEditor ( amodel_group, om_asset, om_search, olib_icon, ofactory_view );
            // TODO: stick this in a JDialog
            JDialog       wdial_edit = new JDialog ();
            wdial_edit.setTitle ( "Edit " + amodel_group.getAsset ().getName () );
            wdial_edit.setModal ( true );
            wdial_edit.add ( wedit_group );
            wdial_edit.pack ();
            wdial_edit.setVisible ( true );
        } catch ( Exception e ) {
            JOptionPane.showMessageDialog( null, "Request to edit group: " + amodel_group.getAsset ().getName () + 
                                           " caught unexpected: " + e,
                                          "alert", JOptionPane.ERROR_MESSAGE
                                          );
        }
    }
    
    /**
     * Internal handler for DeleteRequestEvent
     */
    private void deleteSelectedGroup () {
        // TODO - take a delete comment from the user ...
        //String 
        JGroupsUnderParentView  wgroups_view = (JGroupsUnderParentView) owtbar_control.getConnectedView ();
        LittleGroup             group_selected = wgroups_view.getSelectedGroup ();
        if ( null == group_selected ) {
            return;
        }
        String s_reason = JOptionPane.showInputDialog ( null,
                                                        "Enter a reason for the delete, or Cancel",
                                                        "GroupFolderTool interactive delete"
                                                        );
        
        if ( null != s_reason ) {
            Cursor  cursor_start = owtbar_control.getRootPane ().getCursor ();
            try {
                owtbar_control.getRootPane ().setCursor( new Cursor ( Cursor.WAIT_CURSOR ) );
                om_asset.deleteAsset ( group_selected.getObjectId (), s_reason );
                olib_asset.assetDeleted ( group_selected.getObjectId () );
            } catch ( Exception e ) {
                JOptionPane.showConfirmDialog ( null, 
                                                          "Delete failed, caught: " + e,
                                                          "Delete Error",
                                                          JOptionPane.OK_OPTION,
                                                          JOptionPane.ERROR_MESSAGE
                                                );
                     
            } finally {
                owtbar_control.getRootPane ().setCursor( cursor_start );
            }
        }
    }
    
}

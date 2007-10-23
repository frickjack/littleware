package littleware.apps.swingclient;

import java.awt.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;
import javax.swing.*;
import javax.swing.undo.*;
import javax.swing.event.*;


import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.NoSuchThingException;
import littleware.security.*;


/**
 * AssetEditor customized for editing group membership.
 */
public class JGroupEditor extends JGenericAssetEditor implements AssetEditor {
    private static final Logger       olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JGroupEditor" );

    private final AssetSearchManager        om_search;
    private final AssetManager              om_asset;
    private final IconLibrary               olib_icon;
    
    private final JAssetLink                owlink_group;
    // Remove member controls
    private final DefaultListModel          omodel_memberlist = new DefaultListModel ();
    private final JList                     owlist_members = new JList ( omodel_memberlist );
    private final JButton                   owbutton_delete = new JButton( "Remove Selected" );
    {
        owbutton_delete.addActionListener(
                                       new ActionListener () {
                                           public void actionPerformed(ActionEvent e) {
                                               uiDeleteMembers ();
                                           }
                                       }										 
                                       );
    }
    
    
    // Add member controls
    private final JTextField                owtext_add = new JTextField ( 40 );
    //private final JComboBox                 owcombo_ptype; // principal type
    private final JButton                   owbutton_add    = new JButton ( "Add" );
    

    {
            /*...
        AssetType[] v_ptype = {
            SecurityAssetType.USER,
            SecurityAssetType.GROUP
        };
        
        owcombo_ptype = new JComboBox ( v_ptype );
    ..*/
        
        owbutton_add.addActionListener(
                                  new ActionListener () {
                                      public void actionPerformed(ActionEvent e) {
                                          uiAddNewMember ();
                                      }
                                  }										 
                                  );
    }

    
    //----------------------

    /**
     * Add the principal specified in the UI controls
     * to the local copy of the group asset - 
     * popup dialog on error
     */
    private void uiAddNewMember () {
        try {
            AssetType         n_type = SecurityAssetType.PRINCIPAL; //(AssetType) owcombo_ptype.getSelectedItem ();
            String            s_name = owtext_add.getText ();
            LittlePrincipal   p_new = (LittlePrincipal) om_search.getByName ( s_name, n_type );
            
            if ( null == p_new ) {
                throw new NoSuchThingException ( "No " + n_type + " asset with name " + s_name + " in repository" );
            }
            LittleGroup       group_local = (LittleGroup) this.getLocalAsset ();
            if ( group_local.addMember ( p_new ) ) {
                omodel_memberlist.addElement ( p_new );
                this.setHasLocalChanges ( true );
            }
        } catch ( Exception e ) {
            olog_generic.log ( Level.INFO, "Failed adding new member, caught: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
            JOptionPane.showMessageDialog( null, "Add member failed, caught: " + e, "Add failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    /**
     * Delete the group members selected by the UI from the local group copy.
     * Popup dialog on error.
     */
    private void uiDeleteMembers () {
        try {
            LittleGroup       group_local = (LittleGroup) this.getLocalAsset ();
            Object[] v_selected = owlist_members.getSelectedValues ();
            
            for ( Object x_selected : v_selected ) {
                group_local.removeMember ( (LittlePrincipal) x_selected );
                omodel_memberlist.removeElement ( x_selected );
                this.setHasLocalChanges ( true );
            }
        } catch ( Exception e ) {
            olog_generic.log ( Level.INFO, "Failed removing member, caught: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
            JOptionPane.showMessageDialog( null, "Remove member failed, caught: " + e, "Remove failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    /**
     * Save the local copy of the group.
     * Popup dialog on error.
     */
    private void uiSaveLocalChanges () {
        try {
            saveLocalChanges ( om_asset, "JGroupEditor save" );
        } catch ( Exception e ) {
            olog_generic.log ( Level.INFO, "Failed save, caught: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
            JOptionPane.showMessageDialog( null, "Save failed, caught: " + e, "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //----------------------
    private boolean                         ob_built = false;
    private final JPanel                    owpanel_build = new JPanel ( new GridBagLayout () );
    /**
     * Add a group-edit tab to the underlying editor
     */
    private void buildTab () {
        if ( ob_built ) {
            olog_generic.log ( Level.WARNING, "Ignoring second call to single-call method" );
            return;
        }
        
        owpanel_build.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
		owpanel_build.setLayout ( new GridBagLayout () );
                
        GridBagConstraints grid_control = new GridBagConstraints();
        
        //grid_control.anchor = GridBagConstraints.FIRST_LINE_START;
        grid_control.gridwidth = GridBagConstraints.REMAINDER;
        grid_control.gridheight = 1;
        grid_control.ipadx = 1;
        grid_control.ipady = 1;
        grid_control.insets = new Insets ( 2,2,2,2 );
        
        { // Asset type and icon
          //grid_control.gridwidth = GridBagConstraints.REMAINDER;
            grid_control.gridx = 0;
            grid_control.gridy = 0;
            
            owpanel_build.add ( owlink_group, grid_control );
            grid_control.gridy += grid_control.gridheight;

            owpanel_build.add ( new JLabel ( "Group members:" ),
                       grid_control 
                       );
            grid_control.gridy += grid_control.gridheight;
            grid_control.weighty = 0.9;
            JScrollPane wscroll_list = new JScrollPane( owlist_members ); 
            
            // Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            grid_control.gridheight = 1;
            //wscroll_list.setPreferredSize( new Dimension(100, 200) );
            grid_control.fill = GridBagConstraints.BOTH;
            owpanel_build.add ( wscroll_list,
                       grid_control 
                               );            
            grid_control.weighty = 0.0;
            grid_control.gridy += grid_control.gridheight;
            grid_control.gridheight = 1;
            grid_control.fill = GridBagConstraints.NONE;
        }            
        {
            grid_control.gridwidth = 1;
            owpanel_build.add ( owbutton_delete, grid_control );
            grid_control.gridy += grid_control.gridheight;
        }
        {
            grid_control.gridx = 0;
            grid_control.gridwidth = GridBagConstraints.REMAINDER;
            owpanel_build.add ( new JLabel ( "Enter name of user/group to add, and click the Add button" ), grid_control );
            grid_control.gridwidth = 1;
            grid_control.gridy += grid_control.gridheight;
            
            grid_control.weightx = 0.9;
            grid_control.fill = GridBagConstraints.HORIZONTAL;
            owpanel_build.add ( owtext_add, grid_control );
            grid_control.weightx = 0.0;
            grid_control.fill = GridBagConstraints.NONE;
            grid_control.gridx += grid_control.gridwidth;
            //owpanel_build.add ( owcombo_ptype, grid_control );
            //grid_control.gridx += grid_control.gridwidth;
            owpanel_build.add ( owbutton_add, grid_control );
            grid_control.gridx = 0;
            grid_control.gridy += grid_control.gridheight;
        }
        
        Icon icon_group = olib_icon.lookupIcon ( SecurityAssetType.GROUP );                
        insertTab ( "Edit Group", icon_group, owpanel_build, "Edit Group", -1 );
        ob_built = true;
    }
    
    
    
    /**
     * Constructor initalizes the editor
     *
     * @param model_view to initally view - should be a GROUP type asset
     * @param m_asset AssetManager to save changes with
     * @param m_search to lookup support data with
     * @param lib_icon source of icons
     * @param view_factory read-only view source for browsers
     */
    public JGroupEditor ( AssetModel model_view, 
                          AssetManager m_asset,
                          AssetSearchManager m_search,
                          IconLibrary lib_icon,
                          AssetViewFactory view_factory
                          ) {
        super( model_view, m_asset, m_search, lib_icon, view_factory );
        om_search = m_search;
        om_asset = m_asset;
        olib_icon = lib_icon;
        owlist_members.setCellRenderer ( new JAssetLink( lib_icon ) );
        owlink_group = new JAssetLink ( lib_icon );
        buildTab ();
        updateTab ();
    }
    
 
    /**
     * Reset the entire UI to match the data in getLocalAsset().
     * Disables GROUP tab if underlying AssetModel is not a Group.
     */
    protected void updateAssetUI () {
        super.updateAssetUI ();
        updateTab ();
    }

    /**
     * Internal utility to update the group-tab UI
     */
    private void updateTab () {
        if ( ! ob_built ) {  // superclass constructor up-call before initialization
            return;
        }
        // Model has changed under us
        omodel_memberlist.clear ();

        AssetModel  model_view = getAssetModel ();

        if ( (null == model_view) 
             || (! (model_view.getAsset ().getAssetType ().equals ( SecurityAssetType.GROUP ) ))
             ) {
            olog_generic.log ( Level.FINE, "Non-group model" );
            setTabEnabled ( owpanel_build, false );
            return;
        }
        setTabEnabled ( owpanel_build, true );
        LittleGroup      group_view = (LittleGroup) model_view.getAsset ();
        java.util.List<LittlePrincipal>  v_members = new ArrayList<LittlePrincipal> ();
        
        owlink_group.setLink ( group_view );
        for ( Enumeration<? extends Principal> enum_members = group_view.members ();
              enum_members.hasMoreElements ();
              ) {
            v_members.add ( (LittlePrincipal) enum_members.nextElement () );
        }
        Collections.sort ( v_members, 
                           new Comparator<LittlePrincipal> () {
                               public int compare ( LittlePrincipal p_1, LittlePrincipal p_2 ) {
                                   return p_1.getName ().compareTo ( p_2.getName () );
                               }
                           }
                           );
        for ( LittlePrincipal p_member : v_members ) {
            olog_generic.log ( Level.FINE, "adding group member: " + p_member.getName () );
            omodel_memberlist.addElement ( p_member );
        } 
        setHasLocalChanges ( false );
    }
    
}


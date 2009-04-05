/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.swingclient;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.awt.GridBagLayout;
import java.awt.GridBagConstraints;
import java.awt.Insets;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.security.acl.AclEntry;
import java.security.acl.Permission;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import javax.swing.*;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.NoSuchThingException;
import littleware.security.*;


/**
 * AssetEditor customized for editing ACL membership.
 * Very similar to group editor, but just add a combo box that switches
 * the view between which groups/users are granted a particular permission.
 */
public class JAclEditor extends JGenericAssetEditor implements AssetEditor {
    private static final Logger       olog_generic = Logger.getLogger ( JAclEditor.class.getName() );
    private static final long serialVersionUID = -2200951092337918748L;

    private final AssetSearchManager        om_search;
    private final AssetManager              om_asset;
    private final IconLibrary               olib_icon;
    
    private final JAssetLink                    owlink_acl;
    // Put views of different permissions in separate tabs of a tabbed pane
    private final JAclTabbedPane                owtab_perms;
    
    /**
     * Internal utility class to help manage the different tab views 
     * of an ACL
     */
    private static class JAclTabbedPane extends JTabbedPane {
        private static final long serialVersionUID = -433335749775998619L;
        private final Map<LittlePermission, JList>      omap_perms = new HashMap<LittlePermission, JList> ();
        private final java.util.List<LittlePermission>  ov_perms;
        
        /**
         * Inject dependency on IconLibrary
         */
        public JAclTabbedPane ( JAssetLinkRenderer render_cell ) {
            java.util.List<LittlePermission> v_perms = new ArrayList<LittlePermission> ();
            v_perms.add ( LittlePermission.READ );
            v_perms.add ( LittlePermission.WRITE );
            
            for ( LittlePermission perm_other: LittlePermission.getMembers () ) {
                if ( (! perm_other.equals ( LittlePermission.READ ))
                     && (! perm_other.equals ( LittlePermission.WRITE ))
                     ) {
                    v_perms.add ( perm_other );
                }
            }

            for ( LittlePermission perm_tab : v_perms ) {
                DefaultListModel  model_memberlist = new DefaultListModel ();
                JList             wlist_members = new JList( model_memberlist );
                wlist_members.setCellRenderer ( render_cell );
                omap_perms.put ( perm_tab, wlist_members );
                this.addTab( perm_tab.toString (), wlist_members );
            }
            ov_perms = Collections.unmodifiableList ( v_perms );
        }
    
        /**
         * Little utility to get the Permission that the user is currently viewing
         */
        public LittlePermission getSelectedPermission () {
            return ov_perms.get ( this.getSelectedIndex () );
        }
        /**
         * Clear all the users out of the permission map
         */
        public void clearAll () {
            for ( JList wlist_perm : omap_perms.values () ) {
                ((DefaultListModel) wlist_perm.getModel ()).clear ();
            }
        }
        /**
         * Get the Principals selected in the given permission's tab
         *
         * @param perm_get to get selected members for
         */
        public java.util.List<LittlePrincipal> getSelectedPrincipal ( LittlePermission perm_get ) {
            final java.util.List<LittlePrincipal> v_result = new ArrayList<LittlePrincipal> ();
            Object[] v_selected = omap_perms.get ( perm_get ).getSelectedValues ();
            for ( Object x: v_selected ) {
                v_result.add ( (LittlePrincipal) x );
            }
            return v_result;
        }
        /**
         * Get the ListModel of LittlePrincipals underlying the view
         * for the given permission
         *
         * @param perm_get to get model for
         */
        public DefaultListModel getListModel ( LittlePermission perm_get ) {
            return (DefaultListModel) omap_perms.get ( perm_get ).getModel ();
        }
    }
        
        
    
    /**
     * Little utility to get the JList
     */
    
    private final JButton                   owbutton_delete = new JButton( "Remove Selected" );
    {
        owbutton_delete.addActionListener(
                                          new ActionListener () {
            @Override
                                              public void actionPerformed(ActionEvent e) {
                                                  uiDeleteMembers ();
                                              }
                                          }										 
                                          );
    }
    
    
    // Add member controls
    private final JTextField                owtext_add = new JTextField ( 40 );
    private final JComboBox                 owcombo_ptype; // principal type
    private final JButton                   owbutton_add    = new JButton ( "Add" );
    {
        AssetType[] v_ptype = {
            SecurityAssetType.USER,
            SecurityAssetType.GROUP
        };
        owcombo_ptype = new JComboBox ( v_ptype );
        
        owbutton_add.addActionListener(
                                       new ActionListener () {
            @Override
                                           public void actionPerformed(ActionEvent e) {
                                               uiAddNewMember ();
                                           }
                                       }										 
                                       );
    }
    
    
    //----------------------
    
    
    /**
     * Add the principal specified in the UI controls
     * to the local copy of the acl asset - 
     * popup dialog on error
     */
    private void uiAddNewMember () {
        try {
            AssetType         n_type = (AssetType) owcombo_ptype.getSelectedItem ();
            String            s_name = owtext_add.getText ();
            LittlePrincipal   p_new = (LittlePrincipal) om_search.getByName ( s_name, n_type );
            
            if ( null == p_new ) {
                throw new NoSuchThingException ( "No " + n_type + " asset with name " + s_name + " in repository" );
            }
            LittleAcl         acl_local = (LittleAcl) this.getLocalAsset ();
            AssetModelLibrary lib_asset = getAssetModel ().getLibrary ();
            LittlePermission  perm_selected = owtab_perms.getSelectedPermission ();
            
            lib_asset.syncAsset ( p_new );
            LittleAclEntry    acle_new = acl_local.getEntry ( p_new, false );
            if ( null == acle_new ) {
                acle_new = SecurityAssetType.ACL_ENTRY.create ();
                acle_new.setPrincipal ( p_new );
                acl_local.addEntry ( acle_new );
            }
            if ( acle_new.addPermission ( perm_selected ) ) {
                DefaultListModel model_memberlist = owtab_perms.getListModel ( perm_selected );
                model_memberlist.addElement ( p_new );
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
     * Delete the acl-entry members selected by the UI from the local acl copy.
     * Popup dialog on error.
     */
    private void uiDeleteMembers () {
        try {
            LittleAcl             acl_local = (LittleAcl) this.getLocalAsset ();
            LittlePermission      perm_selected = owtab_perms.getSelectedPermission ();
            List<LittlePrincipal> v_selected = owtab_perms.getSelectedPrincipal ( perm_selected );
            
            DefaultListModel model_memberlist = owtab_perms.getListModel ( perm_selected );
            for ( LittlePrincipal p_selected : v_selected ) {
                LittleAclEntry  acle_selected = acl_local.getEntry ( p_selected, false );
                if ( (null != acle_selected) 
                     && acle_selected.removePermission( perm_selected ) 
                     ) {
                    if ( ! acle_selected.permissions ().hasMoreElements () ) {
                        acl_local.removeEntry ( acle_selected );
                    }
                    model_memberlist.removeElement ( p_selected );
                    this.setHasLocalChanges ( true );
                }
            }
        } catch ( Exception e ) {
            olog_generic.log ( Level.INFO, "Failed removing member, caught: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
            JOptionPane.showMessageDialog( null, "Remove member failed, caught: " + e, "Remove failed", JOptionPane.ERROR_MESSAGE);
        }
        
    }
    
    /**
     * Save the local copy of the acl
     * Popup dialog on error.
     */
    private void uiSaveLocalChanges () {
        try {
            saveLocalChanges ( om_asset, "JAclEditor save" );
        } catch ( Exception e ) {
            olog_generic.log ( Level.INFO, "Failed save, caught: " + e +
                               ", " + BaseException.getStackTrace ( e )
                               );
            JOptionPane.showMessageDialog( null, "Save failed, caught: " + e, "Save failed", JOptionPane.ERROR_MESSAGE);
        }
    }
    
    //----------------------
    private boolean                ob_built = false;
    private final JPanel           owpanel_build = new JPanel ( new GridBagLayout () );        

    /**
     * Add a tab to the underlying generic editor
     */
    private void addAclTab () {
        if ( ob_built ) {
            olog_generic.log ( Level.WARNING, "Ignoring second call to single-call method" );
            return;
        }
        
        owpanel_build.setBorder( BorderFactory.createEmptyBorder(30, 30, 10, 30) );
        owpanel_build.setLayout ( new GridBagLayout () );
        
        GridBagConstraints grid_control = new GridBagConstraints();
        
        //grid_control.anchor = GridBagConstraints.FIRST_LINE_START;
        grid_control.gridwidth = 1;
        grid_control.gridheight = 1;
        grid_control.ipadx = 1;
        grid_control.ipady = 1;
        grid_control.insets = new Insets ( 2,2,2,2 );
        grid_control.fill = GridBagConstraints.NONE;
        
        { // Asset type and icon
            grid_control.gridx = 0;
            grid_control.gridy = 0;
            
            owpanel_build.add ( owlink_acl, grid_control );
            grid_control.gridy += grid_control.gridheight;
            
            owpanel_build.add ( new JLabel ( "Entries by permission:" ),
                               grid_control 
                               );
            grid_control.gridy += grid_control.gridheight;
            grid_control.weighty = 0.9;
            // Dimension screenDim = Toolkit.getDefaultToolkit().getScreenSize();
            grid_control.gridheight = 4;
            grid_control.gridwidth = 3;
            grid_control.fill = GridBagConstraints.BOTH;            
            owpanel_build.add ( owtab_perms,
                               grid_control 
                               );            
            grid_control.weighty = 0.0;
            grid_control.gridy += grid_control.gridheight;
            grid_control.gridheight = 1;
            grid_control.gridwidth = 1;            
            grid_control.fill = GridBagConstraints.NONE;            
        }            
        {
            owpanel_build.add ( owbutton_delete, grid_control );
            grid_control.gridy += grid_control.gridheight;
        }
        {
            grid_control.gridx = 0;
            grid_control.gridwidth = 3;
            owpanel_build.add ( new JLabel ( "Enter name of user/group to add, and click the Add button" ), grid_control );
            grid_control.gridwidth = 1;
            grid_control.gridy += grid_control.gridheight;
            
            grid_control.weightx = 0.9;
            grid_control.fill = GridBagConstraints.HORIZONTAL;            
            owpanel_build.add ( owtext_add, grid_control );
            grid_control.fill = GridBagConstraints.NONE;            
            grid_control.weightx = 0.0;
            grid_control.gridx += grid_control.gridwidth;
            owpanel_build.add ( owcombo_ptype, grid_control );
            grid_control.gridx += grid_control.gridwidth;
            owpanel_build.add ( owbutton_add, grid_control );
            grid_control.gridx = 0;
            grid_control.gridy += grid_control.gridheight;
        }
        
        Icon icon_acl = olib_icon.lookupIcon ( SecurityAssetType.ACL );                
        insertTab ( "Edit Acl", icon_acl, owpanel_build, "Edit Acl", -1 );
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
    @Inject
    public JAclEditor ( AssetModel model_view, 
                          AssetManager m_asset,
                          AssetSearchManager m_search,
                          JAssetLinkRenderer renderLink,
                        AssetViewFactory factory_view,
                        JAssetLink  jLinkAcl,
                        IconLibrary lib_icon,
                        Provider<JAssetLink>  provideLinkView,
                        Provider<JAssetLinkEditor> provideLinkEditor
                        ) {
        super( model_view, m_asset, m_search, lib_icon, factory_view,
                provideLinkView, provideLinkEditor
                );
        om_search = m_search;
        om_asset = m_asset;
        olib_icon = lib_icon;
        owtab_perms = new JAclTabbedPane( renderLink );
        owlink_acl = jLinkAcl;
        addAclTab ();
        updateTab ();
    }
    
    
    
    
    /**
     * Clear and reset the UI view when the data model changes or
     * the selected permission-type changes
     */
    @Override
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
        owtab_perms.clearAll ();
        AssetModel  amodel_view = getAssetModel ();
        
        if ( (null == amodel_view) 
             || (! (amodel_view.getAsset ().getAssetType ().equals ( SecurityAssetType.ACL ) ))
             ) {
            olog_generic.log ( Level.FINE, "Non-acl model" );
            return;
        }

        LittleAcl              acl_view = (LittleAcl) getLocalAsset ();
        owlink_acl.setLink ( acl_view );
        
        Enumeration<AclEntry>  enum_entry = acl_view.entries ();
        java.util.Map<LittlePermission,List<LittlePrincipal>>  map_members = 
            new HashMap<LittlePermission,List<LittlePrincipal>> ();
        
        for ( LittlePermission perm_other: LittlePermission.getMembers () ) {
            map_members.put ( perm_other, new ArrayList<LittlePrincipal> () );
        }
        
        // Scan the ACL-entries 
        while ( enum_entry.hasMoreElements () ) {
            LittleAclEntry acle_check = (LittleAclEntry) enum_entry.nextElement ();
            Enumeration<Permission> enum_perm = acle_check.permissions ();
            while ( enum_perm.hasMoreElements () ) {
                map_members.get ( (LittlePermission) enum_perm.nextElement () ).add ( acle_check.getPrincipal () );
            }
        }

        Comparator<LittlePrincipal> compare_sort = new Comparator<LittlePrincipal> () {
            public int compare ( LittlePrincipal p_1, LittlePrincipal p_2 ) {
                return p_1.getName ().compareTo ( p_2.getName () );
            }
        };
        
        for ( Map.Entry<LittlePermission, List<LittlePrincipal>> entry : map_members.entrySet () ) {
            List<LittlePrincipal> v_members = entry.getValue ();
            
            if ( v_members.size () > 1 ) {
                Collections.sort ( v_members, compare_sort );
            }
            DefaultListModel model_memberlist = owtab_perms.getListModel ( entry.getKey () );
            for ( LittlePrincipal p_member : v_members ) {
                model_memberlist.addElement ( p_member );
            } 
        }
        setHasLocalChanges ( false );
    }
    
}


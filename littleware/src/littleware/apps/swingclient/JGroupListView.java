package littleware.apps.swingclient;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;
import java.security.acl.Group;

import javax.swing.*;
import javax.swing.tree.*;
import javax.swing.event.*;


import littleware.asset.*;
import littleware.base.BaseException;
import littleware.base.NoSuchThingException;
import littleware.base.Whatever;
import littleware.security.*;


/**
 * Simple view of a list of groups and their
 * members.  Take a look at {@link littleware.apps.controller.JGroupListController}
 * for a standard wire-up.
 * Fires a PropertyChange event to listeners when the selection changes.
 * Note: this view only listens on changes to the ListModel, not
 * for property-changes on the AssetModel associated with each list-member.
 */
public class JGroupListView extends JPanel {
    private final static Logger                  olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JGroupListView" );
    
    private final PropertyChangeSupport          opropsupport = new PropertyChangeSupport ( this );
    private final IconLibrary                    olib_icon;
    // List tracks mapping of group-list members to tree nodes
    private final List<DefaultMutableTreeNode>   ov_nodes = new ArrayList<DefaultMutableTreeNode> ();
    private final MutableTreeNode                otnode_root = new DefaultMutableTreeNode ();
    private final DefaultTreeModel               otmodel_groups = new DefaultTreeModel ( otnode_root );
    private final JTree                          owtree_groups = new JTree ( otmodel_groups );
    private final ListModel                      olmodel_groups;
    private       LittleGroup                    ogroup_selected = null;
    
    /**
     * The property names that PropertyChangeListeners can watch for
     */
    public enum Property {
        selectedGroup
    };
    
    /**
     * Utility clears the children out of the given MutableTreeNode,
     * and syncs them with the members of the Group that the treenode
     * is associated with (getUserObject) sorted alphabetically.
     */
    private void syncGroupNode ( DefaultMutableTreeNode tnode_group ) {
        Group           grp_2sync = (Group) tnode_group.getUserObject ();
        
        for ( int j=0; j < tnode_group.getChildCount (); ++j ) {
            tnode_group.remove ( j );
        }

        List<? extends Principal> v_members = Collections.list ( grp_2sync.members () );
        Collections.sort ( v_members, 
                           new Comparator<Principal> () {
            public int compare( Principal p_1, Principal p_2 ) {
                return p_1.getName ().compareTo ( p_2.getName () );
            }
        }
                           );
        int i_child = 0;
        for ( Principal p_child : v_members ) {
            MutableTreeNode tnode_child = new DefaultMutableTreeNode ();
            tnode_child.setUserObject ( p_child );
            tnode_group.insert ( tnode_child, i_child );
            ++i_child;
        }            
    }

    /**
     * Little internal controller maps changes to our exported ListModel
     * to the TreeModel backing the JTree we're using.
     */
    private final ListDataListener        olisten_model = new ListDataListener () {
        /**
         * Reset the tree-nodes corresponding to the group list members changed
         */
        public void	contentsChanged( ListDataEvent evt_list ) {
            for ( int i=evt_list.getIndex0 ();
                  i <= evt_list.getIndex1 ();
                  ++i
                  ) {
                MutableTreeNode tnode_group = ov_nodes.get ( i );
                Group group_changed = (Group) olmodel_groups.getElementAt ( i );
                tnode_group.setUserObject ( group_changed );
                syncGroupNode ( (DefaultMutableTreeNode) tnode_group );
                
                otmodel_groups.nodeChanged ( tnode_group );
                otmodel_groups.nodeStructureChanged ( tnode_group );
            }
        }
            
        /**
         * Insert the new groups in the list into the view tree
         */
        public void	intervalAdded(ListDataEvent evt_list ) {
            for ( int i=evt_list.getIndex0 ();
                  i <= evt_list.getIndex1 ();
                  ++i
                  ) {
                DefaultMutableTreeNode tnode_group = new DefaultMutableTreeNode ();
                Group group_changed = (Group) olmodel_groups.getElementAt ( i );
                olog_generic.log ( Level.FINE, "Adding group to display tree model: " + group_changed.getName () );
                tnode_group.setUserObject ( group_changed );
                
                otnode_root.insert ( tnode_group, i );
                ov_nodes.add ( i, tnode_group );
                syncGroupNode ( tnode_group );
            }
            /*....
            int[] v_members = new int[ evt_list.getIndex1 () - evt_list.getIndex0 () + 1 ];
            for ( int i_member = 0; i_member < v_members.length; ++i_member ) {
                v_members[ i_member ] = evt_list.getIndex0 () + i_member;
            }
                        
            otmodel_groups.nodesWereInserted ( otnode_root, v_members );
            ..*/
            otmodel_groups.nodeStructureChanged ( otnode_root );
        }            
        
        public void	intervalRemoved(ListDataEvent evt_list ) {
            //Object[] v_removed_groups = new Object[ evt_list.getIndex1 () - evt_list.getIndex0 () + 1 ];
            
            for ( int i=evt_list.getIndex1 ();
                  i >= evt_list.getIndex0 ();
                  --i
                  ) {
                DefaultMutableTreeNode tnode_group = ov_nodes.remove ( i );
                otnode_root.remove ( i );
                
                Asset a_removed = (Asset) tnode_group.getUserObject ();
                Asset a_selected = getSelectedGroup ();
                if ( Whatever.equalsSafeNotNull ( a_removed, a_selected ) ) {
                    setSelectedGroup ( null );
                }
            }
            
            /*..
            int[] v_members = new int[ evt_list.getIndex1 () - evt_list.getIndex0 () + 1 ];
            for ( int i_member = 0; i_member < v_members.length; ++i_member ) {
                v_members[ i_member ] = evt_list.getIndex0 () + i_member;
            }
            otmodel_groups.nodesWereRemoved ( otnode_root, v_members, v_removed_groups );
            ..*/
            otmodel_groups.nodeStructureChanged ( otnode_root );
        }            
        
    };
    
    private final TreeSelectionListener olisten_selection = new TreeSelectionListener () {
        public void valueChanged(TreeSelectionEvent e) {
            DefaultMutableTreeNode tnode_asset = (DefaultMutableTreeNode)
                           owtree_groups.getLastSelectedPathComponent();
            
            LittleGroup group_selected = null;
            
            if ( (null != tnode_asset) 
                 && (otnode_root.getIndex ( tnode_asset ) >= 0)
                 ) {
                Asset a_selected = (Asset) tnode_asset.getUserObject();
                if ( (null != a_selected) 
                     && a_selected.getAssetType ().equals ( SecurityAssetType.GROUP ) ) {
                    group_selected = (LittleGroup) a_selected;
                }
            }
            setSelectedGroup ( group_selected );
        }        
    };
    
    {
        owtree_groups.getSelectionModel().setSelectionMode( TreeSelectionModel.SINGLE_TREE_SELECTION );
        owtree_groups.getSelectionModel ().addTreeSelectionListener ( olisten_selection );
    }    
    
    /**
     * Internal selected-group property setter.
     * Fires PropertyChangeEvent.
     *
     * @param group_selected may be null, otherwise must be Group in the Model
     */
    private void setSelectedGroup ( LittleGroup group_selected ) {
        if ( ! Whatever.equalsSafe ( group_selected, ogroup_selected ) ) {
            LittleGroup  group_old = ogroup_selected;
            ogroup_selected = group_selected;
            opropsupport.firePropertyChange ( Property.selectedGroup.toString (), 
                                            group_old, group_selected
                                            );
        }
    }
    
    /** Get the currently selected Group, or null if none selected */
    public LittleGroup getSelectedGroup () {
        return ogroup_selected;
    }

    
    /**
     * Inject the view with a given model and dependencies.
     *
     * @param lib_icon source of icons
     * @param lmodel_grouplist DataModel underlying the view
     */
    public JGroupListView ( IconLibrary lib_icon, ListModel lmodel_grouplist ) {
        olib_icon = lib_icon;
        olmodel_groups = lmodel_grouplist;
        owtree_groups.setCellRenderer ( new JAssetLink ( lib_icon, true ) );
        
        olmodel_groups.addListDataListener ( olisten_model );
        if ( olmodel_groups.getSize () > 0 ) {
            olisten_model.intervalAdded ( new ListDataEvent ( olmodel_groups,
                                                              ListDataEvent.INTERVAL_ADDED,
                                                              0, olmodel_groups.getSize () - 1
                                                              )
                                          );
        }        
        
        this.add (
                   new JScrollPane( owtree_groups,  JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED,
                                   JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED )
                  );
    }
    

    
    /**
     * Initialize the view with a DefaultListModel and the groups in the given collection
     *
     * @param lib_icon source of icons
     * @param v_groups to initialize view to
     */
    public JGroupListView ( IconLibrary lib_icon, List<Group> v_groups ) {
        this( lib_icon, new DefaultListModel () );
        
        int i_position = 0;
        for ( Group group_add : v_groups ) {
            ((DefaultListModel) olmodel_groups).addElement ( group_add );
        }
    }
    
    /**
     * Provide a hook to let subtypes set the Asset that
     * the members of the list are rooted at
     *
     * @param a_root asset - may be null
     */
    public void setRoot ( Asset a_root ) {
        otnode_root.setUserObject ( a_root );
        otmodel_groups.nodeChanged ( otnode_root );
    }
    
    /**
     * Hook to data model controls can listen on
     * to pickup changes to the data displayed by this view.
     */
    public ListModel  getModel () { 
        return olmodel_groups;
    }
    
    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        opropsupport.addPropertyChangeListener ( listen_props );
    }
    
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        opropsupport.removePropertyChangeListener ( listen_props );
    }
    
}

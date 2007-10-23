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
import littleware.base.swing.ListModelIterator;
import littleware.security.*;


/**
 * Specialization of JGroupListView that implements a
 * view of the Groups and their members linking FROM the AssetView model.
 * The view adds each groups to the AssetModelLibrary, and listens for
 * property changes.
 */
public class JGroupsUnderParentView extends JGroupListView implements AssetView {
    private static final Logger         olog_generic = Logger.getLogger ( "littleware.apps.swingclient.JGroupsUnderParentView" );
    
    private final AssetModelLibrary     olib_asset;
    private final DefaultListModel      olmodel_groups;
    private final AssetSearchManager    om_search;
    
    private boolean                     ob_update_scheduled = false;
    private final AbstractAssetView     oaview_delegate = new AbstractAssetView ( this ) {
        /** Events form the data model */
        public void eventFromModel ( LittleEvent evt_from_model ) {
            olog_generic.log ( Level.FINE, "Got PropertyChangeEvent, update scheduled ?: " + ob_update_scheduled );
            if ( ! ob_update_scheduled ) {
                // Since we update everything anyway - avoid doing it multiple times
                ob_update_scheduled = true;  
                SwingUtilities.invokeLater ( new Runnable () {
                    public void run () {
                        try {
                            updateGrouplist ();
                        } finally {
                            ob_update_scheduled = false;
                        }
                    }
                }
                                             );
            }
        }
    };
    
    // Listen to the groups that are being displayed
    private final LittleListener  olisten_children = new LittleListener () {
        public void receiveLittleEvent ( LittleEvent event_groupmodel ) {
            AssetModel  amodel_source = (AssetModel) event_groupmodel.getSource ();
            LittleGroup group_source = (LittleGroup) amodel_source.getAsset ();
            // find the group in the list model
            final int i_size = olmodel_groups.getSize ();
            for ( int i_pos=0; i_pos < i_size; ++i_pos ) {
                if ( ((Asset) olmodel_groups.getElementAt( i_pos )).getObjectId ().equals ( group_source.getObjectId () ) ) {
                    olmodel_groups.set ( i_pos, group_source );
                    break;
                }
            }
            
        }
    };
    
    
    /**
     * Inject dependencies.  Non-subtype clients should
     * use the create() factory method.
     *
     * @param lib_asset asset-model library
     * @param lib_icon icon library
     * @param lmodel_groups data model listing visible groups maintained for super class
     * @param m_search asset repository simple search
     */
    private JGroupsUnderParentView ( AssetModelLibrary  lib_asset,
                                     IconLibrary        lib_icon,
                                     DefaultListModel   lmodel_groups,
                                     AssetSearchManager m_search
                                    ) {
        super ( lib_icon, lmodel_groups );
        olib_asset = lib_asset;
        om_search = m_search;
        olmodel_groups = lmodel_groups;
    }
    
    /**
     * Inject dependencies
     *
     * @param lib_asset asset-model library     
     * @param lib_icon icon library
     * @param m_search asset repository simple search
     */    
    public JGroupsUnderParentView ( AssetModelLibrary lib_asset,
                                    IconLibrary lib_icon,
                                    AssetSearchManager m_search ) {
        this ( lib_asset, lib_icon, new DefaultListModel (), m_search );
    }
    
    public void	addLittleListener( LittleListener listen_little ) {
		oaview_delegate.addLittleListener ( listen_little );
	}
	
	
	public void     removeLittleListener( LittleListener listen_little ) {
		oaview_delegate.removeLittleListener ( listen_little );
	}
    
    @Override
    public void addPropertyChangeListener( PropertyChangeListener listen_props ) {
        super.addPropertyChangeListener ( listen_props );
        oaview_delegate.addPropertyChangeListener ( listen_props );
    }
    
    @Override
    public void removePropertyChangeListener( PropertyChangeListener listen_props ) {
        super.removePropertyChangeListener ( listen_props );
        oaview_delegate.removePropertyChangeListener ( listen_props );
    }
    
    public AssetModel getAssetModel () {
        return oaview_delegate.getAssetModel ();
    }
    
    /**
     * Internal utility to update the DefaultListModel underlying the UI
     * when the Group being viewed is updated.
     */
    private void updateGrouplist () {
        Asset  a_root = getAssetModel ().getAsset ();
        
        for ( Iterator<Object> r_list = new ListModelIterator( olmodel_groups );
              r_list.hasNext ();
              )
        {
            AssetModel  amodel_group = olib_asset.get ( ((Asset) r_list.next ()).getObjectId () );
            if ( null != amodel_group ) {
                amodel_group.removeLittleListener ( olisten_children );
            }
        }
        olmodel_groups.clear ();
        
        try {  // /load the children
            List<Asset>  v_groups = new ArrayList<Asset> ();
            {
                Collection<UUID>    v_ids = new ArrayList<UUID> ();  // serializable collection
                v_ids.addAll ( om_search.getAssetIdsFrom ( a_root.getObjectId (), SecurityAssetType.GROUP ).values () );
                v_groups.addAll ( om_search.getAssets ( v_ids ) );
            }
            Collections.sort ( v_groups, new Comparator<Asset> () {
                public int compare( Asset a_1, Asset a_2 ) {
                    return a_1.getName ().compareTo ( a_2.getName () );
                }
            }
                               );
            
            for ( Asset a_group : v_groups ) {
                olog_generic.log ( Level.FINE, "Adding group to list model: " + a_group.getName () );
                // watch for new group members
                olib_asset.syncAsset ( a_group ).addLittleListener ( olisten_children );
                olmodel_groups.addElement ( a_group );
            }
        } catch ( Exception e ) {
            // Just log it for now
            olog_generic.log ( Level.WARNING, "Failed to resolve children under " + a_root + ", caught: " + e );
        }
        
    }
    
    /**
     * Reset the underlying JGroupListModel to the
     * GROUP children of the new model.
     */
    public void setAssetModel ( AssetModel model_asset ) {
        Asset a_root = model_asset.getAsset ();
        setRoot ( a_root );
        oaview_delegate.setAssetModel ( model_asset );
        updateGrouplist ();
    }
    
	
}

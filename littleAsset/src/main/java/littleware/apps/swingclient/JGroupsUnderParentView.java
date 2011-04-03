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

import littleware.base.feedback.LittleListener;
import littleware.base.feedback.LittleEvent;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import javax.swing.*;

import littleware.apps.client.*;
import littleware.asset.*;
import littleware.base.feedback.Feedback;
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
    private static final long serialVersionUID = 1251881511039167615L;
    
    private final AssetModelLibrary     olib_asset;
    private final DefaultListModel      olmodel_groups;
    private final AssetSearchManager    om_search;
    
    private boolean                     ob_update_scheduled = false;
    private final AbstractAssetView     oaview_delegate = new AbstractAssetView ( this ) {
        /** Events form the data model */
        @Override
        public void eventFromModel ( LittleEvent evt_from_model ) {
            olog_generic.log ( Level.FINE, "Got PropertyChangeEvent, update scheduled ?: " + ob_update_scheduled );
            if ( ! ob_update_scheduled ) {
                // Since we update everything anyway - avoid doing it multiple times
                ob_update_scheduled = true;  
                SwingUtilities.invokeLater ( new Runnable () {
                    @Override
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
        @Override
        public void receiveLittleEvent ( LittleEvent event_groupmodel ) {
            final AssetModel  amodel_source = (AssetModel) event_groupmodel.getSource ();
            final LittleGroup group_source = amodel_source.getAsset ().narrow( LittleGroup.class );
            // find the group in the list model
            final int i_size = olmodel_groups.getSize ();
            for ( int i_pos=0; i_pos < i_size; ++i_pos ) {
                if ( ((Asset) olmodel_groups.getElementAt( i_pos )).getId ().equals ( group_source.getId () ) ) {
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
                                     AssetSearchManager m_search,
                                     JAssetLinkRenderer renderLinkCell
                                    ) {
        super ( lib_icon, lmodel_groups, renderLinkCell );
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
                                    AssetSearchManager m_search,
                                    JAssetLinkRenderer renderLinkCell
                                    ) {
        this ( lib_asset, lib_icon, new DefaultListModel (), m_search,
                renderLinkCell
                );
    }
    
    @Override
    public void	addLittleListener( LittleListener listen_little ) {
		oaview_delegate.addLittleListener ( listen_little );
	}
	
	
    @Override
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
    
    @Override
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
            AssetModel  amodel_group = olib_asset.get ( ((Asset) r_list.next ()).getId () );
            if ( null != amodel_group ) {
                amodel_group.removeLittleListener ( olisten_children );
            }
        }
        olmodel_groups.clear ();
        
        try {  // /load the children
            List<Asset>  v_groups = new ArrayList<Asset> ();
            {
                Collection<UUID>    v_ids = new ArrayList<UUID> ();  // serializable collection
                v_ids.addAll ( om_search.getAssetIdsFrom ( a_root.getId (), SecurityAssetType.GROUP ).values () );
                v_groups.addAll ( om_search.getAssets ( v_ids ) );
            }
            Collections.sort ( v_groups, new Comparator<Asset> () {
                @Override
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
    @Override
    public void setAssetModel ( AssetModel model_asset ) {
        Asset a_root = model_asset.getAsset ();
        setRoot ( a_root );
        oaview_delegate.setAssetModel ( model_asset );
        updateGrouplist ();
    }

    @Override
    public Feedback getFeedback() {
        return oaview_delegate.getFeedback();
    }

    @Override
    public void setFeedback( Feedback feedback ) {
        oaview_delegate.setFeedback( feedback );
    }
    
}

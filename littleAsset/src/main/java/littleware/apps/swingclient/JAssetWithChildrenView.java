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
import javax.swing.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.apps.image.ThumbManager;
import littleware.asset.*;


/** 
 * Abstract specialization of JGenericAssetView that
 * displays a list of child assets in the RightPanel window.
 * Subtypes just need to define the getChildInfo() method.
 */
public abstract class JAssetWithChildrenView extends JGenericAssetView {
	private final static Logger        olog_generic = Logger.getLogger ( JAssetWithChildrenView.class.getName() );
	
    private final IconLibrary                    olib_icon;
    private final JPanel                         owlist_members;
    private final AssetRetriever                 om_retriever;
    
    // Should replace with a custom list-model later to avoid so many updates
    private final SimpleListModel<Asset>         omodel_children = new SimpleListModel ( new ArrayList<Asset> () );
        
    
    /**
     * Subtypes override with call that returns a map from
     * the children of this asset to include in the view
     * each mapped to an info string to include with
     * the child.  The info string may be empty.
     */
    protected abstract Map<Asset,String> getChildInfo ();
    
    
    private Map<Asset,String>  ov_childinfo = new HashMap<Asset,String> ();
    
    
    @Override
    protected void updateAssetUI () {
        super.updateAssetUI ();

        java.util.List<Asset>        v_members = omodel_children.getList ();
        v_members.clear ();
        
        // Save the child-info for access by setLink before changing the omodel_list
        ov_childinfo = getChildInfo ();
        
        v_members.addAll ( ov_childinfo.keySet () );
        olog_generic.log ( Level.FINE, "Updating child info for: " + getAssetModel ().getAsset ().getName () +
                           ", num children: " + v_members.size ()
                           );

        Collections.sort ( v_members, 
                           new Comparator<Asset> () {
            @Override
                               public int compare ( Asset a_1, Asset a_2 ) {
                                   return a_1.getName ().compareTo ( a_2.getName () );
                               }
                           }
                           );
        
        omodel_children.fireChangeEvent ();
        /*... old DefaultListModel way ...
        omodel_children.clear ();
        for ( Asset a_child : v_members ) {
            omodel_children.addElement ( a_child );
        }        
        ...*/
    }
    
    /**
     * Let subtypes disable the generic-child tab
     *
     * @param b_enable set true to enable, false to disable
     */
    protected void setTabEnabled ( boolean b_enable ) {
        setTabEnabled ( owlist_members, b_enable );
    }
    
    
    /**
     * Constructor sets up the UI to view the given group.
     * Subtype constructor should invoke updateChildren() to populate the
     * child-info list.
     *
     * @param m_retriever to retrieve asset details with
     * @param lib_icon icon source
     * @param s_children_header label for the children-list area (ex: "Group members")
     * @param s_tab_label short string to label the child-tab with
     * @param icon_tab icon to label the child-tab with - may be null
     * @throws IllegalArgumentException if model_asset does not reference a group
     */
    @Inject
    protected JAssetWithChildrenView( AssetRetriever m_retriever,
                                      IconLibrary lib_icon, 
                                      AssetModelLibrary lib_asset,
                                      String s_children_header,
                                      String s_tab_label, Icon icon_tab, 
                                      String s_tab_tooltip,
                                      JAssetLinkList jList,
                                      Provider<JAssetLink> provideLinkView
                       ) {
        super( m_retriever, lib_icon, provideLinkView );
        om_retriever = m_retriever;
        olib_icon = lib_icon;
        jList.setModel(omodel_children);
        owlist_members = jList;
        /*..
        owlist_members = new JAssetLinkList ( omodel_children,
                                              lib_icon,
                                              lib_asset,
                                              m_retriever,
                                              s_children_header
                                              ) {
            @Override
            protected void setLink ( JAssetLink wlink_asset, Asset a_view ) {
                wlink_asset.setLink ( a_view );
                String s_value = ov_childinfo.get ( a_view );
                if ( null != s_value ) {
                    wlink_asset.setText ( wlink_asset.getText () + ": " + s_value );
                }
            }
                        
        };
        ..*/
        jList.addLittleListener (
                                      getListenBridge()
                                      );
        
        insertTab ( s_tab_label, icon_tab,
                    owlist_members, s_tab_tooltip,
                    -1
                    );        
    }
    
}
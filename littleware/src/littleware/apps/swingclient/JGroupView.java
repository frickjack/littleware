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
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;

import littleware.apps.client.*;
import littleware.apps.misc.ThumbManager;
import littleware.asset.*;
import littleware.security.SecurityAssetType;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;

/** 
 * Swing based AssetView for GROUP type assets.
 */
public class JGroupView extends JAssetWithChildrenView {
	private final static Logger        olog_generic = Logger.getLogger ( JGroupView.class.getName() );
    private static final long serialVersionUID = 8446740180654817120L;
	

    /**
     * Let our base class know about our members
     */
    @Override
    protected Map<Asset,String> getChildInfo () {
        AssetModel model_view = getAssetModel ();
        Map<Asset,String> v_childinfo = new HashMap<Asset,String> ();

        if ( ! model_view.getAsset ().getAssetType ().equals ( SecurityAssetType.GROUP ) ) {
            super.setTabEnabled ( false );
            return v_childinfo;
        }
        super.setTabEnabled ( true );
        LittleGroup group_view = (LittleGroup) model_view.getAsset ();
        
        for ( Enumeration<? extends Principal> enum_members = group_view.members ();
              enum_members.hasMoreElements ();
              ) {
            v_childinfo.put ( (LittlePrincipal) enum_members.nextElement (), null );
        }
        return v_childinfo;
    }

    /**
     * Constructor sets up the UI to view the given group
     */
    @Inject
    public JGroupView( AssetRetriever m_retriever,
                       IconLibrary lib_icon,
                       AssetModelLibrary lib_asset,
                       JAssetLinkList    jListChildren,
                       Provider<JAssetLink> provideLinkView
                       ) {
        super( m_retriever, lib_icon, lib_asset,
                "Group members",
               "Group Members", lib_icon.lookupIcon ( SecurityAssetType.GROUP ),
               "View members of group", jListChildren, provideLinkView
               );
    }
        
}
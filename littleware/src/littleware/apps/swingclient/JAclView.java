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
import java.security.acl.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.apps.client.*;
import littleware.apps.misc.ThumbManager;
import littleware.asset.*;
import littleware.security.*;


/** 
 * Swing based AssetView for ACL type assets.
 */
public class JAclView extends JAssetWithChildrenView {
	private final static Logger        olog_generic = Logger.getLogger ( JAclView.class.getName() );
    private static final long serialVersionUID = 6943919728024132915L;
	
    
    /**
     * Let our base class know about our members
     */
    @Override
    protected Map<Asset,String> getChildInfo () {
        AssetModel model_view = getAssetModel ();
        Map<Asset,String> v_childinfo = new HashMap<Asset,String> ();
        
        if ( ! model_view.getAsset ().getAssetType ().equals ( SecurityAssetType.ACL ) ) {
            olog_generic.log ( Level.FINE, "Ignoring non-ACL asset" );
            super.setTabEnabled ( false );
            return v_childinfo;
        }
        setTabEnabled ( true );
        
        LittleAcl acl_view = (LittleAcl) model_view.getAsset ();

        StringBuilder    sb_perms = new StringBuilder( 128 );
        for ( Enumeration<AclEntry> enum_members = acl_view.entries ();
              enum_members.hasMoreElements ();
              ) {
            LittleAclEntry  acl_entry = (LittleAclEntry) enum_members.nextElement ();
            LittlePrincipal p_entry = acl_entry.getPrincipal ();

            sb_perms.setLength ( 0 );
            for ( Enumeration<Permission> enum_perm = acl_entry.permissions (); 
                  enum_perm.hasMoreElements ();
                  ) {
                sb_perms.append ( enum_perm.nextElement ().toString () );
            }
            String s_info = sb_perms.toString ();
            olog_generic.log ( Level.FINE, "Adding to child info: " + s_info );
            v_childinfo.put ( p_entry, s_info );
        }
        return v_childinfo;
    }
    
    /**
     * Constructor sets up the UI to view the given acl
     */
    @Inject
    public JAclView( AssetRetriever m_retriever,
                       IconLibrary lib_icon ,
                       ThumbManager m_thumb,
                       AssetModelLibrary lib_asset,
                       JAssetLinkList    jListChildren,
                       Provider<JAssetLink> provideLinkView
                       ) {
        super( m_retriever, lib_icon, m_thumb, lib_asset, "ACL entries",
               "ACL Entries", lib_icon.lookupIcon( SecurityAssetType.ACL ),
               "View ACL entries", jListChildren, provideLinkView
               );
    }
    
}
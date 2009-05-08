/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security.server;

import littleware.asset.server.NullAssetSpecializer;
import littleware.asset.server.AssetSpecializer;
import com.google.inject.Inject;
import java.rmi.RemoteException;
import java.util.*;
import java.security.*;
import java.security.acl.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.*;
import littleware.asset.*;
import littleware.security.*;

/**
 * Simple AclManager implementation.
 */
public class SimpleAclManager extends NullAssetSpecializer implements AclManager, AssetSpecializer {
	
	private static final Logger             olog = Logger.getLogger ( SimpleAclManager.class.getName() );

	private final AssetManager       om_asset;
	private final AssetSearchManager om_searcher;
	
	
	/** 
	 * Constructor injects dependencies
	 *
	 * @param m_asset Asset manager
	 * @param m_searcher Asset lookup
	 * @param m_account to access acount info through
	 */
    @Inject
	public SimpleAclManager ( AssetManager m_asset, 
							  AssetSearchManager m_searcher ) {
		om_asset = m_asset;
		om_searcher = m_searcher;
	}

	

    @Override
	public LittleAcl getAcl( UUID u_id ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		return (LittleAcl) om_searcher.getAsset ( u_id );
	}

	
    @Override
	public LittleAcl getAcl ( String s_name )
		throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		LittleAcl a_result = om_searcher.getByName ( s_name, SecurityAssetType.ACL );
        if ( null == a_result ) {
            throw new NoSuchThingException ( "No ACL with name: " + s_name );
        }
        return a_result;
	}		
	
	

	

	/**
	 * Specialize ACL type assets
	 */
    @Override
	public <T extends Asset> T narrow ( T a_in, AssetRetriever m_retriever
						  ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		LittleAcl   acl_in = (LittleAcl) a_in;

        acl_in.clearEntries ();  // clear cloned entry list and rebuild
		final Map<String,UUID>  v_links = m_retriever.getAssetIdsFrom ( acl_in.getObjectId (),
																  SecurityAssetType.ACL_ENTRY
																  );
		
		final Set<Asset> v_link_assets = m_retriever.getAssets ( v_links.values () );
		
        for ( Asset a_link : v_link_assets ) {
            final UUID             u_principal = a_link.getToId ();
            final LittleAclEntry   acl_entry = (LittleAclEntry) a_link;

            final Principal p_entry = (Principal) m_retriever.getAsset ( u_principal );
            acl_entry.setPrincipal ( p_entry );
            
            acl_in.addEntry ( acl_entry );
            olog.log ( Level.FINE, "Just added entry for " + acl_entry.getName () +
                                " (negative: " + acl_entry.isNegative () + 
                               ") to ACL " + acl_in.getName () 
                               );
                               
        }
        return a_in;
	}
	
	
	/**
	 * Add an ACL-entry link to the repository
	 *
	 * @param acl_in to add to
	 * @param p_add principal to add to ACL
	 * @param perm_add permission to associate with the principal
	 * @param b_positive set true for positive permission, false for negative permission
	 * @param m_asset to save the new asset with
	 */
	private LittleAclEntry  addEntryToAcl ( LittleAcl acl_in, LittleAclEntry acl_entry,
								   AssetManager m_asset
								   ) throws BaseException, AssetException,
		GeneralSecurityException, RemoteException
	{
		acl_entry.setName ( acl_entry.getPrincipal ().getName () + "." + (acl_entry.isNegative () ? "negative" : "positive") );
		acl_entry.setFromId ( acl_in.getObjectId () );
		acl_entry.setOwnerId ( acl_in.getOwnerId () );
		acl_entry.setAclId ( acl_in.getAclId () );
        acl_entry.setHomeId ( acl_in.getHomeId () );
		acl_entry = (LittleAclEntry) m_asset.saveAsset ( acl_entry, "ACL entry tracker" );
		return acl_entry;
	}
	
	
	/**
	 * Save a new ACL entries into the repository
	 */
    @Override
	public void postCreateCallback ( Asset a_new, AssetManager m_asset  							   
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( SecurityAssetType.ACL.equals ( a_new.getAssetType () ) ) {
			LittleAcl     acl_new = (LittleAcl) a_new;
			
			for ( Enumeration<AclEntry> v_entries = acl_new.entries ();
				  v_entries.hasMoreElements ();
				  ) {
				LittleAclEntry      acl_entry = (LittleAclEntry) v_entries.nextElement ();
		
				addEntryToAcl ( acl_new, acl_entry, m_asset );
			}
		}
	}
	
	
    @Override
	public void postUpdateCallback ( Asset a_pre_update, Asset a_now, AssetManager m_asset 
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( SecurityAssetType.ACL.equals ( a_now.getAssetType () ) ) {
			Set<UUID>            v_now_entries = new HashSet<UUID> ();
			
			for ( Enumeration<AclEntry> v_entries = ((LittleAcl) a_now).entries ();
				  v_entries.hasMoreElements ();
				  ) {
				LittleAclEntry acl_entry = (LittleAclEntry) v_entries.nextElement ();
				
				if ( null != acl_entry.getObjectId () ) {
					v_now_entries.add ( acl_entry.getObjectId () );
				}
			}
			for ( Enumeration<AclEntry> v_entries = ((LittleAcl) a_pre_update).entries ();
				  v_entries.hasMoreElements ();
				  ) {
				LittleAclEntry acl_entry = (LittleAclEntry) v_entries.nextElement ();
				if ( ! v_now_entries.contains ( acl_entry.getObjectId () ) ) {
					m_asset.deleteAsset ( acl_entry.getObjectId () , "ACL update remove entry" );
				}
			}
			postCreateCallback ( a_now, m_asset );
		}
	}
	
	
    @Override
	public void postDeleteCallback ( Asset a_deleted, AssetManager m_asset
									 ) throws BaseException, AssetException, 
		GeneralSecurityException, RemoteException
	{
		if ( SecurityAssetType.ACL.equals ( a_deleted.getAssetType () ) ) {
			// Delete all the ACL_ENTRY assets off this thing
			for ( Enumeration<AclEntry> v_entries = ((LittleAcl) a_deleted).entries ();
				  v_entries.hasMoreElements ();
				  ) {
				LittleAclEntry a_cleanup = (LittleAclEntry) v_entries.nextElement ();
				m_asset.deleteAsset ( a_cleanup.getObjectId () , "Cleanup after ACL delete" );
			}
		}
	}
	

}


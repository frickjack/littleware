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
public class SimpleAclManager extends NullAssetSpecializer implements AclSpecializer {

    private static final Logger log = Logger.getLogger(SimpleAclManager.class.getName());
    private final AssetManager assetMgr;
    private final AssetSearchManager search;

    /**
     * Constructor injects dependencies
     *
     * @param m_asset Asset manager
     * @param m_searcher Asset lookup
     * @param m_account to access acount info through
     */
    @Inject
    public SimpleAclManager(AssetManager m_asset,
            AssetSearchManager m_searcher) {
        assetMgr = m_asset;
        search = m_searcher;
    }

    /**
     * Specialize ACL type assets
     */
    @Override
    public <T extends Asset> T narrow(T a_in, AssetRetriever retriever) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        final LittleAcl.Builder aclBuilder = a_in.narrow(LittleAcl.class).copy();

        final Map<String, UUID> linkMap = retriever.getAssetIdsFrom(aclBuilder.getId(),
                SecurityAssetType.ACL_ENTRY);

        final List<Asset> lineAssets = retriever.getAssets(linkMap.values());

        for (Asset link : lineAssets) {
            final UUID principalId = link.getToId();
            final LittlePrincipal entry = retriever.getAsset(principalId).get().narrow(LittlePrincipal.class);
            final LittleAclEntry aclEntry = link.narrow( LittleAclEntry.class ).copy().principal( entry ).build();

            aclBuilder.addEntry(aclEntry);
            log.log(Level.FINE, "Just added entry for " + aclEntry.getName() +
                    " (negative: " + aclEntry.isNegative() +
                    ") to ACL " + aclBuilder.getName());

        }
        return (T) aclBuilder.build();
    }


    /**
     * Save a new ACL entries into the repository
     */
    @Override
    public void postCreateCallback(Asset asset, AssetManager saver) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (SecurityAssetType.ACL.equals(asset.getAssetType())) {
            final LittleAcl acl = asset.narrow( LittleAcl.class );

            for (Enumeration<AclEntry> entries = ((LittleAcl) asset).entries();
                    entries.hasMoreElements();) {
                final LittleAclEntry startEntry = (LittleAclEntry) entries.nextElement();
                final LittlePrincipal principal = search.getAsset( startEntry.getToId() ).get().narrow();
                final LittleAclEntry.Builder entryBuilder = (LittleAclEntry.Builder) startEntry.copy().
                        principal( principal ).
                        name( principal.getName() + "." + (startEntry.isNegative() ? "negative" : "positive")).
                        parent( asset ).
                        ownerId(acl.getOwnerId());
                 assetMgr.saveAsset(entryBuilder.build(), "ACL entry tracker");
            }
        }
    }

    @Override
    public void postUpdateCallback(Asset a_pre_update, Asset a_now, AssetManager m_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (SecurityAssetType.ACL.equals(a_now.getAssetType())) {
            Set<UUID> v_now_entries = new HashSet<UUID>();

            for (Enumeration<AclEntry> v_entries = ((LittleAcl) a_now).entries();
                    v_entries.hasMoreElements();) {
                LittleAclEntry acl_entry = (LittleAclEntry) v_entries.nextElement();

                if (null != acl_entry.getId()) {
                    v_now_entries.add(acl_entry.getId());
                }
            }
            for (Enumeration<AclEntry> v_entries = ((LittleAcl) a_pre_update).entries();
                    v_entries.hasMoreElements();) {
                LittleAclEntry acl_entry = (LittleAclEntry) v_entries.nextElement();
                if (!v_now_entries.contains(acl_entry.getId())) {
                    m_asset.deleteAsset(acl_entry.getId(), "ACL update remove entry");
                }
            }
            postCreateCallback(a_now, m_asset);
        }
    }

    @Override
    public void postDeleteCallback(Asset a_deleted, AssetManager m_asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (SecurityAssetType.ACL.equals(a_deleted.getAssetType())) {
            // Delete all the ACL_ENTRY assets off this thing
            for (Enumeration<AclEntry> v_entries = ((LittleAcl) a_deleted).entries();
                    v_entries.hasMoreElements();) {
                final LittleAclEntry a_cleanup = (LittleAclEntry) v_entries.nextElement();
                m_asset.deleteAsset(a_cleanup.getId(), "Cleanup after ACL delete");
            }
        }
    }
}


/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.server.internal;

import littleware.asset.server.NullAssetSpecializer;
import com.google.inject.Inject;
import java.util.*;
import java.security.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.*;
import littleware.asset.*;
import littleware.asset.server.LittleContext;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerSearchManager;
import littleware.security.*;

/**
 * Simple AclManager implementation.
 */
public class SimpleAclManager extends NullAssetSpecializer implements AclSpecializer {

    private static final Logger log = Logger.getLogger(SimpleAclManager.class.getName());
    private final ServerAssetManager assetMgr;
    private final ServerSearchManager search;

    /**
     * Constructor injects dependencies
     *
     * @param assetMgr Asset manager
     * @param search Asset lookup
     * @param m_account to access acount info through
     */
    @Inject
    public SimpleAclManager(ServerAssetManager assetMgr,
            ServerSearchManager search) {
        this.assetMgr = assetMgr;
        this.search = search;
    }

    /**
     * Specialize ACL type assets
     */
    @Override
    public <T extends Asset> T narrow( LittleContext ctx, T assetIn) throws BaseException, AssetException,
            GeneralSecurityException {
        if ( assetIn instanceof LittleAclEntry ) {
            final LittleAclEntry entry = (LittleAclEntry) assetIn;
            return (T) entry.copy().principal(
                    (LittlePrincipal) search.getAsset( ctx, entry.getPrincipalId() ).get()
                    ).build();
        }
        // LittleAcl
        final LittleAcl.Builder aclBuilder = assetIn.narrow(LittleAcl.class).copy();

        final Map<String, UUID> linkMap = search.getAssetIdsFrom( ctx, aclBuilder.getId(),
                LittleAclEntry.ACL_ENTRY);

        final List<Asset> linkAssets = search.getAssets( ctx, linkMap.values());

        for (Asset link : linkAssets) {
            //final UUID principalId = link.getToId();
            //final LittlePrincipal entry = retriever.getAsset(principalId).get().narrow(LittlePrincipal.class);
            final LittleAclEntry aclEntry = link.narrow( LittleAclEntry.class );

            aclBuilder.addEntry(aclEntry);
            log.log(Level.FINE, "Just added entry for {0} (negative: {1}) to ACL {2}", new Object[]{aclEntry.getName(), aclEntry.isNegative(), aclBuilder.getName()});
        }
        return (T) aclBuilder.build();
    }


    /**
     * Save a new ACL entries into the repository
     */
    @Override
    public void postCreateCallback( LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleAcl.ACL_TYPE.equals(asset.getAssetType())) {
            final LittleAcl acl = asset.narrow();

            for (Enumeration<LittleAclEntry> entries = ((LittleAcl) asset).entries();
                    entries.hasMoreElements();) {
                final LittleAclEntry startEntry = (LittleAclEntry) entries.nextElement();
                final LittlePrincipal principal = search.getAsset( ctx, startEntry.getPrincipalId() ).get().narrow();
                final LittleAclEntry.Builder entryBuilder = (LittleAclEntry.Builder) startEntry.copy().
                        principal( principal ).
                        name( principal.getName() + "." + (startEntry.isNegative() ? "negative" : "positive")).
                        acl( acl ).
                        ownerId(acl.getOwnerId());
                 assetMgr.saveAsset( ctx, entryBuilder.build(), "ACL entry tracker");
            }
        }
    }

    @Override
    public void postUpdateCallback( LittleContext ctx, Asset preUpdate, Asset postUpdate) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleAcl.ACL_TYPE.equals(postUpdate.getAssetType())) {
            Set<UUID> v_now_entries = new HashSet<UUID>();

            for (Enumeration<LittleAclEntry> v_entries = ((LittleAcl) postUpdate).entries();
                    v_entries.hasMoreElements();) {
                LittleAclEntry acl_entry = v_entries.nextElement();

                if (null != acl_entry.getId()) {
                    v_now_entries.add(acl_entry.getId());
                }
            }
            for (Enumeration<LittleAclEntry> v_entries = ((LittleAcl) preUpdate).entries();
                    v_entries.hasMoreElements();) {
                LittleAclEntry acl_entry = (LittleAclEntry) v_entries.nextElement();
                if (!v_now_entries.contains(acl_entry.getId())) {
                    assetMgr.deleteAsset( ctx, acl_entry.getId(), "ACL update remove entry");
                }
            }
            postCreateCallback( ctx, postUpdate);
        }
    }

    @Override
    public void postDeleteCallback( LittleContext ctx, Asset asset ) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleAcl.ACL_TYPE.equals(asset.getAssetType())) {
            // Delete all the ACL_ENTRY assets off this thing
            for (Enumeration<LittleAclEntry> entries = ((LittleAcl) asset).entries();
                    entries.hasMoreElements();) {
                final LittleAclEntry entry = entries.nextElement();
                assetMgr.deleteAsset( ctx, entry.getId(), "Cleanup after ACL delete");
            }
        }
    }
}

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
import littleware.asset.internal.RemoteAssetRetriever.AssetResult;
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
    public <T extends Asset> T narrow(LittleContext ctx, T assetIn) throws BaseException, AssetException,
            GeneralSecurityException {
        if (assetIn instanceof LittleAclEntry) {
            final LittleAclEntry entry = (LittleAclEntry) assetIn;
            return (T) entry.copy().narrow(LittleAclEntry.Builder.class).principal(
                    search.getAsset(ctx, entry.getPrincipalId(), -1).getAsset().get().narrow(LittlePrincipal.class)).build();
        }
        // LittleAcl
        final LittleAcl.Builder aclBuilder = assetIn.copy().narrow();

        final Map<String, UUID> linkMap = search.getAssetIdsFrom(ctx, aclBuilder.getId(),
                LittleAclEntry.ACL_ENTRY);

        final Collection<AssetResult> linkAssets = search.getAssets(ctx, linkMap.values()).values();

        for (AssetResult ref : linkAssets) {
            if (ref.getAsset().isSet()) {
                //final UUID principalId = link.getToId();
                //final LittlePrincipal entry = retriever.getAsset(principalId).get().narrow(LittlePrincipal.class);
                final LittleAclEntry aclEntry = ref.getAsset().get().narrow(LittleAclEntry.class);

                aclBuilder.addEntry(aclEntry);
                log.log(Level.FINE, "Just added entry for {0} (negative: {1}) to ACL {2}", new Object[]{aclEntry.getName(), aclEntry.isNegative(), aclBuilder.getName()});
            }
        }
        return (T) aclBuilder.build();
    }

    /**
     * Save a new ACL entries into the repository
     */
    @Override
    public Set<Asset> postCreateCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleAcl.ACL_TYPE.equals(asset.getAssetType())) {
            final LittleAcl acl = asset.narrow();

            for (Enumeration<LittleAclEntry> entries = ((LittleAcl) asset).entries();
                    entries.hasMoreElements();) {
                final LittleAclEntry startEntry = (LittleAclEntry) entries.nextElement();
                final LittlePrincipal principal = search.getAsset(ctx, startEntry.getPrincipalId(), -1L ).getAsset().get().narrow();
                final LittleAclEntry.Builder entryBuilder = startEntry.copy().narrow(LittleAclEntry.Builder.class).
                        principal(principal).
                        name(principal.getName() + "." + (startEntry.isNegative() ? "negative" : "positive")).
                        owningAcl(acl).
                        ownerId(acl.getOwnerId());
                assetMgr.saveAsset(ctx, entryBuilder.build(), "ACL entry tracker");
            }
        }
        return Collections.emptySet();
    }

    @Override
    public Set<Asset> postUpdateCallback(LittleContext ctx, Asset preUpdate, Asset postUpdate) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleAcl.ACL_TYPE.equals(postUpdate.getAssetType())) {
            final Set<UUID> postUpdateEntries = new HashSet<UUID>();

            for (Enumeration<LittleAclEntry> entries = ((LittleAcl) postUpdate).entries();
                    entries.hasMoreElements();) {
                final LittleAclEntry entry = entries.nextElement();

                if (null != entry.getId()) {
                    postUpdateEntries.add(entry.getId());
                }
            }
            for (Enumeration<LittleAclEntry> entries = ((LittleAcl) preUpdate).entries();
                    entries.hasMoreElements();) {
                final LittleAclEntry entry = (LittleAclEntry) entries.nextElement();
                if (!postUpdateEntries.contains(entry.getId())) {
                    assetMgr.deleteAsset(ctx, entry.getId(), "ACL update remove entry");
                }
            }
            postCreateCallback(ctx, postUpdate);
        }
        return Collections.emptySet();
    }

    @Override
    public void postDeleteCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleAcl.ACL_TYPE.equals(asset.getAssetType())) {
            // Delete all the ACL_ENTRY assets off this thing
            for (Enumeration<LittleAclEntry> entries = ((LittleAcl) asset).entries();
                    entries.hasMoreElements();) {
                final LittleAclEntry entry = entries.nextElement();
                assetMgr.deleteAsset(ctx, entry.getId(), "Cleanup after ACL delete");
            }
        }
    }
}

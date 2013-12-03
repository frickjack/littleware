/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.server.internal;

import com.google.common.collect.ImmutableMap;
import littleware.asset.server.NullAssetSpecializer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.*;
import littleware.asset.*;
import littleware.asset.internal.RemoteSearchManager.AssetResult;
import littleware.asset.server.LittleContext;
import littleware.security.LittleGroupMember.MemberBuilder;
import littleware.security.server.QuotaUtil;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.ServerAssetManager;
import littleware.asset.server.ServerSearchManager;
import littleware.security.*;

/**
 * Simple security-manager implementation
 */
public class SimpleAccountManager extends NullAssetSpecializer {

    private static final Logger log = Logger.getLogger(SimpleAccountManager.class.getName());
    private final ServerAssetManager assetMgr;
    private final ServerSearchManager search;
    private final QuotaUtil quotaUtil;
    private final Provider<LittleTransaction> transactionProvider;
    private final Provider<MemberBuilder> memberBuilder;
    private final Everybody everybody;

    /**
     * Constructor injects dependencies
     *
     * @param assetMgr asset manager
     * @param search asset search manager
     * @param m_dbauth access to password database handlers
     */
    @Inject
    public SimpleAccountManager(ServerAssetManager assetMgr,
            ServerSearchManager search,
            QuotaUtil quotaUtil,
            Provider<LittleTransaction> transactionProvider,
            Provider<LittleGroupMember.MemberBuilder> memberBuilder,
            Everybody everybody) {
        this.assetMgr = assetMgr;
        this.search = search;
        this.quotaUtil = quotaUtil;
        this.transactionProvider = transactionProvider;
        this.memberBuilder = memberBuilder;
        this.everybody = everybody;
    }

    /**
     * Specialize USER and GROUP type assets
     *
     * @param asset instance of class returned by a_in.getAssetType ().create ()
     * @return asset decorated with new data, or a new Asset consistent with the data in a_in
     */
    @Override
    public <T extends Asset> T narrow(LittleContext ctx, T asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (asset.getAssetType().equals(LittleUser.USER_TYPE) || asset.getAssetType().equals(Quota.QUOTA_TYPE)) {
            return asset;
        }
        if (asset.getId().equals(AccountManager.UUID_EVERYBODY_GROUP)) {
            return (T) everybody;
        }
        Whatever.get().check("AccountManager is not the specializer for: "
                + asset.getAssetType(),
                asset.getAssetType().equals(LittleGroup.GROUP_TYPE));
        log.log(Level.FINE, "Narrowing group: {0}", asset.getName());

        // It's a GROUP - need to populate it
        final LittleGroup.Builder groupBuilder = asset.copy().narrow();
        final ImmutableMap<String, AssetInfo> groupLinks = search.getAssetIdsFrom(ctx, groupBuilder.getId(),
                LittleGroupMember.GROUP_MEMBER_TYPE);
        final Collection<UUID>   linkIds = AssetInfo.mapIds(groupLinks.values() );
        final Collection<AssetResult> linkAssets = search.getAssets(ctx, linkIds).values();

        /** 
         * This check not true after a member group/user has been deleted.
         * Cleanup takes place at update time for this group.
         *
        Whatever.get().check ( "got expected number of assets: " + v_links.size () + " == " + v_link_assets.size (),
        v_links.size () == v_link_assets.size () 
        );
         */
        log.log(Level.FINE, "Group: {0} found {1} children under {2} of type {3}", new Object[]{asset.getName(), linkAssets.size(), asset.getId(), LittleGroupMember.GROUP_MEMBER_TYPE});


        final Set<UUID> memberIds = new HashSet<UUID>();
        for (AssetResult linkResult : linkAssets) {
            if (linkResult.getAsset().isSet()) {
                final LittleGroupMember member = linkResult.getAsset().get().narrow();
                log.log(Level.FINE, "Got link UUID {0}", member.getMemberId());
                memberIds.add(member.getMemberId());
            }
        }

        // Recursion here!
        final Collection<AssetResult> memberAssets = search.getAssets(ctx, memberIds).values();

        for (AssetResult memberResult : memberAssets) {
            if (memberResult.getAsset().isSet()) {
                final LittlePrincipal member = memberResult.getAsset().get().narrow();
                log.log(Level.FINE, "adding {0} to {1}", new Object[]{member.getName(), groupBuilder.getName()});
                groupBuilder.add(member);
            }
        }

        return (T) groupBuilder.build();
    }

    /**
     * Little internal utility that creates the GROUP_MEMBER necessary
     * to add the given member to the given group
     *
     * @param group to add to
     * @param member to add to group
     * @param assetMgr manager to use to save the data
     * @return the new group-member asset
     */
    public Asset addMemberToGroup(LittleContext ctx, LittleGroup group, LittlePrincipal member,
            ServerAssetManager assetMgr) throws BaseException, AssetException, GeneralSecurityException {
        final LittleGroupMember link = memberBuilder.get().
                group(group).member(member).
                name(member.getName()).
                comment("Member of group: " + member.getName()).
                build();
        return assetMgr.saveAsset(ctx, link, "new group member").get(link.getId());
    }

    /**
     * Specializer USER/GROUP updates. <br />
     *  <ul>
     *    <li> USER: setup QUOTA </li>
     *    <li> GROUP: save members ? </li>
     * </ul>
     */
    @Override
    public Set<Asset> postCreateCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleUser.USER_TYPE.equals(asset.getAssetType())) {
            // We need to setup a quota
            final LittleUser caller = ctx.getCaller();
            final Quota quota = quotaUtil.getQuota(ctx, caller, search);
            if (null != quota) {
                final Quota.Builder quotaBuilder = quota.copy().narrow();

                quotaBuilder.setId(UUID.randomUUID());
                quotaBuilder.setOwnerId(AccountManager.UUID_ADMIN);
                quotaBuilder.setUserId(asset.getId());
                quotaBuilder.setNextInChainId(quota.getId());
                assetMgr.saveAsset(ctx, quotaBuilder.build(), "New quota");
            }

        } else if (LittleGroup.GROUP_TYPE.equals(asset.getAssetType())) {
            postUpdateCallback(ctx, null, asset);
        }
        return Collections.emptySet();
    }

    /**
     * Delete group-member links to/from when a group or user gets deleted
     */
    @Override
    public void postDeleteCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (asset.getId().equals(AccountManager.UUID_EVERYBODY_GROUP)) {
            throw new IllegalArgumentException("May not modify the everybody group");
        }
        if (LittleGroup.GROUP_TYPE.equals(asset.getAssetType()) || LittleUser.USER_TYPE.equals(asset.getAssetType())) {
            final Set<UUID> vChildren = new HashSet<>();
            vChildren.addAll( AssetInfo.mapIds(
                    search.getAssetIdsFrom(ctx, asset.getId(),
                    LittleGroupMember.GROUP_MEMBER_TYPE).values())
                    );
            vChildren.addAll( AssetInfo.mapIds(
                    search.getAssetIdsTo(ctx, asset.getId(),
                    LittleGroupMember.GROUP_MEMBER_TYPE).values()) 
                    );

            final Collection<AssetResult> linkResults = search.getAssets(ctx, vChildren).values();
            for (AssetResult maybeLink : linkResults) {
                if (maybeLink.getAsset().isSet()) {
                    assetMgr.deleteAsset(ctx, maybeLink.getAsset().get().getId(), "cleaning up deleted principal");
                }
            }
        }

    }

    /**
     * Update group membership info when group asset gets updated
     *
     * @param preUpdate may be null so that postCreateCallback
     *              can leverage this
     */
    @Override
    public Set<Asset> postUpdateCallback(LittleContext ctx, Asset preUpdate, Asset postUpdate) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleGroup.GROUP_TYPE.equals(postUpdate.getAssetType())) {
            final LittleGroup group = postUpdate.narrow(LittleGroup.class);
            if (group.getId().equals(AccountManager.UUID_EVERYBODY_GROUP)) {
                throw new IllegalArgumentException("May not modify the everybody group");
            }
            log.log(Level.FINE, "Update callback running for {0}", group.getName());
            final Set<Principal> v_before = new HashSet<Principal>();

            if (null != preUpdate) {
                v_before.addAll(preUpdate.narrow(LittleGroup.class).getMembers());
            }

            final List<LittlePrincipal> addPrincipals = new ArrayList<LittlePrincipal>();
            final Set<UUID> memberSet = new HashSet<>();

            // Get the list of new group members that need added
            for (LittlePrincipal member : group.getMembers()) {
                memberSet.add(member.getId());
                if (!v_before.remove(member)) {
                    addPrincipals.add(member);
                }
            }

            {
                // Get the collection of assets linking the group-asset
                // to the group-members, and delete the unneeded ones
                final ImmutableMap<String, AssetInfo> childrenMap = search.getAssetIdsFrom(ctx, group.getId(),
                        LittleGroupMember.GROUP_MEMBER_TYPE);
                for (AssetInfo linkInfo : childrenMap.values()) {
                        final LittleGroupMember member = search.getAsset( ctx, linkInfo.getId() ).get().narrow();
                        if (!memberSet.contains(member.getMemberId())) {
                            assetMgr.deleteAsset(ctx, member.getId(), "member no longer in group");
                        }
                }
            }

            // Add the new members to the group
            for (LittlePrincipal member : addPrincipals) {
                log.log(Level.FINE, "Adding {0} to group {1}", new Object[]{member.getName(), group.getName()});
                addMemberToGroup(ctx, group, member, assetMgr);
            }
        }
        return Collections.emptySet();
    }
}

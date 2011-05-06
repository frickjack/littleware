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
import com.google.inject.Provider;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.*;
import littleware.asset.*;
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
            Provider<LittleGroupMember.MemberBuilder> memberBuilder) {
        this.assetMgr = assetMgr;
        this.search = search;
        this.quotaUtil = quotaUtil;
        this.transactionProvider = transactionProvider;
        this.memberBuilder = memberBuilder;
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
        Whatever.get().check("AccountManager is not the specializer for: "
                + asset.getAssetType(),
                asset.getAssetType().equals(LittleGroup.GROUP_TYPE));
        log.log(Level.FINE, "Narrowing group: {0}", asset.getName());

        // It's a GROUP - need to populate it
        final LittleGroup.Builder groupBuilder = asset.narrow(LittleGroup.class).copy();
        final Map<String, UUID> groupLinks = search.getAssetIdsFrom( ctx, groupBuilder.getId(),
                LittleGroupMember.GROUP_MEMBER_TYPE);

        final List<Asset> v_link_assets = search.getAssets( ctx, groupLinks.values());

        /** 
         * This check not true after a member group/user has been deleted.
         * Cleanup takes place at update time for this group.
         *
        Whatever.get().check ( "got expected number of assets: " + v_links.size () + " == " + v_link_assets.size (),
        v_links.size () == v_link_assets.size () 
        );
         */
        log.log(Level.FINE, "Group: {0} found {1} children under {2} of type {3}", new Object[]{asset.getName(), v_link_assets.size(), asset.getId(), LittleGroupMember.GROUP_MEMBER_TYPE});


        final Set<UUID> v_members = new HashSet<UUID>();
        for (Asset link : v_link_assets) {
            final LittleGroupMember member = link.narrow();
            log.log(Level.FINE, "Got link UUID {0}", member.getMemberId());
            v_members.add(member.getMemberId());
        }

        // Recursion here!
        final List<Asset> v_member_assets = search.getAssets(ctx, v_members);

        for (Asset a_member : v_member_assets) {
            log.log(Level.FINE, "adding {0} to {1}", new Object[]{a_member.getName(), groupBuilder.getName()});
            groupBuilder.add(a_member.narrow(LittlePrincipal.class));
        }

        if (groupBuilder.getName().equals(AccountManager.LITTLEWARE_ADMIN_GROUP)) {
            // then add the admin principal
            groupBuilder.add(search.getAsset(ctx, AccountManager.UUID_ADMIN).get().narrow(LittleUser.class));
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
        return assetMgr.saveAsset(ctx,
                memberBuilder.get().
                group(group).member(member).
                name(member.getName()).
                comment("Member of group: " + member.getName()).
                build(), "new group member");
    }

    /**
     * Specializer USER/GROUP updates. <br />
     *  <ul>
     *    <li> USER: setup QUOTA </li>
     *    <li> GROUP: save members ? </li>
     * </ul>
     */
    @Override
    public void postCreateCallback(LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleUser.USER_TYPE.equals(asset.getAssetType())) {
            // We need to setup a quota
            final LittleUser caller = ctx.getCaller();
            Quota a_caller_quota = quotaUtil.getQuota( ctx, caller, search);
            if (null != a_caller_quota) {
                final Quota.Builder quotaBuilder = a_caller_quota.copy();

                quotaBuilder.setId(UUID.randomUUID());
                quotaBuilder.setOwnerId(AccountManager.UUID_ADMIN);
                quotaBuilder.setUserId(asset.getId());
                quotaBuilder.setNextInChainId(a_caller_quota.getId());
                assetMgr.saveAsset(ctx, quotaBuilder.build(), "New quota");
            }
            // Add this frickjack to the everybody group
            {
                final LittleGroup everybody = search.getByName(ctx, AccountManager.LITTLEWARE_EVERYBODY_GROUP, LittleGroup.GROUP_TYPE).get().narrow();
                addMemberToGroup(
                        ctx.getAdminContext(), everybody,
                        asset.narrow(LittlePrincipal.class),
                        assetMgr
                        );
            }
            
            // Give the user a password
            /*... Disable for now - moving to JPA, and don't want to deal with this now ...
            try {
            DbWriter<String> sql_password = om_dbauth.makeDbPasswordSaver(a_new.getId());
            sql_password.saveObject("default_password");
            } catch (SQLException e) {
            throw new DataAccessException("Falure updating password, caught: " + e, e);
            }
             */

        } else if (LittleGroup.GROUP_TYPE.equals(asset.getAssetType())) {
            postUpdateCallback( ctx, null, asset);
        }
    }

    /**
     * Delete group-member links to/from when a group or user gets deleted
     */
    @Override
    public void postDeleteCallback( LittleContext ctx, Asset asset) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleGroup.GROUP_TYPE.equals(asset.getAssetType()) || LittleUser.USER_TYPE.equals(asset.getAssetType())) {
            Set<UUID> vChildren = new HashSet<UUID>();
            vChildren.addAll(
                    search.getAssetIdsFrom( ctx, asset.getId(),
                    LittleGroupMember.GROUP_MEMBER_TYPE).values());
            vChildren.addAll(
                    search.getAssetIdsTo( ctx, asset.getId(),
                    LittleGroupMember.GROUP_MEMBER_TYPE));

            final List<Asset> v_member_links = search.getAssets( ctx, vChildren);
            for (Asset p_link : v_member_links) {
                assetMgr.deleteAsset( ctx, p_link.getId(), "cleaning up deleted principal");
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
    public void postUpdateCallback( LittleContext ctx, Asset preUpdate, Asset postUpdate ) throws BaseException, AssetException,
            GeneralSecurityException {
        if (LittleGroup.GROUP_TYPE.equals(postUpdate .getAssetType())) {
            final LittleGroup group = postUpdate .narrow(LittleGroup.class);
            log.log(Level.FINE, "Update callback running for {0}", group.getName());
            final Set<Principal> v_before = new HashSet<Principal>();

            if (null != preUpdate) {
                v_before.addAll(preUpdate.narrow(LittleGroup.class).getMembers());
            }

            final List<LittlePrincipal> v_add = new ArrayList<LittlePrincipal>();
            final Set<UUID> memberSet = new HashSet<UUID>();

            // Get the list of new group members that need added
            for (LittlePrincipal member : group.getMembers()) {
                memberSet.add(member.getId());
                if (!v_before.remove(member)) {
                    v_add.add(member);
                }
            }

            {
                // Get the collection of assets linking the group-asset
                // to the group-members, and delete the unneeded ones
                final Map<String, UUID> v_children = search.getAssetIdsFrom( ctx, group.getId(),
                        LittleGroupMember.GROUP_MEMBER_TYPE);
                final List<Asset> v_member_links = search.getAssets( ctx, v_children.values());
                for (Asset link : v_member_links) {
                    final LittleGroupMember member = link.narrow();
                    if (!memberSet.contains(member.getMemberId())) {
                        assetMgr.deleteAsset( ctx, member.getId(), "member no longer in group");
                    }
                }
            }

            // Add the new members to the group
            for (LittlePrincipal member : v_add) {
                log.log(Level.FINE, "Adding {0} to group {1}", new Object[]{member.getName(), group.getName()});
                addMemberToGroup( ctx, group, member, assetMgr);
            }
        }
    }
}

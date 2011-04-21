/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.server.internal;

import littleware.asset.server.NullAssetSpecializer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.security.Principal;
import java.security.PrivilegedActionException;
import java.security.PrivilegedExceptionAction;
import javax.security.auth.*;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.base.*;
import littleware.asset.*;
import littleware.security.LittleGroupMember.MemberBuilder;
import littleware.security.server.QuotaUtil;
import littleware.asset.server.LittleTransaction;
import littleware.security.*;

/**
 * Simple security-manager implementation
 */
public class SimpleAccountManager extends NullAssetSpecializer {

    private static final Logger log = Logger.getLogger(SimpleAccountManager.class.getName());
    private final AssetManager assetMgr;
    private final AssetSearchManager search;
    private final QuotaUtil quotaUtil;
    private final Provider<LittleTransaction> transactionProvider;
    private final Provider<LittleUser> callerProvider;
    private final Provider<MemberBuilder> memberBuilder;

    /**
     * Constructor injects dependencies
     *
     * @param assetMgr asset manager
     * @param search asset search manager
     * @param m_dbauth access to password database handlers
     */
    @Inject
    public SimpleAccountManager(AssetManager assetMgr,
            AssetSearchManager search,
            QuotaUtil quotaUtil,
            Provider<LittleTransaction> transactionProvider,
            Provider<LittleUser> callerProvider,
            Provider<LittleGroupMember.MemberBuilder> memberBuilder ) {
        this.assetMgr = assetMgr;
        this.search = search;
        this.quotaUtil = quotaUtil;
        this.transactionProvider = transactionProvider;
        this.callerProvider = callerProvider;
        this.memberBuilder = memberBuilder;
    }

    /**
     * Specialize USER and GROUP type assets
     *
     * @param asset instance of class returned by a_in.getAssetType ().create ()
     * @return asset decorated with new data, or a new Asset consistent with the data in a_in
     */
    @Override
    public <T extends Asset> T narrow(T asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (asset.getAssetType().equals(LittleUser.USER_TYPE) || asset.getAssetType().equals(Quota.QUOTA_TYPE)) {
            return asset;
        }
        Whatever.get().check("AccountManager is not the specializer for: " +
                asset.getAssetType(),
                asset.getAssetType().equals(LittleGroup.GROUP_TYPE));
        log.log(Level.FINE, "Narrowing group: {0}", asset.getName());

        // It's a GROUP - need to populate it
        final LittleGroup.Builder groupBuilder = asset.narrow(LittleGroup.class).copy();
        Map<String, UUID> v_links = search.getAssetIdsFrom(groupBuilder.getId(),
                LittleGroupMember.GROUP_MEMBER_TYPE);

        final List<Asset> v_link_assets = search.getAssets(v_links.values());

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
            v_members.add( member.getMemberId() );
        }

        // Recursion here!
        final List<Asset> v_member_assets = search.getAssets(v_members);

        for (Asset a_member : v_member_assets) {
            log.log(Level.FINE, "adding {0} to {1}", new Object[]{a_member.getName(), groupBuilder.getName()});
            groupBuilder.add(a_member.narrow(LittlePrincipal.class));
        }

        if (groupBuilder.getName().equals(AccountManager.LITTLEWARE_ADMIN_GROUP)) {
            // then add the admin principal
            groupBuilder.add(search.getAsset(AccountManager.UUID_ADMIN).get().narrow(LittleUser.class));
        }

        return (T) groupBuilder.build();
    }
    private static Subject oj_admin = null;

    /**
     * Get a Subject representing the littleware admin
     */
    private Subject getAdmin() throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (null == oj_admin) {
            try {
                final LittleUser p_admin = search.getByName(AccountManager.LITTLEWARE_ADMIN, LittleUser.USER_TYPE).get().narrow();
                final Set<Principal> v_users = new HashSet<Principal>();

                v_users.add(p_admin);
                oj_admin = new Subject(true, v_users, new HashSet<Object>(), new HashSet<Object>());
            } catch (NoSuchThingException e) {
                throw new AssertionFailedException("LITTLEWARE_ADMIN should exist, caught: " + e, e);
            } catch (GeneralSecurityException e) {
                throw new AssertionFailedException("LITTLEWARE_ADMIN should be accessible, but caught: " + e,
                        e);
            }
        }
        return oj_admin;
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
    public Asset addMemberToGroup(LittleGroup group, LittlePrincipal member,
            AssetManager assetMgr) throws BaseException, AssetException, GeneralSecurityException,
            RemoteException {
        return assetMgr.saveAsset(
                memberBuilder.get().
                group(group).member(member).
                name(member.getName()).
                comment("Member of group: " + member.getName()).
                build(), "new group member");
    }

    /**
     * Little PrivilegedAction to add a member to a group as the ADMIN user
     */
    public class AddToGroupAction implements PrivilegedExceptionAction<Asset> {

        private LittleGroup op_group = null;
        private LittlePrincipal op_member = null;
        private AssetManager om_asset = null;

        public AddToGroupAction(LittleGroup p_group, LittlePrincipal p_member,
                AssetManager m_asset) {
            op_group = p_group;
            op_member = p_member;
            om_asset = m_asset;
        }

        /** Create a GROUP_MEMBER asset linking this action's group and new member */
        @Override
        public Asset run() throws BaseException, AssetException,
                GeneralSecurityException, RemoteException {
            return addMemberToGroup(op_group, op_member, om_asset);
        }
    }

    /**
     * Specializer USER/GROUP updates. <br />
     *  <ul>
     *    <li> USER: setup QUOTA </li>
     *    <li> GROUP: save members ? </li>
     * </ul>
     */
    @Override
    public void postCreateCallback(Asset asset) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (LittleUser.USER_TYPE.equals(asset.getAssetType())) {
            // We need to setup a quota
            final LittleUser caller = callerProvider.get();
            Quota a_caller_quota = quotaUtil.getQuota(caller, search);
            if (null != a_caller_quota) {
                final Quota.Builder quotaBuilder = a_caller_quota.copy();

                quotaBuilder.setId(UUID.randomUUID());
                quotaBuilder.setOwnerId( AccountManager.UUID_ADMIN);
                quotaBuilder.setUserId(asset.getId());
                quotaBuilder.setNextInChainId(a_caller_quota.getId());
                assetMgr.saveAsset(quotaBuilder.build(), "New quota");
            }
            // Add this frickjack to the everybody group
            {
                final LittleGroup everybody = search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP, LittleGroup.GROUP_TYPE).get().narrow();

                try {
                    PrivilegedExceptionAction act_add2group = new AddToGroupAction(everybody,
                            asset.narrow(LittlePrincipal.class),
                            assetMgr);
                    Subject j_admin = getAdmin();

                    Subject.doAs(j_admin, act_add2group);
                } catch (PrivilegedActionException e) {
                    Exception e_cause = e.getException();
                    if (e_cause instanceof BaseException) {
                        throw (BaseException) e_cause;
                    }
                    if (e_cause instanceof GeneralSecurityException) {
                        throw (GeneralSecurityException) e_cause;
                    }
                    if (e_cause instanceof AssetException) {
                        throw (AssetException) e_cause;
                    }
                    throw new AssertionFailedException("Exception of unexpected type", e_cause);
                }
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
            postUpdateCallback(null, asset);
        }
    }

    /**
     * Delete group-member links to/from when a group or user gets deleted
     */
    @Override
    public void postDeleteCallback(Asset a_deleted ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (LittleGroup.GROUP_TYPE.equals(a_deleted.getAssetType()) || LittleUser.USER_TYPE.equals(a_deleted.getAssetType())) {
            Set<UUID> vChildren = new HashSet<UUID>();
            vChildren.addAll(
                    search.getAssetIdsFrom(a_deleted.getId(),
                    LittleGroupMember.GROUP_MEMBER_TYPE).values());
            vChildren.addAll(
                    search.getAssetIdsTo(a_deleted.getId(),
                    LittleGroupMember.GROUP_MEMBER_TYPE));

            final List<Asset> v_member_links = search.getAssets(vChildren);
            for (Asset p_link : v_member_links) {
                assetMgr.deleteAsset(p_link.getId(), "cleaning up deleted principal");
            }
        }

    }

    /**
     * Update group membership info when group asset gets updated
     *
     * @param a_pre_update may be null so that postCreateCallback
     *              can leverage this
     */
    @Override
    public void postUpdateCallback(Asset a_pre_update, Asset a_now ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (LittleGroup.GROUP_TYPE.equals(a_now.getAssetType())) {
            final LittleGroup group = a_now.narrow(LittleGroup.class);
            log.log(Level.FINE, "Update callback running for {0}", group.getName());
            final Set<Principal> v_before = new HashSet<Principal>();

            if (null != a_pre_update) {
                v_before.addAll(a_pre_update.narrow(LittleGroup.class).getMembers());
            }

            final List<LittlePrincipal> v_add = new ArrayList<LittlePrincipal>();
            final Set<UUID> memberSet = new HashSet<UUID>();

            // Get the list of new group members that need added
            for (LittlePrincipal member : group.getMembers() ) {
                memberSet.add(member.getId());
                if (!v_before.remove(member)) {
                    v_add.add(member);
                }
            }

            {
                // Get the collection of assets linking the group-asset
                // to the group-members, and delete the unneeded ones
                final Map<String, UUID> v_children = search.getAssetIdsFrom(group.getId(),
                        LittleGroupMember.GROUP_MEMBER_TYPE);
                final List<Asset> v_member_links = search.getAssets(v_children.values());
                for (Asset link : v_member_links) {
                    final LittleGroupMember member = link.narrow();
                    if (!memberSet.contains(member.getMemberId() )) {
                        assetMgr.deleteAsset(member.getId(), "member no longer in group");
                    }
                }
            }

            // Add the new members to the group
            for (LittlePrincipal member : v_add) {
                log.log(Level.FINE, "Adding {0} to group {1}", new Object[]{member.getName(), group.getName()});
                addMemberToGroup(group, member, assetMgr);
            }
        }
    }
}

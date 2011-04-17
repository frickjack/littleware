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
import littleware.security.server.QuotaUtil;
import littleware.asset.server.LittleTransaction;
import littleware.security.*;

/**
 * Simple security-manager implementation
 */
public class SimpleAccountManager extends NullAssetSpecializer {

    private static final Logger log = Logger.getLogger(SimpleAccountManager.class.getName());
    private final AssetManager om_asset;
    private final AssetSearchManager om_search;
    private final QuotaUtil om_quota;
    private final Provider<LittleTransaction> oprovideTrans;
    private final Provider<LittleUser> provideCaller;

    /**
     * Constructor injects dependencies
     *
     * @param m_asset asset manager
     * @param m_searcher asset search manager
     * @param m_dbauth access to password database handlers
     */
    @Inject
    public SimpleAccountManager(AssetManager m_asset,
            AssetSearchManager m_searcher,
            QuotaUtil m_quota,
            Provider<LittleTransaction> provideTrans,
            Provider<LittleUser> provideCaller ) {
        om_asset = m_asset;
        om_search = m_searcher;
        om_quota = m_quota;
        oprovideTrans = provideTrans;
        this.provideCaller = provideCaller;
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
        if (asset.getAssetType().equals(SecurityAssetType.USER) || asset.getAssetType().equals(SecurityAssetType.QUOTA)) {
            return asset;
        }
        Whatever.get().check("AccountManager is not the specializer for: " +
                asset.getAssetType(),
                asset.getAssetType().equals(SecurityAssetType.GROUP));
        log.log(Level.FINE, "Narrowing group: " + asset.getName());

        // It's a GROUP - need to populate it
        final LittleGroup.Builder groupBuilder = asset.narrow(LittleGroup.class).copy();
        Map<String, UUID> v_links = om_search.getAssetIdsFrom(groupBuilder.getId(),
                SecurityAssetType.GROUP_MEMBER);

        final List<Asset> v_link_assets = om_search.getAssets(v_links.values());

        /** 
         * This check not true after a member group/user has been deleted.
         * Cleanup takes place at update time for this group.
         *
        Whatever.get().check ( "got expected number of assets: " + v_links.size () + " == " + v_link_assets.size (),
        v_links.size () == v_link_assets.size () 
        );
         */
        log.log(Level.FINE, "Group: " + asset.getName() + " found " + v_link_assets.size() +
                " children under " + asset.getId() + " of type " +
                SecurityAssetType.GROUP_MEMBER);


        final Set<UUID> v_members = new HashSet<UUID>();
        for (Asset a_link : v_link_assets) {
            log.log(Level.FINE, "Got link UUID " + a_link.getToId());
            v_members.add(a_link.getToId());
        }

        // Recursion here!
        final List<Asset> v_member_assets = om_search.getAssets(v_members);

        for (Asset a_member : v_member_assets) {
            log.log(Level.FINE, "adding " + a_member.getName() + " to " +
                    groupBuilder.getName());
            groupBuilder.add(a_member.narrow(LittlePrincipal.class));
        }

        if (groupBuilder.getName().equals(AccountManager.LITTLEWARE_ADMIN_GROUP)) {
            // then add the admin principal
            groupBuilder.add(om_search.getAsset(AccountManager.UUID_ADMIN).get().narrow(LittleUser.class));
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
                final LittleUser p_admin = om_search.getByName(AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER).get().narrow();
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
     * @param p_group to add to
     * @param p_member to add to group
     * @param m_asset manager to use to save the data
     * @return the new group-member asset
     */
    public Asset addMemberToGroup(LittleGroup p_group, LittlePrincipal p_member,
            AssetManager m_asset) throws BaseException, AssetException, GeneralSecurityException,
            RemoteException {
        return m_asset.saveAsset(
                SecurityAssetType.GROUP_MEMBER.create().
                parent(p_group).toId(p_member.getId()).
                name(p_member.getName()).
                comment("Member of group: " + p_member.getName()).
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
        if (SecurityAssetType.USER.equals(asset.getAssetType())) {
            // We need to setup a quota
            final LittleUser caller = provideCaller.get();
            Quota a_caller_quota = om_quota.getQuota(caller, om_search);
            if (null != a_caller_quota) {
                final Quota.Builder quotaBuilder = a_caller_quota.copy();

                quotaBuilder.setId(UUID.randomUUID());
                quotaBuilder.setOwnerId( AccountManager.UUID_ADMIN);
                quotaBuilder.setFromId(asset.getId());
                quotaBuilder.setToId(a_caller_quota.getId());
                om_asset.saveAsset(quotaBuilder.build(), "New quota");
            }
            // Add this frickjack to the everybody group
            {
                final LittleGroup everybody = om_search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP, SecurityAssetType.GROUP).get().narrow();

                try {
                    PrivilegedExceptionAction act_add2group = new AddToGroupAction(everybody,
                            asset.narrow(LittlePrincipal.class),
                            om_asset);
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

        } else if (SecurityAssetType.GROUP.equals(asset.getAssetType())) {
            postUpdateCallback(null, asset);
        }
    }

    /**
     * Delete group-member links to/from when a group or user gets deleted
     */
    @Override
    public void postDeleteCallback(Asset a_deleted ) throws BaseException, AssetException,
            GeneralSecurityException, RemoteException {
        if (SecurityAssetType.GROUP.equals(a_deleted.getAssetType()) || SecurityAssetType.USER.equals(a_deleted.getAssetType())) {
            Set<UUID> vChildren = new HashSet<UUID>();
            vChildren.addAll(
                    om_search.getAssetIdsFrom(a_deleted.getId(),
                    SecurityAssetType.GROUP_MEMBER).values());
            vChildren.addAll(
                    om_search.getAssetIdsTo(a_deleted.getId(),
                    SecurityAssetType.GROUP_MEMBER));

            final List<Asset> v_member_links = om_search.getAssets(vChildren);
            for (Asset p_link : v_member_links) {
                om_asset.deleteAsset(p_link.getId(), "cleaning up deleted principal");
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
        if (SecurityAssetType.GROUP.equals(a_now.getAssetType())) {
            final LittleGroup group = a_now.narrow(LittleGroup.class);
            log.log(Level.FINE, "Update callback running for " + group.getName());
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
                Map<String, UUID> v_children = om_search.getAssetIdsFrom(group.getId(),
                        SecurityAssetType.GROUP_MEMBER);
                final List<Asset> v_member_links = om_search.getAssets(v_children.values());
                for (Asset a_link : v_member_links) {
                    if (!memberSet.contains(a_link.getToId())) {
                        om_asset.deleteAsset(a_link.getId(), "member no longer in group");
                    }
                }
            }

            // Add the new members to the group
            for (LittlePrincipal member : v_add) {
                log.log(Level.FINE, "Adding " + member.getName() + " to group " + group.getName());
                addMemberToGroup(group, member, om_asset);
            }
        }
    }
}

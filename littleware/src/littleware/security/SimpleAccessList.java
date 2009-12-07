/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import java.security.acl.*;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.base.AssertionFailedException;

/**
 * Simple implementation of ACL.
 */
public class SimpleAccessList extends SimpleAsset implements LittleAcl {

    private static Logger olog_generic = Logger.getLogger("littleware.security.SimpleAccessList");
    // Map from principal to AclEntry pair - positive and negative AclEntry instances -
    private Map<Principal, AclEntry> ov_positive_user_entries = new HashMap<Principal, AclEntry>();
    private Map<Principal, AclEntry> ov_negative_user_entries = new HashMap<Principal, AclEntry>();
    private Map<Principal, AclEntry> ov_positive_group_entries = new HashMap<Principal, AclEntry>();
    private Map<Principal, AclEntry> ov_negative_group_entries = new HashMap<Principal, AclEntry>();

    /**
     * Do nothing constructor - needed for serializable, etc.
     */
    public SimpleAccessList() {
        setAssetType(SecurityAssetType.ACL);
    }

    @Override
    public void setName(Principal p_caller, String s_name) {
        this.setName(s_name);
    }

    /**
     * Get enumeration view of the ACL entries.
     * The returned entries are read-only - must clone()
     * to get a modifiable version.
     */
    @Override
    public Enumeration<AclEntry> entries() {
        List<AclEntry> v_entries = new ArrayList<AclEntry>();

        v_entries.addAll(ov_positive_user_entries.values());
        v_entries.addAll(ov_negative_user_entries.values());
        v_entries.addAll(ov_positive_group_entries.values());
        v_entries.addAll(ov_negative_group_entries.values());
        return Collections.enumeration(v_entries);
    }

    @Override
    public boolean checkPermission(Principal p_user, Permission perm_access) {
        if (ov_negative_user_entries.containsKey(p_user) && ((AclEntry) ov_negative_user_entries.get(p_user)).checkPermission(perm_access)) {
            return false;
        } else if (ov_positive_user_entries.containsKey(p_user) && ((AclEntry) ov_positive_user_entries.get(p_user)).checkPermission(perm_access)) {
            return true;
        } else {
            // Loop over all the groups
            for (Iterator<Principal> r_i = ov_negative_group_entries.keySet().iterator();
                    r_i.hasNext();) {
                Group p_group = (Group) r_i.next();
                if (p_group.isMember(p_user) && ((AclEntry) ov_negative_group_entries.get(p_group)).checkPermission(perm_access)) {
                    return false;
                }
            }

            olog_generic.log(Level.FINE, "Checking " + p_user.getName() + " permission on ACL " +
                    this.getName());

            for (Iterator<Map.Entry<Principal, AclEntry>> r_i = ov_positive_group_entries.entrySet().iterator();
                    r_i.hasNext();) {
                Map.Entry<Principal, AclEntry> x_entry = r_i.next();
                Group p_group = (Group) x_entry.getKey();
                boolean b_member = p_group.isMember(p_user);

                olog_generic.log(Level.FINE, "Checking " + p_user.getName() + " membership in group " +
                        p_group.getName() + ": " + b_member);

                if (b_member &&
                        x_entry.getValue().checkPermission(perm_access)) {
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public Enumeration<Permission> getPermissions(Principal x_principal) {
        Set<Permission> v_perms = new HashSet<Permission>();

        // Build up the list of positive group permissions for this principal
        for (Iterator<Principal> r_i = ov_positive_group_entries.keySet().iterator();
                r_i.hasNext();) {
            Group p_group = (Group) r_i.next();
            if (p_group.isMember(x_principal)) {
                v_perms.addAll(Collections.list(
                        ((AclEntry) ov_positive_group_entries.get(p_group)).permissions()));
            }
        }
        // Subtract out the negative group permissions
        for (Iterator<Principal> r_i = ov_negative_group_entries.keySet().iterator();
                r_i.hasNext();) {
            Group p_group = (Group) r_i.next();
            if (p_group.isMember(x_principal)) {
                v_perms.removeAll(Collections.list(
                        ((AclEntry) ov_negative_group_entries.get(p_group)).permissions()));
            }
        }

        // Add in the postive user permissions
        AclEntry x_lookup = ov_positive_user_entries.get(x_principal);
        if (null != x_lookup) {
            v_perms.addAll(Collections.list(x_lookup.permissions()));
        }

        x_lookup = ov_negative_user_entries.get(x_principal);
        if (null != x_lookup) {
            v_perms.removeAll(Collections.list(((AclEntry) x_lookup).permissions()));
        }

        return Collections.enumeration(v_perms);
    }

    /**
     * Get the map that the given AclEntry for the given principal ought to belong in.
     *
     * @param p_user that the entry applies to
     * @param b_negative set true if we want the negative entry
     * @return one of the internal ov_ maps
     */
    private Map<Principal, AclEntry> getCacheForEntry(Principal p_entry,
            boolean b_is_negative) {
        if (b_is_negative) { // x_entry.isNegative () )
            if (p_entry instanceof Group) {
                return ov_negative_group_entries;
            }
            return ov_negative_user_entries;
        } else {
            if (p_entry instanceof Group) {
                return ov_positive_group_entries;
            }
            return ov_positive_user_entries;
        }
    }

    /**
     * Get the map that the AclEntry for the given principal ought to belong in.
     *
     * @param x_entry that we want to add or remove
     * @return one of the internal ov_ maps
     */
    private Map<Principal, AclEntry> getCacheForEntry(AclEntry x_entry) {
        return getCacheForEntry(x_entry.getPrincipal(), x_entry.isNegative());
    }

    @Override
    public LittleAclEntry getEntry(Principal p_user, boolean b_negative) {
        Map<Principal, AclEntry> v_entries = getCacheForEntry(p_user, b_negative);
        return (LittleAclEntry) v_entries.get(p_user);
    }

    @Override
    public boolean addEntry(Principal p_caller, AclEntry x_entry) {
        return addEntry((LittleAclEntry) x_entry);
    }

    @Override
    public boolean addEntry(LittleAclEntry x_entry) {
        Map<Principal, AclEntry> v_edit = getCacheForEntry(x_entry);
        Principal p_user = x_entry.getPrincipal();

        if (!(x_entry instanceof SimpleAclEntry)) {
            throw new AssertionFailedException("Entry must be instance of SimpleAclEntry");
        }

        if (v_edit.containsKey(p_user)) {
            return false;
        }

        v_edit.put(x_entry.getPrincipal(), x_entry);
        //((SimpleAclEntry) x_entry).setReadOnly ();
        return true;
    }

    @Override
    public boolean removeEntry(Principal p_caller, AclEntry x_entry) {
        return removeEntry((LittleAclEntry) x_entry);
    }

    @Override
    public boolean removeEntry(LittleAclEntry x_entry) {
        Map<Principal, AclEntry> v_edit = getCacheForEntry(x_entry);
        Principal p_user = x_entry.getPrincipal();

        return (null != v_edit.remove(p_user));
    }

    @Override
    public void clearEntries() {
        ov_positive_user_entries.clear();
        ov_negative_user_entries.clear();
        ov_positive_group_entries.clear();
        ov_negative_group_entries.clear();
    }

    @Override
    public boolean isOwner(Principal p_owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean deleteOwner(Principal p_caller, Principal p_owner) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addOwner(Principal p_caller, Principal p_owner) {
        throw new UnsupportedOperationException();
    }

    /**
     * Return a simple copy of this object - except setup new empty
     * member sets.
     */
    @Override
    public SimpleAccessList clone() {
        SimpleAccessList acl_copy = (SimpleAccessList) super.clone();
        acl_copy.ov_positive_user_entries = new HashMap<Principal, AclEntry>();
        acl_copy.ov_negative_user_entries = new HashMap<Principal, AclEntry>();
        acl_copy.ov_positive_group_entries = new HashMap<Principal, AclEntry>();
        acl_copy.ov_negative_group_entries = new HashMap<Principal, AclEntry>();
        return acl_copy;
    }

    /**
     * Internal utility to sync AclEntry maps
     *
     * @param map_syncto to clear and copy data into
     * @param map_syncfrom to copy
     */
    private void mapSync(Map<Principal, AclEntry> map_syncto,
            Map<Principal, AclEntry> map_syncfrom) {
        map_syncto.clear();

        for (Map.Entry<Principal, AclEntry> entry_x : map_syncfrom.entrySet()) {
            LittleAclEntry acle_new = SecurityAssetType.ACL_ENTRY.create();
            acle_new.sync((LittleAclEntry) entry_x.getValue());
            map_syncto.put(entry_x.getKey(), acle_new);
        }
    }

    @Override
    public void sync(Asset a_copy_source) {
        if (this == a_copy_source) {
            return;
        }
        super.sync(a_copy_source);
        SimpleAccessList acl_copy_source = (SimpleAccessList) a_copy_source;

        /**
         * Need to decouple copy's AclEntry from source's AclEntry,
         * so can edit each independently.
         */
        mapSync(ov_positive_user_entries, acl_copy_source.ov_positive_user_entries);
        mapSync(ov_negative_user_entries, acl_copy_source.ov_negative_user_entries);
        mapSync(ov_positive_group_entries, acl_copy_source.ov_positive_group_entries);
        mapSync(ov_negative_group_entries, acl_copy_source.ov_negative_group_entries);
    }

    @Override
    public String toString() {
        return "ACL " + this.getName() + " (" + this.getObjectId() + ")";
    }
}



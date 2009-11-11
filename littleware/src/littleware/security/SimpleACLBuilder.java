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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.security.acl.*;
import java.security.Principal;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.base.Maybe;

/**
 * Simple implementation of ACL.
 */
public class SimpleACLBuilder extends SimpleAssetBuilder implements LittleAcl.Builder {

    private static Logger log = Logger.getLogger(SimpleACLBuilder.class.getName());

    /**
     * Do nothing constructor - needed for serializable, etc.
     */
    public SimpleACLBuilder() {
        super(SecurityAssetType.ACL);
    }

    private class ACL extends SimpleAssetBuilder.SimpleAsset implements LittleAcl {
        private Map<Principal, AclEntry> positiveUserEntries;
        private Map<Principal, AclEntry> negativeUserEntries;
        private Map<Principal, AclEntry> positiveGroupEntries;
        private Map<Principal, AclEntry> negativeGroupEntries;

        /** For serialization */
        public ACL() {}

        public ACL(SimpleACLBuilder builder,
                Collection<LittleAclEntry> entries ) {
            super( builder );
            final ImmutableMap.Builder<Principal,AclEntry> positiveUserBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<Principal,AclEntry> positiveGroupBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<Principal,AclEntry> negativeUserBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<Principal,AclEntry> negativeGroupBuilder = ImmutableMap.builder();

            for( LittleAclEntry entry : entries ) {
                if ( entry.isNegative() ) {
                    if ( entry.getPrincipal() instanceof LittleUser ) {
                        negativeUserBuilder.put( entry.getPrincipal(), entry);
                    } else {
                        negativeGroupBuilder.put( entry.getPrincipal(), entry );
                    }
                } else {
                    if ( entry.getPrincipal() instanceof LittleUser ) {
                        positiveUserBuilder.put( entry.getPrincipal(), entry);
                    } else {
                        positiveGroupBuilder.put( entry.getPrincipal(), entry );
                    }
                }
            }
            this.positiveUserEntries = positiveUserBuilder.build();
            this.positiveGroupEntries = positiveGroupBuilder.build();
            this.negativeUserEntries = negativeUserBuilder.build();
            this.negativeGroupEntries = negativeGroupBuilder.build();
        }

        @Override
        public void setName(Principal p_caller, String s_name) {
            throw new UnsupportedOperationException();
        }

        /**
         * Get enumeration view of the ACL entries.
         * The returned entries are read-only - must clone()
         * to get a modifiable version.
         */
        @Override
        public Enumeration<AclEntry> entries() {
            List<AclEntry> v_entries = new ArrayList<AclEntry>();

            v_entries.addAll(positiveUserEntries.values());
            v_entries.addAll(negativeUserEntries.values());
            v_entries.addAll(positiveGroupEntries.values());
            v_entries.addAll(negativeGroupEntries.values());
            return Collections.enumeration(v_entries);
        }

        @Override
        public boolean checkPermission(Principal p_user, Permission perm_access) {
            if (negativeUserEntries.containsKey(p_user) && ((AclEntry) negativeUserEntries.get(p_user)).checkPermission(perm_access)) {
                return false;
            } else if (positiveUserEntries.containsKey(p_user) && ((AclEntry) positiveUserEntries.get(p_user)).checkPermission(perm_access)) {
                return true;
            } else {
                // Loop over all the groups
                for (Iterator<Principal> r_i = negativeGroupEntries.keySet().iterator();
                        r_i.hasNext();) {
                    Group p_group = (Group) r_i.next();
                    if (p_group.isMember(p_user) && ((AclEntry) negativeGroupEntries.get(p_group)).checkPermission(perm_access)) {
                        return false;
                    }
                }

                log.log(Level.FINE, "Checking " + p_user.getName() + " permission on ACL " +
                        this.getName());

                for (Iterator<Map.Entry<Principal, AclEntry>> r_i = positiveGroupEntries.entrySet().iterator();
                        r_i.hasNext();) {
                    Map.Entry<Principal, AclEntry> x_entry = r_i.next();
                    Group p_group = (Group) x_entry.getKey();
                    final boolean isMember = p_group.isMember(p_user);

                    log.log(Level.FINE, "Checking " + p_user.getName() + " membership in group " +
                            p_group.getName() + ": " + isMember);

                    if (isMember &&
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
            for (Iterator<Principal> r_i = positiveGroupEntries.keySet().iterator();
                    r_i.hasNext();) {
                Group p_group = (Group) r_i.next();
                if (p_group.isMember(x_principal)) {
                    v_perms.addAll(Collections.list(
                            ((AclEntry) positiveGroupEntries.get(p_group)).permissions()));
                }
            }
            // Subtract out the negative group permissions
            for (Iterator<Principal> r_i = negativeGroupEntries.keySet().iterator();
                    r_i.hasNext();) {
                Group p_group = (Group) r_i.next();
                if (p_group.isMember(x_principal)) {
                    v_perms.removeAll(Collections.list(
                            ((AclEntry) negativeGroupEntries.get(p_group)).permissions()));
                }
            }

            // Add in the postive user permissions
            AclEntry x_lookup = positiveUserEntries.get(x_principal);
            if (null != x_lookup) {
                v_perms.addAll(Collections.list(x_lookup.permissions()));
            }

            x_lookup = negativeUserEntries.get(x_principal);
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
        private Map<Principal, AclEntry> getCacheForEntry(Principal principal,
                boolean isNegative) {
            if (isNegative) { // x_entry.isNegative () )
                if (principal instanceof Group) {
                    return negativeGroupEntries;
                }
                return negativeUserEntries;
            } else {
                if (principal instanceof Group) {
                    return positiveGroupEntries;
                }
                return positiveUserEntries;
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
        public Maybe<LittleAclEntry> getEntry(Principal principal, boolean negative) {
            final Map<Principal, AclEntry> entryMap = getCacheForEntry(principal, negative);
            return Maybe.emptyIfNull( (LittleAclEntry) entryMap.get(principal) );
        }

        @Override
        public boolean addEntry(Principal p_caller, AclEntry x_entry) {
            throw new UnsupportedOperationException();
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

        @Override
        public String toString() {
            return "ACL " + this.getName() + " (" + this.getId() + ")";
        }

        @Override
        public boolean removeEntry(Principal caller, AclEntry entry) throws NotOwnerException {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public Builder copy() {
            return (Builder) super.copy();
        }
    }

    final ImmutableSet.Builder<LittleAclEntry>  entrySetBuilder = ImmutableSet.builder();


    @Override
    public boolean addEntry(LittleAclEntry entry) {
        if ( ! entry.getFromId().equals( getId() ) ) {
            throw new IllegalArgumentException( "Entry does not link from new ACL" );
        }
        entrySetBuilder.add(entry);
        return true;
    }



    @Override
    public LittleAcl.Builder copy(Asset source) {
        super.copy(source);
        final LittleAcl acl = source.narrow();
        for( final Enumeration<AclEntry> it = acl.entries();
             it.hasMoreElements();
        ) {
            entrySetBuilder.add( (LittleAclEntry) it.nextElement() );
        }
        return this;
    }

    @Override
    public LittleAcl build() {
        throw new UnsupportedOperationException("Not supported yet.");
    }
}



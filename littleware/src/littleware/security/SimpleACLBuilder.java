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
import java.security.acl.Permission;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.base.Maybe;
import littleware.security.LittleAcl.Builder;

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

    private static class AclAsset extends SimpleAssetBuilder.SimpleAsset implements LittleAcl {
        private Map<LittlePrincipal, LittleAclEntry> positiveUserEntries;
        private Map<LittlePrincipal, LittleAclEntry> negativeUserEntries;
        private Map<LittleGroup, LittleAclEntry> positiveGroupEntries;
        private Map<LittleGroup, LittleAclEntry> negativeGroupEntries;
        private Collection<LittleAclEntry> entries;

        /** For serialization */
        public AclAsset() {}

        public AclAsset(SimpleACLBuilder builder,
                Collection<LittleAclEntry> entries ) {
            super( builder );
            final ImmutableMap.Builder<LittlePrincipal,LittleAclEntry> positiveUserBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<LittleGroup,LittleAclEntry> positiveGroupBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<LittlePrincipal,LittleAclEntry> negativeUserBuilder = ImmutableMap.builder();
            final ImmutableMap.Builder<LittleGroup,LittleAclEntry> negativeGroupBuilder = ImmutableMap.builder();

            for( LittleAclEntry entry : entries ) {
                if ( entry.isNegative() ) {
                    if ( entry.getPrincipal() instanceof LittleUser ) {
                        negativeUserBuilder.put( entry.getPrincipal(), entry);
                    } else {
                        negativeGroupBuilder.put( (LittleGroup) entry.getPrincipal(), entry );
                    }
                } else {
                    if ( entry.getPrincipal() instanceof LittleUser ) {
                        positiveUserBuilder.put( entry.getPrincipal(), entry);
                    } else {
                        positiveGroupBuilder.put( (LittleGroup) entry.getPrincipal(), entry );
                    }
                }
            }
            this.positiveUserEntries = positiveUserBuilder.build();
            this.positiveGroupEntries = positiveGroupBuilder.build();
            this.negativeUserEntries = negativeUserBuilder.build();
            this.negativeGroupEntries = negativeGroupBuilder.build();
            this.entries = entries;
        }


        /**
         * Get enumeration view of the ACL entries.
         */
        @Override
        public Enumeration<LittleAclEntry> entries() {
            return Collections.enumeration(entries);
        }
        @Override
        public Collection<LittleAclEntry> getEntries() {
            return entries;
        }

        @Override
        public boolean checkPermission(LittlePrincipal user, Permission permission) {
            if (negativeUserEntries.containsKey(user) && negativeUserEntries.get(user).checkPermission(permission) ) {
                return false;
            } else if (positiveUserEntries.containsKey(user) && positiveUserEntries.get(user).checkPermission(permission)) {
                return true;
            } else {
                // Loop over all the groups
                for (LittleGroup group : negativeGroupEntries.keySet() ) {
                    if (group.isMember(user) && negativeGroupEntries.get(group).checkPermission(permission)) {
                        return false;
                    }
                }

                log.log(Level.FINE, "Checking " + user.getName() + " permission on ACL " +
                        this.getName());

                for (Map.Entry<LittleGroup, LittleAclEntry> entry : positiveGroupEntries.entrySet() ) {
                    final LittleGroup group = entry.getKey().narrow();
                    final boolean isMember = group.isMember(user);

                    log.log(Level.FINE, "Checking " + user.getName() + " membership in group " +
                            group.getName() + ": " + isMember);

                    if (isMember &&
                            entry.getValue().checkPermission(permission)) {
                        return true;
                    }
                }
            }

            return false;
        }

        @Override
        public Collection<Permission> getPermissions(LittlePrincipal principal) {
            final Set<Permission> permissionSet = new HashSet<Permission>();
            // Build up the list of positive group permissions for this principal
            for (LittleGroup group : positiveGroupEntries.keySet() ) {
                if (group.isMember(principal)) {
                    permissionSet.addAll(Collections.list(
                            positiveGroupEntries.get(group).permissions()));
                }
            }
            // Subtract out the negative group permissions
            for (LittleGroup group : negativeGroupEntries.keySet() ) {
                if (group.isMember(principal)) {
                    permissionSet.removeAll(Collections.list(
                            negativeGroupEntries.get(group).permissions()));
                }
            }

            // Add in the postive user permissions
            LittleAclEntry userEntry = positiveUserEntries.get(principal);
            if (null != userEntry) {
                permissionSet.addAll(Collections.list(userEntry.permissions()));
            }

            userEntry = negativeUserEntries.get(principal);
            if (null != userEntry) {
                permissionSet.removeAll(Collections.list( userEntry.permissions()));
            }

            return ImmutableSet.copyOf(permissionSet);
        }

        /**
         * Get the map that the given AclEntry for the given principal ought to belong in.
         *
         * @param p_user that the entry applies to
         * @param b_negative set true if we want the negative entry
         * @return one of the internal ov_ maps
         */
        private Map<? extends LittlePrincipal, LittleAclEntry> getCacheForEntry(LittlePrincipal principal,
                boolean isNegative) {
            if (isNegative) { // x_entry.isNegative () )
                if (principal instanceof LittleGroup) {
                    return negativeGroupEntries;
                }
                return negativeUserEntries;
            } else {
                if (principal instanceof LittleGroup) {
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
        private Map<? extends LittlePrincipal, LittleAclEntry> getCacheForEntry(LittleAclEntry entry) {
            return getCacheForEntry(entry.getPrincipal(), entry.isNegative());
        }

        
        @Override
        public Maybe<LittleAclEntry> getEntry(LittlePrincipal principal, boolean negative) {
            final Map<? extends LittlePrincipal, LittleAclEntry> entryMap = getCacheForEntry(principal, negative);
            return Maybe.emptyIfNull( (LittleAclEntry) entryMap.get(principal) );
        }


        @Override
        public String toString() {
            return "ACL " + this.getName() + " (" + this.getId() + ")";
        }


        @Override
        public Builder copy() {
            return (Builder) super.copy();
        }
    }

    //-------------

    final Set<LittleAclEntry>  entrySet = new HashSet<LittleAclEntry>();


    @Override
    public LittleAcl.Builder addEntry(LittleAclEntry entry) {
        if ( ! entry.getFromId().equals( getId() ) ) {
            throw new IllegalArgumentException( "Entry does not link from new ACL" );
        }
        entrySet.add(entry);
        return this;
    }

    @Override
    public LittleAcl.Builder removeEntry( LittleAclEntry entry ) {
        entrySet.remove(entry);
        return this;
    }




    @Override
    public LittleAcl.Builder copy(Asset source) {
        super.copy(source);
        if ( ! (source instanceof LittleAcl) ) {
            return this;
        }
        final LittleAcl acl = source.narrow();
        for( final Enumeration<LittleAclEntry> it = acl.entries();
             it.hasMoreElements();
        ) {
            entrySet.add( (LittleAclEntry) it.nextElement() );
        }
        return this;
    }

    @Override
    public LittleAcl build() {
        return new AclAsset( this, ImmutableSet.copyOf(entrySet) );
    }

    @Override
    public LittleAcl.Builder name(String value) {
        super.name( value ); return this;
    }

    @Override
    public LittleAcl.Builder creatorId(UUID value) {
        super.creatorId( value ); return this;
    }

    @Override
    public LittleAcl.Builder lastUpdaterId(UUID value) {
        super.lastUpdaterId( value ); return this;
    }

    @Override
    public LittleAcl.Builder aclId(UUID value) {
        super.aclId( value ); return this;
    }

    @Override
    public LittleAcl.Builder ownerId(UUID value) {
        super.ownerId( value ); return this;
    }

    @Override
    public LittleAcl.Builder comment(String value) {
        super.comment( value ); return this;
    }

    @Override
    public LittleAcl.Builder lastUpdate(String value) {
        super.lastUpdate( value ); return this;
    }

    @Override
    public LittleAcl.Builder homeId(UUID value) {
        super.homeId( value ); return this;
    }

    @Override
    public LittleAcl.Builder fromId(UUID value) {
        super.fromId( value ); return this;
    }

    @Override
    public LittleAcl.Builder toId(UUID value) {
        super.toId(value); return this;
    }

    @Override
    public LittleAcl.Builder startDate(Date value) {
        super.startDate( value ); return this;
    }

    @Override
    public LittleAcl.Builder endDate(Date value) {
        super.endDate( value ); return this;
    }

    @Override
    public LittleAcl.Builder createDate(Date value) {
        super.createDate( value ); return this;
    }

    @Override
    public LittleAcl.Builder lastUpdateDate(Date value) {
        super.lastUpdateDate( value ); return this;
    }

    @Override
    public LittleAcl.Builder value(float value) {
        super.value( value ); return this;
    }

    @Override
    public LittleAcl.Builder state(int value) {
        super.state(value); return this;
    }

    @Override
    public LittleAcl.Builder transaction(long value) {
        super.transaction( value ); return this;
    }

    @Override
    public Builder data(String value) {
        super.data( value ); return this;
    }


}



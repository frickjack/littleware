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

import com.google.common.collect.ImmutableSet;

import com.google.common.collect.Sets;
import java.security.Principal;
import java.util.*;

import littleware.asset.SimpleAssetBuilder;

/**
 * Implement java.security.Group interface for tracking groups as principles in 
 * the littleware framework.
 * Littleware requires that user-names and group-names be globally
 * unique across both sets.
 * Permission protected class - clients should only be able to create
 * this thing via a littleware.db.GroupManager instance.
 * The members of group.littleware.administrator, the group
 * creator, X for group.X groups, and the group's owner 
 * (see editGroup) are the owners of a group.
 */
public class GroupBuilder extends SimpleAssetBuilder implements LittleGroup.Builder {

    private ImmutableSet.Builder<LittlePrincipal> memberBuilder = ImmutableSet.builder();

    /**
     * Do nothing internal constructor for NULL-group needed by SimpleOwner
     * and java.io.Serializable
     */
    public GroupBuilder() {
        super(SecurityAssetType.GROUP);
    }

    @Override
    public LittleGroup.Builder  add( LittlePrincipal principal ) {
        memberBuilder.add(principal);
        return this;
    }

    @Override
    public LittleGroup.Builder  remove( LittlePrincipal principal ) {
        final ImmutableSet.Builder<LittlePrincipal> builder = ImmutableSet.builder();
        builder.addAll(
                Sets.difference(memberBuilder.build(), Collections.singleton(principal))
                );
        memberBuilder = builder;
        return this;
    }


    @Override
    public LittleGroup.Builder  addAll( Collection<? extends LittlePrincipal> principalSet ) {
        for ( LittlePrincipal principal : principalSet ) {
            memberBuilder.add(principal);
        }
        return this;
    }

    @Override
    public LittleGroup build() {
       return new GroupAsset( this, memberBuilder.build() );
    }

    private static class GroupAsset extends SimpleAssetBuilder.SimpleAsset implements LittleGroup {
        // Internal dynamic lookup cache - saves results of one lookup for reuse next time
        private Set<LittlePrincipal> memberCache;
        private Set<LittlePrincipal> memberSet;
        
        /**
         * For serialization 
         */
        private GroupAsset() {}

        /**
         * Initializer given reference to group owner
         *
         * @param s_name of the group
         * @param u_id of the group
         * @param s_comment attached to the group
         */
        public GroupAsset(GroupBuilder builder, Set<LittlePrincipal> memberSet) {
            super(builder);
            this.memberSet = memberSet;
            final ImmutableSet.Builder<LittlePrincipal> cacheBuilder = ImmutableSet.builder();
            cacheBuilder.addAll(memberSet);
            for ( LittlePrincipal principal : memberSet ) {
                if ( principal instanceof LittleGroup ) {
                    cacheBuilder.addAll( ((GroupAsset) principal).memberCache );
                }
            }
            memberCache = cacheBuilder.build();
        }

        /**
         * Does a closure on the freakin' subgroups to search for the requested member.
         * Caches data from the search in an internal cache that gets
         * flushed if a 'remove' operation gets performed.
         * Method is syncrhonized to avoid issues manipulating that internal cache.
         */
        @Override
        public boolean isMember(Principal member) {
            return memberCache.contains( (LittlePrincipal) member);
        }

        /**
         * Get an enumeration of the group members
         *
         * @return enumeration
         */
        @Override
        public Enumeration<? extends Principal> members() {
            return Collections.enumeration( memberSet );
        }

        @Override
        public boolean addMember(Principal user) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public boolean removeMember(Principal user) {
            throw new UnsupportedOperationException("Not supported yet.");
        }

        @Override
        public LittleGroup.Builder copy() {
             final LittleGroup.Builder builder = (Builder) super.copy();
             return builder.addAll( memberSet );
        }
    }


}


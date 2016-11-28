package littleware.security.internal;


import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.Set;
import java.util.UUID;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.Asset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.LittleGroup.Builder;
import littleware.security.LittlePrincipal;

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
public class GroupBuilder extends AbstractAssetBuilder<LittleGroup.Builder> implements LittleGroup.Builder {
    private ImmutableSet.Builder<LittlePrincipal> memberBuilder = ImmutableSet.builder();

    /**
     * Do nothing internal constructor for NULL-group needed by SimpleOwner
     * and java.io.Serializable
     */
    public GroupBuilder() {
        super(LittleGroup.GROUP_TYPE);
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
        if ( getId().equals( AccountManager.UUID_EVERYBODY_GROUP ) ) {
            return SimpleEverybody.singleton;
        } else {
            return new GroupAsset( this, memberBuilder.build() );
        }
    }

    @Override
    public LittleGroup.Builder copy( Asset source ) {
        super.copy( source );
        if ( ! (source instanceof GroupAsset) ) {
            return this;
        }
        return addAll( ((GroupAsset) source).memberSet );
    }

    @Override
    public LittleGroup.Builder name(String value) {
        super.name( value ); return this;
    }

    @Override
    public LittleGroup.Builder creatorId(UUID value) {
        super.creatorId( value ); return this;
    }

    @Override
    public LittleGroup.Builder lastUpdaterId(UUID value) {
        super.lastUpdaterId( value ); return this;
    }

    @Override
    public LittleGroup.Builder aclId(UUID value) {
        super.aclId( value ); return this;
    }

    @Override
    public LittleGroup.Builder ownerId(UUID value) {
        super.ownerId( value ); return this;
    }

    @Override
    public LittleGroup.Builder comment(String value) {
        super.comment( value ); return this;
    }

    @Override
    public LittleGroup.Builder lastUpdate(String value) {
        super.lastUpdate( value ); return this;
    }

    @Override
    public LittleGroup.Builder homeId(UUID value) {
        super.homeId( value ); return this;
    }

    @Override
    public LittleGroup.Builder fromId(UUID value) {
        super.fromId( value ); return this;
    }

    @Override
    public LittleGroup.Builder toId(UUID value) {
        super.toId(value); return this;
    }

    @Override
    public LittleGroup.Builder startDate(Date value) {
        super.startDate( value ); return this;
    }

    @Override
    public LittleGroup.Builder endDate(Date value) {
        super.endDate( value ); return this;
    }

    @Override
    public LittleGroup.Builder createDate(Date value) {
        super.createDate( value ); return this;
    }

    @Override
    public LittleGroup.Builder lastUpdateDate(Date value) {
        super.lastUpdateDate( value ); return this;
    }

    @Override
    public LittleGroup.Builder value(float value) {
        super.value( value ); return this;
    }

    @Override
    public LittleGroup.Builder state(int value) {
        super.state(value); return this;
    }

    @Override
    public LittleGroup.Builder timestamp(long value) {
        super.timestamp( value ); return this;
    }

    @Override
    public Builder data(String value) {
        super.data( value ); return this;
    }



    private static class GroupAsset extends AbstractAsset implements LittleGroup {
        // Internal dynamic lookup cache - saves results of one lookup for reuse next time
        private Set<LittlePrincipal> memberCache;
        private Set<LittlePrincipal> memberSet;
        
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
        public boolean isMember( LittlePrincipal member) {
            return memberCache.contains( member);
        }

        /**
         * Get an enumeration of the group members
         *
         * @return enumeration
         */
        @Override
        public Collection<LittlePrincipal> getMembers() {
            return memberSet;
        }


        @Override
        public LittleGroup.Builder copy() {
            return (new GroupBuilder()).copy( this );
        }
    }


}


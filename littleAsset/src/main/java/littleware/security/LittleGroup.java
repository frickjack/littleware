package littleware.security;

import littleware.security.internal.GroupBuilder;
import com.google.inject.ImplementedBy;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;

/**
 * Slight extention of Principal interface
 * to support notion of a principal id and comment
 */
public interface LittleGroup extends LittlePrincipal {

    public Collection<LittlePrincipal> getMembers();
    public boolean isMember( LittlePrincipal member);

    @Override
    public Builder copy();
    
    @ImplementedBy(GroupBuilder.class)
    public interface Builder extends AssetBuilder {
        public Builder  add( LittlePrincipal principal );
        public Builder  remove( LittlePrincipal principal );
        public Builder  addAll( Collection<? extends LittlePrincipal> principalSet );
        @Override
        public Builder name(String value);

        @Override
        public Builder creatorId(UUID value);

        @Override
        public Builder lastUpdaterId(UUID value);

        @Override
        public Builder aclId(UUID value);

        @Override
        public Builder ownerId(UUID value);

        @Override
        public Builder comment(String value);

        @Override
        public Builder lastUpdate(String value);

        @Override
        public Builder data(String value);

        @Override
        public Builder homeId(UUID value);

        @Override
        public Builder fromId(UUID value);

        @Override
        public Builder toId(UUID value);

        @Override
        public Builder startDate(Date value);

        @Override
        public Builder endDate(Date value);

        @Override
        public Builder createDate(Date value);

        @Override
        public Builder lastUpdateDate(Date value);

        @Override
        public Builder value(float value);

        @Override
        public Builder state(int value);

        @Override
        public Builder timestamp(long value);

        @Override
        public Builder copy( Asset value );

        @Override
        public LittleGroup build();
    }
}


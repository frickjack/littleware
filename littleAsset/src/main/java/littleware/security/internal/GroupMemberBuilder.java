package littleware.security.internal;

import java.util.UUID;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.security.LittleGroup;
import littleware.security.LittleGroupMember;
import littleware.security.LittleGroupMember.MemberBuilder;
import littleware.security.LittlePrincipal;


public class GroupMemberBuilder extends AbstractAssetBuilder<LittleGroupMember.MemberBuilder> implements LittleGroupMember.MemberBuilder {

    public GroupMemberBuilder() {
        super( LittleGroupMember.GROUP_MEMBER_TYPE );
    }
    
    @Override
    public UUID getGroupId() {
        return getFromId();
    }

    @Override
    public final void setGroupId(UUID value) {
        groupId( value );
    }

    @Override
    public MemberBuilder groupId(UUID value) {
        return fromId( value );
    }

    @Override
    public UUID getMemberId() {
        return getToId();
    }

    @Override
    public final void setMemberId(UUID value) {
        memberId( value );
    }

    @Override
    public MemberBuilder memberId(UUID value) {
        return toId( value );
    }

    @Override
    public LittleGroupMember build() {
        return new Member( this );
    }

    @Override
    public MemberBuilder group(LittleGroup value) {
        setOwnerId( value.getId() );
        return parent( value );
    }

    @Override
    public MemberBuilder member(LittlePrincipal value) {
        setName( value.getName() );
        return toId( value.getId() );
    }

    //-----------------------------------------------

    private static class Member extends AbstractAsset implements LittleGroupMember {
        
        public Member( GroupMemberBuilder builder ) {
            super( builder );
        }

        @Override
        public UUID getGroupId() {
            return getFromId();
        }

        @Override
        public UUID getMemberId() {
            return getToId();
        }

        @Override
        public MemberBuilder copy() {
            return (new GroupMemberBuilder()).copy( this );
        }

    }

}

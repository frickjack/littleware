/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.security;

import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.LinkAsset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.asset.TreeNode;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;

/**
 * Asset connects a group with its members
 */
public interface LittleGroupMember extends LinkAsset {
    public UUID getGroupId();
    public UUID getMemberId();


    /** GROUP asset type - with AccountManager asset specializer */
    public static final AssetType GROUP_MEMBER_TYPE = new AssetType(
            UUIDFactory.parseUUID("BA50260718204D50BAC6AC711CEE1536"),
            "littleware.GROUP_MEMBER", LinkAsset.LINK_TYPE ){};

    //------------------------------------
    public interface MemberBuilder extends AssetBuilder {
        public UUID getGroupId();
        public void setGroupId( UUID value );
        public MemberBuilder groupId( UUID value );
        public MemberBuilder group( LittleGroup value );

        public UUID getMemberId();
        public void setMemberId( UUID value );
        public MemberBuilder memberId( UUID value );
        public MemberBuilder member( LittlePrincipal value );

        @Override
        public MemberBuilder name(String value);

        @Override
        public MemberBuilder creatorId(UUID value);

        @Override
        public MemberBuilder lastUpdaterId(UUID value);

        @Override
        public MemberBuilder aclId(UUID value);

        @Override
        public MemberBuilder ownerId(UUID value);

        @Override
        public MemberBuilder comment(String value);

        @Override
        public MemberBuilder lastUpdate(String value);

        @Override
        public MemberBuilder homeId(UUID value);

        @Override
        public MemberBuilder createDate(Date value);

        @Override
        public MemberBuilder lastUpdateDate(Date value);


        @Override
        public MemberBuilder timestamp(long value);

        @Override
        public MemberBuilder copy( Asset value );

        @Override
        public LittleGroupMember build();

    }
}

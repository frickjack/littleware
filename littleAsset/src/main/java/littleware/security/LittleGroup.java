/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.security;

import java.util.Collection;
import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.UUIDFactory;

/**
 * Slight extention of Principal interface
 * to support notion of a principal id and comment
 */
public interface LittleGroup extends LittlePrincipal {

    public Collection<LittlePrincipal> getMembers();
    public boolean isMember( LittlePrincipal member);


    /** GROUP asset type - with AccountManager asset specializer */
    public static final AssetType GROUP_TYPE = new AssetType(
            UUIDFactory.parseUUID("FAA894CEC15B49CF8F8EC5C280062776"),
            "littleware.GROUP") {

        @Override
        public Option<AssetType> getSuperType() {
            return Maybe.something((AssetType) LittlePrincipal.PRINCIPAL_TYPE);
        }        
    };

    //------------------------------------------------
    

    public interface Builder extends TreeNode.TreeNodeBuilder {
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
        public Builder homeId(UUID value);

        @Override
        public Builder parentId(UUID value);

        @Override
        public Builder parent( TreeParent parent );
        
        @Override
        public Builder createDate(Date value);

        @Override
        public Builder lastUpdateDate(Date value);


        @Override
        public Builder timestamp(long value);

        @Override
        public Builder copy( Asset value );

        @Override
        public LittleGroup build();
    }
}


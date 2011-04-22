/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security;

import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.base.Maybe;
import littleware.base.UUIDFactory;

/**
 * Specialization of LittlePrincipal with a few
 * more user specific methods not available on groups.
 */
public interface LittleUser extends LittlePrincipal {

    /**
     * Little principal-status class
     */
    public enum Status {

        ACTIVE,
        INACTIVE
    }

    /** Maps getValue() to a UserStatus */
    public Status getStatus();

    @Override
    public Builder copy();

    public interface Builder extends TreeNode.TreeNodeBuilder {

        @Override
        LittleUser build();

        public void setStatus(Status status);

        public Status getStatus();

        public Builder status(Status status);

        @Override
        public Builder parentId(UUID value);

        @Override
        public Builder id(UUID value);

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
        public Builder lastUpdate(String value);

        @Override
        public Builder homeId(UUID value);

        @Override
        public Builder timestamp(long value);

        @Override
        public Builder copy(Asset source);

        /**
         * Sets parentId, homeId, and aclId from parent
         */
        @Override
        public Builder parent(TreeParent parent);
    }
    public static final AssetType USER_TYPE = new AssetType(UUIDFactory.parseUUID("2FAFD5D1074F4BF8A4F01753DBFF4CD5"),
            "littleware.USER", TreeNode.TREE_NODE_TYPE) {

        @Override
        public boolean isAdminToCreate() {
            return true;
        }

        @Override
        public Maybe<AssetType> getSuperType() {
            return Maybe.something((AssetType) LittlePrincipal.PRINCIPAL_TYPE);
        }

        /** Always return true */
        @Override
        public boolean isNameUnique() {
            return true;
        }
    };
}

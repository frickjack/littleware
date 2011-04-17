/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.UUID;
import littleware.base.Maybe;
import littleware.base.UUIDFactory;

/**
 * Asset with a parent
 */
public interface TreeNode extends Asset {

    public UUID getParentId();

    @Override
    public TreeNodeBuilder copy();
    public static final AssetType TREE_NODE_TYPE = new AssetType(
            UUIDFactory.parseUUID("3c766457-d7a7-432b-b6db-281053672204"),
            "littleware.TreeNode");
    
    public interface TreeNodeBuilder extends AssetBuilder {

        @Override
        public TreeNode build();

        public UUID getParentId();

        public void setParentId(UUID value);

        public TreeNodeBuilder parentId(UUID value);

        @Override
        public TreeNodeBuilder id(UUID value);

        @Override
        public TreeNodeBuilder name(String value);

        @Override
        public TreeNodeBuilder creatorId(UUID value);

        @Override
        public TreeNodeBuilder lastUpdaterId(UUID value);

        @Override
        public TreeNodeBuilder aclId(UUID value);

        @Override
        public TreeNodeBuilder ownerId(UUID value);

        @Override
        public TreeNodeBuilder lastUpdate(String value);

        @Override
        public TreeNodeBuilder homeId(UUID value);

        @Override
        public TreeNodeBuilder timestamp(long value);

        /**
         * Sets parentId, homeId, and aclId from parent
         */
        public TreeNodeBuilder parent(TreeNode parent);

        public TreeNodeBuilder parent(LittleHome parent);
    }
}

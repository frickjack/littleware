package littleware.asset;

import java.util.UUID;
import littleware.base.UUIDFactory;

/**
 * Asset with a parent
 */
public interface TreeNode extends TreeParent, TreeChild {

    public static final AssetType TREE_NODE_TYPE = new AssetType(
            UUIDFactory.parseUUID("3c766457-d7a7-432b-b6db-281053672204"),
            "littleware.TreeNode");
    
    public interface TreeNodeBuilder extends TreeChildBuilder {

        @Override
        public TreeNode build();

        @Override
        public UUID getParentId();

        @Override
        public void setParentId(UUID value);

        @Override
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
        @Override
        public TreeNodeBuilder parent(TreeParent parent);

        @Override
        public TreeNodeBuilder comment( String value );

    }
}

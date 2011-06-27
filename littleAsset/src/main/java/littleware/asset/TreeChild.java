/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.UUID;

/**
 * An asset in a tree with a parent implementing TreeParent
 */
public interface TreeChild {
    public UUID getParentId();
    
    public interface TreeChildBuilder extends AssetBuilder {

        @Override
        public TreeNode build();

        public UUID getParentId();

        public void setParentId(UUID value);

        public TreeChildBuilder parentId(UUID value);

        @Override
        public TreeChildBuilder id(UUID value);

        @Override
        public TreeChildBuilder name(String value);

        @Override
        public TreeChildBuilder creatorId(UUID value);

        @Override
        public TreeChildBuilder lastUpdaterId(UUID value);

        @Override
        public TreeChildBuilder aclId(UUID value);

        @Override
        public TreeChildBuilder ownerId(UUID value);

        @Override
        public TreeChildBuilder lastUpdate(String value);

        @Override
        public TreeChildBuilder homeId(UUID value);

        @Override
        public TreeChildBuilder timestamp(long value);

        /**
         * Sets parentId, homeId, and aclId from parent
         */
        public TreeChildBuilder parent(TreeParent parent);

        @Override
        public TreeChildBuilder comment( String value );

    }
    
}

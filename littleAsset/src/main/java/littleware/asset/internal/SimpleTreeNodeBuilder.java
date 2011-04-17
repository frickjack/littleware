/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.asset.internal;

import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.asset.TreeNode;

public class SimpleTreeNodeBuilder extends AbstractAssetBuilder<TreeNode.TreeNodeBuilder> implements TreeNode.TreeNodeBuilder {

    private static class SimpleNode extends AbstractAsset implements TreeNode {
        public SimpleNode() {}
        public SimpleNode( SimpleTreeNodeBuilder builder ) {
            super( builder );
        }

        @Override
        public TreeNodeBuilder copy() {
            return (new SimpleTreeNodeBuilder()).copy( this );
        }

    }

    public SimpleTreeNodeBuilder() {
        super( TreeNode.TREE_NODE_TYPE );
    }
    @Override
    public TreeNode build() {
        return new SimpleNode( this );
    }


}

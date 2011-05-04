/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.internal;

import java.util.UUID;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.asset.TemplateScanner;
import littleware.base.Option;

/**
 * Base class for client and server side TemplateScanner implementations
 */
public abstract class AbstractTemplateScanner implements TemplateScanner {

    protected abstract Option<TreeNode>  loadAsset( UUID parentId, String name );


    public static class Info implements ExistInfo {
        private final TreeParent node;
        private final boolean exists;
        public Info( TreeParent node, boolean exists ) {
            this.node = node;
            this.exists = exists;
        }

        @Override
        public boolean getAssetExists() {
            return exists;
        }

        @Override
        public TreeParent getAsset() {
            return node;
        }
    }

    @Override
    public ExistInfo visit(TreeParent parent, AssetTreeTemplate template) {
        final TreeNode.TreeNodeBuilder builder = template.getBuilder();
        final Option<TreeNode> maybeExists = loadAsset( parent.getId(), builder.getName());
        final TreeNode node;
        if (maybeExists.isEmpty()) {
            // This TreeNode does not yet exist!
            final UUID rememberAclId = builder.getAclId();
            if (null != parent) {
                builder.parent( parent );
                if (null != rememberAclId) {
                    builder.aclId(rememberAclId);
                }
            }
            if (builder.getAssetType().isA(LittleHome.HOME_TYPE)) {
                builder.setParentId(null);
            }
            node = builder.build();
            return new Info( node, false );
        } else {
            node = maybeExists.get().narrow();
            return new Info( node, true );
        }
    }

}

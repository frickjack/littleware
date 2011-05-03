/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.internal;

import com.google.common.collect.ImmutableList;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.UUID;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.TreeParent;
import littleware.asset.client.AssetRef;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.TemplateScanner;
import littleware.base.BaseException;

public class SimpleTemplateScanner implements TemplateScanner {

    AssetSearchManager search = null;

    @Override
    public Collection<AssetInfo> scan(TreeParent parent, AssetTreeTemplate template) throws BaseException, GeneralSecurityException, RemoteException {
        final TreeNode.TreeNodeBuilder builder = template.getBuilder();
        final AssetRef maybeExists = search.getAssetFrom(parent.getId(), builder.getName());
        final ImmutableList.Builder<AssetInfo> resultBuilder = ImmutableList.builder();
        final TreeNode node;
        if (maybeExists.isEmpty()) {
            // This TreeNode does not yet exist!
            final UUID rememberAclId = builder.getAclId();
            if (null != parent) {
                if (parent instanceof TreeNode) {
                    builder.parent(parent.narrow(TreeNode.class));
                } else {
                    builder.parent(parent.narrow(LittleHome.class));
                }
                if (null != rememberAclId) {
                    builder.aclId(rememberAclId);
                }
            }
            if (builder.getAssetType().isA(LittleHome.HOME_TYPE)) {
                builder.setParentId(null);
            }
            node = builder.build();
            resultBuilder.add(new SimpleInfo(node, false));
        } else {
            node = maybeExists.get().narrow();
            resultBuilder.add(new SimpleInfo(node, true));
        }

        for (AssetTreeTemplate child : template.getChildren() ) {
            resultBuilder.addAll( scan( node, child ));
        }
        return resultBuilder.build();
    }

    /**
     * Little POJO bucket holds the asset is a node on the tree,
     * and the exists property states whether or not that
     * asset already exists in the repository.
     */
    public static class SimpleInfo implements TemplateScanner.AssetInfo {

        private final TreeNode asset;
        private final boolean exists;

        public SimpleInfo(TreeNode asset, boolean exists) {
            this.asset = asset;
            this.exists = exists;
        }

        @Override
        public TreeNode getAsset() {
            return asset;
        }

        @Override
        public boolean getAssetExists() {
            return exists;
        }
    }
}

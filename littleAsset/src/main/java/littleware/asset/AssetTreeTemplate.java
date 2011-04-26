/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import littleware.asset.client.AssetSearchManager;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import littleware.base.BaseException;

/**
 * Template mechanism for building asset trees or maybe other things too
 */
public interface AssetTreeTemplate {

    /**
     * Little POJO bucket holds the asset is a node on the tree,
     * and the exists property states whether or not that
     * asset already exists in the repository.
     */
    public interface AssetInfo {

        public TreeNode getAsset();

        public boolean getAssetExists();
    }

    public List<AssetTreeTemplate> getChildren();

    public TreeNode.TreeNodeBuilder getBuilder();

    /**
     * Shortcut for getBuilder.getName
     */
    public String getName();

    /**
     * Shortcut for getBuilder.getAssetType
     */
    public AssetType getType();

    /**
     * Scan the tree defined by this template under the given parent.
     * If name-unique asset already exists under a different parent then
     * just create a link to it, and continue down its subtree
     *
     * @param parent passed to getBuilder.parent but keeping getBulder.getAclId if not null
     * @return collection of nodes that define the subtree under this template
     *      in asset-create safe order -
     *      some nodes may already exist, others may need to be saved to the repo
     */
    public Collection<AssetInfo> visit(TreeNode parent, AssetSearchManager search) throws BaseException, AssetException, GeneralSecurityException, RemoteException;
    public Collection<AssetInfo> visit(LittleHome parent, AssetSearchManager search) throws BaseException, AssetException, GeneralSecurityException, RemoteException;



    // ----------------------------------------------------------

    /**
     * Begin building ...
     */
    public interface TemplateBuilder {

        public ByAssetBuilder assetBuilder(TreeNode.TreeNodeBuilder value);

        /**
         * Shortcut for assetBuilder( GenericAsset.GENERIC.create().name( name ) )
         */
        public ByAssetBuilder assetBuilder(String name);

        public ByPathBuilder path(AssetPath value);

        /**
         * Shared abstract interface
         */
        public interface GenericBuilder {

            public GenericBuilder addChildren(AssetTreeTemplate... children);

            public GenericBuilder addChildren(Collection<? extends AssetTreeTemplate> children);

            public AssetTreeTemplate build();
        }


        /**
         * Build a template that places children under a given asset
         */
        public interface ByAssetBuilder extends GenericBuilder {

            public TreeNode.TreeNodeBuilder getBuilder();

            @Override
            public ByAssetBuilder addChildren(AssetTreeTemplate... children);

            @Override
            public ByAssetBuilder addChildren(Collection<? extends AssetTreeTemplate> children);
        }

        /**
         * Build a template that places children under
         * a given path
         */
        public interface ByPathBuilder extends GenericBuilder {

            public AssetPath getPath();

            @Override
            public ByPathBuilder addChildren(AssetTreeTemplate... children);

            @Override
            public ByPathBuilder addChildren(Collection<? extends AssetTreeTemplate> children);
        }
    }

}

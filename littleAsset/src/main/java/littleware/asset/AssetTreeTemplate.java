/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.Collection;
import java.util.List;

/**
 * Template mechanism for building asset trees or maybe other things too
 */
public interface AssetTreeTemplate {


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

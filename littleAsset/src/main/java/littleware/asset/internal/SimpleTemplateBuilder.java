/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.internal;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.Collection;
import java.util.List;
import littleware.asset.AssetPath;
import littleware.asset.AssetTreeTemplate;
import littleware.asset.AssetType;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.base.validate.ValidationException;

/**
 * Simple implementation of AssetTreeTemplate and supporting interfaces
 */
public class SimpleTemplateBuilder implements AssetTreeTemplate.TemplateBuilder {
    private final Provider<TreeNode.TreeNodeBuilder> nodeProvider;

    @Inject
    public SimpleTemplateBuilder( Provider<TreeNode.TreeNodeBuilder> nodeProvider ) {
        this.nodeProvider = nodeProvider;
    }

    @Override
    public ByAssetBuilder assetBuilder(TreeNode.TreeNodeBuilder value) {
        return new SimpleAssetBuilder(value);
    }

    @Override
    public ByPathBuilder path(AssetPath value) {
        if (value.hasRootBacktrack()) {
            throw new IllegalArgumentException("May not build template for unrooted relative path");
        }
        return new SimplePathBuilder(value, nodeProvider );
    }

    @Override
    public ByAssetBuilder assetBuilder(String name) {
        return assetBuilder( nodeProvider.get().name( name ) );
    }


    public static abstract class SimpleGenericBuilder implements AssetTreeTemplate.TemplateBuilder.GenericBuilder {

        protected final ImmutableList.Builder<AssetTreeTemplate> childListBuilder = ImmutableList.builder();

        @Override
        public GenericBuilder addChildren(AssetTreeTemplate... children) {
            childListBuilder.add(children);
            return this;
        }

        @Override
        public GenericBuilder addChildren(Collection<? extends AssetTreeTemplate> children) {
            childListBuilder.addAll(children);
            return this;
        }
    }

    public static class SimpleAssetBuilder extends SimpleGenericBuilder implements AssetTreeTemplate.TemplateBuilder.ByAssetBuilder {

        private final TreeNodeBuilder builder;

        public SimpleAssetBuilder(TreeNodeBuilder builder) {
            this.builder = builder;
        }

        @Override
        public TreeNodeBuilder getBuilder() {
            return builder;
        }

        @Override
        public AssetTreeTemplate build() {
            ValidationException.validate(null != this.builder, "Builder property must be set");
            final AssetTreeTemplate result = new SimpleTreeTemplate(builder, childListBuilder.build());
            for (AssetTreeTemplate child : result.getChildren()) {
                ValidationException.validate(!child.getBuilder().getAssetType().equals(LittleHome.HOME_TYPE), "Home type asset may only be tree root");
            }
            return result;
        }

        @Override
        public ByAssetBuilder addChildren(AssetTreeTemplate... children) {
            super.addChildren(children);
            return this;
        }

        @Override
        public ByAssetBuilder addChildren(Collection<? extends AssetTreeTemplate> children) {
            super.addChildren(children);
            return this;
        }
    }

    public static class SimplePathBuilder extends SimpleGenericBuilder implements AssetTreeTemplate.TemplateBuilder.ByPathBuilder {
        private final AssetPath path;
        private final Provider<TreeNodeBuilder> nodeProvider;

        public SimplePathBuilder(AssetPath path, Provider<TreeNode.TreeNodeBuilder> nodeProvider ) {
            this.path = path;
            this.nodeProvider = nodeProvider;
        }

        @Override
        public AssetTreeTemplate build() {
            if (!path.hasParent()) {
                throw new IllegalArgumentException( "Template base path references a HOME asset: " + path );
            }
            AssetTreeTemplate result = (new SimpleAssetBuilder(nodeProvider.get().name(path.getBasename()))).addChildren(super.childListBuilder.build()).build();
            AssetPath dir = path.getParent();
            while( dir.hasParent() ) {
                result = (new SimpleAssetBuilder( nodeProvider.get().name( dir.getBasename() ))).addChildren( result ).build();
                dir = dir.getParent();
            }
            return result;
        }

        @Override
        public AssetPath getPath() {
            return path;
        }

        @Override
        public ByPathBuilder addChildren(AssetTreeTemplate... children) {
            super.addChildren(children);
            return this;
        }

        @Override
        public ByPathBuilder addChildren(Collection<? extends AssetTreeTemplate> children) {
            super.addChildren(children);
            return this;
        }
    }

    /**
     * Template mechanism for building asset trees or maybe other things too
     */
    public static class SimpleTreeTemplate implements AssetTreeTemplate {

        private final TreeNodeBuilder builder;

        @Override
        public List<AssetTreeTemplate> getChildren() {
            return children;
        }

        @Override
        public TreeNodeBuilder getBuilder() {
            return builder;
        }

        @Override
        public String getName() {
            return builder.getName();
        }

        @Override
        public AssetType getType() {
            return builder.getAssetType();
        }
        private final List<AssetTreeTemplate> children;

        public SimpleTreeTemplate(TreeNodeBuilder builder, Collection<? extends AssetTreeTemplate> children) {
            this.builder = builder;
            this.children = ImmutableList.copyOf(children);
        }



    }
}

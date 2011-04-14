/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import com.google.common.collect.ImmutableList;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Collection;
import java.util.List;
import java.util.UUID;
import littleware.asset.AssetTreeTemplate.ByAssetBuilder;
import littleware.asset.AssetTreeTemplate.ByPathBuilder;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.validate.ValidationException;

/**
 * Simple implementation of AssetTreeTemplate and supporting interfaces
 */
public class SimpleTemplateBuilder implements AssetTreeTemplate.TemplateBuilder {

    @Override
    public ByAssetBuilder assetBuilder(AssetBuilder value) {
        return new SimpleAssetBuilder(value);
    }

    @Override
    public ByPathBuilder path(AssetPath value) {
        if (value.hasRootBacktrack()) {
            throw new IllegalArgumentException("May not build template for unrooted relative path");
        }
        return new SimplePathBuilder(value);
    }

    @Override
    public ByAssetBuilder assetBuilder(String name) {
        return assetBuilder( AssetType.GENERIC.create().name( name ) );
    }

    public static abstract class SimpleGenericBuilder implements AssetTreeTemplate.GenericBuilder {

        protected final ImmutableList.Builder<AssetTreeTemplate> childBuilder = ImmutableList.builder();

        @Override
        public AssetTreeTemplate.GenericBuilder addChildren(AssetTreeTemplate... children) {
            childBuilder.add(children);
            return this;
        }

        @Override
        public AssetTreeTemplate.GenericBuilder addChildren(Collection<? extends AssetTreeTemplate> children) {
            childBuilder.addAll(children);
            return this;
        }
    }

    public static class SimpleAssetBuilder extends SimpleGenericBuilder implements AssetTreeTemplate.ByAssetBuilder {

        private final AssetBuilder builder;

        public SimpleAssetBuilder(AssetBuilder builder) {
            this.builder = builder;
        }

        @Override
        public AssetBuilder getBuilder() {
            return builder;
        }

        @Override
        public AssetTreeTemplate build() {
            ValidationException.validate(null != this.builder, "Builder property must be set");
            final AssetTreeTemplate result = new SimpleTreeTemplate(builder, childBuilder.build());
            for (AssetTreeTemplate child : result.getChildren()) {
                ValidationException.validate(!child.getBuilder().getAssetType().equals(AssetType.HOME), "Home type asset may only be tree root");
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

    public static class SimplePathBuilder extends SimpleGenericBuilder implements AssetTreeTemplate.ByPathBuilder {

        private final AssetPath path;

        public SimplePathBuilder(AssetPath path) {
            this.path = path;
        }

        @Override
        public AssetTreeTemplate build() {
            if (!path.hasParent()) {
                return (new SimpleAssetBuilder(AssetType.HOME.create().name(path.getBasename()))).addChildren(super.childBuilder.build()).build();
            }
            AssetTreeTemplate result = (new SimpleAssetBuilder(AssetType.GENERIC.create().name(path.getBasename()))).addChildren(super.childBuilder.build()).build();
            AssetPath dir = path.getParent();
            while( dir.hasParent() ) {
                result = (new SimpleAssetBuilder( AssetType.GENERIC.create().name( dir.getBasename() ))).addChildren( result ).build();
            }
            return (new SimpleAssetBuilder(AssetType.HOME.create().name(dir.getBasename()))).addChildren(result).build();
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
     * Little POJO bucket holds the asset is a node on the tree,
     * and the exists property states whether or not that
     * asset already exists in the repository.
     */
    public static class SimpleInfo implements AssetTreeTemplate.AssetInfo {

        private final Asset asset;
        private final boolean exists;

        public SimpleInfo(Asset asset, boolean exists) {
            this.asset = asset;
            this.exists = exists;
        }

        @Override
        public Asset getAsset() {
            return asset;
        }

        @Override
        public boolean getAssetExists() {
            return exists;
        }
    }

    /**
     * Template mechanism for building asset trees or maybe other things too
     */
    public static class SimpleTreeTemplate implements AssetTreeTemplate {

        private final AssetBuilder builder;

        @Override
        public List<AssetTreeTemplate> getChildren() {
            return children;
        }

        @Override
        public AssetBuilder getBuilder() {
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

        public SimpleTreeTemplate(AssetBuilder builder, Collection<? extends AssetTreeTemplate> children) {
            this.builder = builder;
            this.children = ImmutableList.copyOf(children);
        }

        @Override
        public Collection<AssetInfo> visit(Asset parent, AssetSearchManager search) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
            Maybe<Asset> maybe = Maybe.empty();

            if (null != parent) {
                maybe = search.getAssetFrom(parent.getId(), builder.getName());
            }
            if (maybe.isEmpty()) {
                maybe = search.getAsset(builder.getId());
            }
            if (maybe.isEmpty() && builder.getAssetType().isNameUnique()) {
                maybe = search.getByName(builder.getName(), builder.getAssetType());
            }

            final ImmutableList.Builder<AssetInfo> resultBuilder = ImmutableList.builder();
            final Asset asset;
            if (maybe.isEmpty()) {
                final UUID rememberAclId = builder.getAclId();
                if (null != parent) {
                    builder.parent(parent);
                    if ( null != rememberAclId ) {
                        builder.aclId( rememberAclId );
                    }
                }
                if (builder.getAssetType().isA(AssetType.HOME)) {
                    builder.setFromId(null);
                }
                asset = builder.build();
                resultBuilder.add(new SimpleInfo(asset, false));
            } else {
                asset = maybe.get();
                if ((null != parent)
                        && (!asset.getFromId().equals(parent.getId()))) {
                    throw new IllegalArgumentException("Asset already exists under different tree: "
                            + asset.getName() + " (" + asset.getAssetType() + ")");
                }
                resultBuilder.add(new SimpleInfo(asset, true));
            }

            for (AssetTreeTemplate child : children) {
                resultBuilder.addAll(child.visit(asset, search));
            }
            return resultBuilder.build();
        }

        /**
         * Same as visit( null, search )
         */
        @Override
        public Collection<AssetInfo> visit(AssetSearchManager search) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
            return visit(null, search);
        }
    }
}

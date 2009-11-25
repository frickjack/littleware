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

import com.google.common.collect.ImmutableList;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import littleware.base.BaseException;
import littleware.base.Maybe;

/**
 * Template mechanism for building asset trees or maybe other things too
 */
public class AssetTreeTemplate {

    /**
     * Little POJO bucket holds the asset is a node on the tree,
     * and the exists property states whether or not that
     * asset already exists in the repository.
     */
    public static class AssetInfo {

        private final Asset asset;
        private final boolean exists;

        public AssetInfo(Asset asset, boolean exists) {
            this.asset = asset;
            this.exists = exists;
        }

        public Asset getAsset() {
            return asset;
        }

        public boolean getAssetExists() {
            return exists;
        }
    }
    private final String name;
    private final AssetType type;

    public List<AssetTreeTemplate> getChildren() {
        return children;
    }

    public String getName() {
        return name;
    }

    public AssetType getType() {
        return type;
    }
    private final List<AssetTreeTemplate> children;

    public AssetTreeTemplate(String name, AssetTreeTemplate... children) {
        this.name = name;
        this.type = AssetType.GENERIC;
        this.children = new ImmutableList.Builder<AssetTreeTemplate>().addAll(Arrays.asList(children)).build();
    }

    public AssetTreeTemplate(String name, Collection<? extends AssetTreeTemplate> children) {
        this.name = name;
        this.type = AssetType.GENERIC;
        this.children = ImmutableList.copyOf(children);
    }


    public AssetTreeTemplate(String name, AssetType type, AssetTreeTemplate... children) {
        this.name = name;
        this.type = type;
        this.children = new ImmutableList.Builder<AssetTreeTemplate>().addAll(Arrays.asList(children)).build();
    }

    public AssetTreeTemplate( String name, AssetType type, Collection<? extends AssetTreeTemplate> children ) {
        this.name = name;
        this.type = type;
        this.children = ImmutableList.copyOf( children );
    }

    /**
     * Scan the tree defined by this template under the given parent.
     * If name-unique asset already exists under a different parent then
     * just create a link to it, and continue down its subtree
     *
     * @param parent ignored if null
     * @return collection of nodes that define the subtree under this template
     *      in asset-create safe order -
     *      some nodes may already exist, others may need to be saved to the repo
     */
    public Collection<AssetInfo> visit(Asset parent, AssetSearchManager search) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        Maybe<Asset> maybe = Maybe.empty();

        if ( null != parent ) {
            maybe = search.getAssetFrom(parent.getId(), name);
        } 
        if ( maybe.isEmpty() && type.isNameUnique() ) {
            maybe = search.getByName(name, type);
        }
        
        
        final ImmutableList.Builder<AssetInfo> builder = ImmutableList.builder();
        final Asset asset;
        if (maybe.isEmpty()) {
            final AssetBuilder assetBuilder;
            if ( null != parent ) {
                assetBuilder = type.create().parent(parent).name( name );
            } else {
                assetBuilder = type.create();
                assetBuilder.setName( name );
            }
            if ( type.isA( AssetType.HOME ) ) {
                assetBuilder.setFromId(null);
            }
            asset = assetBuilder.build();
            builder.add( new AssetInfo( asset, false ) );
        } else {
            asset = maybe.get();
            if ( (null != parent)
                    && (! asset.getFromId().equals( parent.getId() ) )
            ) {
                throw new IllegalArgumentException ( "Asset already exists under different tree: " +
                        asset.getName() + " (" + asset.getAssetType() + ")"
                        );
            }
            builder.add( new AssetInfo( asset, true ) );
        }

        for (AssetTreeTemplate child : children) {
            builder.addAll( child.visit(asset, search ) );
        }
        return builder.build();
    }
}

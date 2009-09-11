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

    public AssetTreeTemplate(String name, AssetType type, AssetTreeTemplate... children) {
        this.name = name;
        this.type = type;
        this.children = new ImmutableList.Builder<AssetTreeTemplate>().addAll(Arrays.asList(children)).build();
    }

    /**
     * Scan the tree defined by this template under the given parent.
     *
     * @return collection of nodes that define the subtree under this template -
     *      some nodes may already exist, others may need to be saved to the repo
     */
    public Collection<AssetInfo> visit(Asset parent, AssetSearchManager search) throws BaseException, AssetException, GeneralSecurityException, RemoteException {
        final Maybe<Asset> maybe = search.getAssetFrom(parent.getObjectId(), name);
        final Asset asset;
        final ImmutableList.Builder<AssetInfo> builder = ImmutableList.builder();
        if (maybe.isEmpty()) {
            asset = AssetType.createSubfolder(type, name, parent);
            builder.add( new AssetInfo( asset, false ) );
        } else {
            asset = maybe.get();
            builder.add( new AssetInfo( asset, true ) );
        }

        for (AssetTreeTemplate child : children) {
            builder.addAll( child.visit(asset, search ) );
        }
        return builder.build();
    }
}

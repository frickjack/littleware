/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo;

import com.google.common.collect.ImmutableList;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;
import java.io.StringWriter;
import java.lang.reflect.Type;
import java.util.Iterator;
import java.util.List;
import littleware.apps.lgo.LgoAssetList.AssetListBuilder;
import littleware.asset.Asset;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.base.AssertionFailedException;

/**
 * Simple LgoAssetList support
 */
public class SimpleAssetListBuilder implements LgoAssetList.AssetListBuilder {

    private final ImmutableList.Builder<Asset> builder = ImmutableList.builder();
    private final HumanPicklerProvider picklerProvider;

    @Inject
    public SimpleAssetListBuilder(HumanPicklerProvider picklerProvider) {
        this.picklerProvider = picklerProvider;
    }

    @Override
    public AssetListBuilder add(Asset value) {
        builder.add(value);
        return this;
    }

    @Override
    public AssetListBuilder addAll(Iterable<? extends Asset> value) {
        builder.addAll(value);
        return this;
    }

    @Override
    public LgoAssetList build() {
        return new AssetList(builder.build(), picklerProvider);
    }

    public static class AssetList implements LgoAssetList {

        private final ImmutableList<Asset> assetList;
        private final HumanPicklerProvider picklerProvider;

        private AssetList(ImmutableList<Asset> assetList, HumanPicklerProvider picklerProvider) {
            this.assetList = assetList;
            this.picklerProvider = picklerProvider;
        }

        @Override
        public List<Asset> getList() {
            return assetList;
        }

        @Override
        public Iterator<Asset> iterator() {
            return assetList.iterator();
        }

        @Override
        public String toString() {
            final StringWriter writer = new StringWriter();
            try {
                for (Asset scan : assetList) {
                    picklerProvider.get().pickle(scan, writer);
                }
            } catch (Exception ex) {
                throw new AssertionFailedException("Failed pickle", ex);
            }
            return writer.toString();
        }
    }

    /**
     * GsonSerializer for AssetList - register with GsonProvider in LgoActivator
     */
    public static class GsonSerializer implements JsonSerializer<AssetList> {

        @Override
        public JsonElement serialize(AssetList assetList, Type type, JsonSerializationContext jsc) {
            final JsonArray result = new JsonArray();
            for( Asset scan : assetList ) {
                result.add( jsc.serialize(scan, Asset.class ));
            }
            return result;
        }

    }
}

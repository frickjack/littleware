/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.inject.Provider;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;

/**
 * Littleware asset serializer/deserializer works in conjuction with an internal
 * delegator that first pickles the AssetType, then delegates the remaining work
 * to a type specific handler.  Interface designed to allow sharing between subtypes/whatever.
 */
public interface GsonAssetAdapter {
    public AssetType getAssetType();
    public Provider<? extends AssetBuilder>  getBuilderFactory();
    
    public JsonObject serialize(Asset assetIn, JsonObject json, JsonSerializationContext jsc);
    public AssetBuilder deserialize( AssetBuilder assetBuilder, JsonObject json, JsonDeserializationContext jdc) throws JsonParseException;
}

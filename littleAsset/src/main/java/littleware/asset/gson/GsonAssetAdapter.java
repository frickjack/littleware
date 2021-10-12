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
    
    public JsonObject serialize(Asset assetIn, JsonSerializationContext jsc);
    public AssetBuilder deserialize( 
            JsonObject json, 
            JsonDeserializationContext jdc,
            LittleGsonResolver resolver
            ) throws JsonParseException;
}

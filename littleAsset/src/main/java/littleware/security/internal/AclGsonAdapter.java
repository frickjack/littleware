package littleware.security.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.gson.AbstractAssetAdapter;
import littleware.asset.gson.GsonAssetAdapter;
import littleware.asset.gson.LittleGsonResolver;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.security.AccountManager;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittlePrincipal;

public class AclGsonAdapter extends AbstractAssetAdapter {

    private final GsonAssetAdapter entryAdapter;

    @Inject
    public AclGsonAdapter(Provider<LittleAcl.Builder> builderFactory,
            Provider<LittleAclEntry.Builder> entryFactory) {
        super(LittleAcl.ACL_TYPE, builderFactory);
        entryAdapter = new AbstractAssetAdapter(
                LittleAclEntry.ACL_ENTRY, entryFactory) {
        };
    }

    @Override
    public JsonObject serialize(Asset assetIn, 
            JsonSerializationContext jsc) {
        final JsonObject json = super.serialize(assetIn, jsc);
        final JsonArray entries = new JsonArray();
        for (LittleAclEntry member : ((LittleAcl) assetIn).getEntries()) {
            entries.add( entryAdapter.serialize( member, jsc));
        }
        json.add("entries", entries);
        return json;
    }

    @Override
    public AssetBuilder deserialize(JsonObject json, JsonDeserializationContext jdc, LittleGsonResolver resolver) throws JsonParseException {
        final LittleAcl.Builder builder = super.deserialize(json, jdc, resolver).narrow();

        if (builder.getId().equals(AccountManager.UUID_EVERYBODY_GROUP)) {
            return builder;
        }
        try {
            for (JsonElement element : json.getAsJsonArray("entries")) {
                final LittleAclEntry.Builder entryBuilder = this.entryAdapter.deserialize( element.getAsJsonObject(), jdc, resolver).narrow();
                resolver.getAsset( ((AbstractAssetBuilder) entryBuilder).getToId() ).map(
                        (Asset a) -> {
                            entryBuilder.setPrincipal(a.narrow(LittlePrincipal.class));
                            builder.addEntry( entryBuilder.build() );
                            return builder;
                        }
                );
            }
        } catch (Exception ex) {
            throw new JsonParseException("Failed to load member assets", ex);
        }
        return builder;
    }
}
/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.internal;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.gson.AbstractAssetAdapter;
import littleware.asset.gson.LittleGsonResolver;
import littleware.base.UUIDFactory;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;

public class GroupGsonAdapter extends AbstractAssetAdapter {

    @Inject
    public GroupGsonAdapter(Provider<LittleGroup.Builder> builderFactory) {
        super(LittleGroup.GROUP_TYPE, builderFactory);
    }

    @Override
    public JsonObject serialize(Asset assetIn, 
            JsonSerializationContext jsc) {
        final JsonObject json = super.serialize(assetIn, jsc);
        if ( ! assetIn.getId().equals( AccountManager.UUID_EVERYBODY_GROUP ) ) {
            final JsonArray members = new JsonArray();
            for (LittlePrincipal member : ((LittleGroup) assetIn).getMembers()) {
                members.add(new JsonPrimitive(UUIDFactory.makeCleanString(member.getId())));
            }
            json.add("members", members);
        }
        return json;
    }

    @Override
    public AssetBuilder deserialize(JsonObject json, JsonDeserializationContext jdc, LittleGsonResolver resolver) throws JsonParseException {
        final LittleGroup.Builder builder = super.deserialize(json, jdc, resolver).narrow();

        if ( builder.getId().equals( AccountManager.UUID_EVERYBODY_GROUP ) ) {
            return builder;
        }
        try {
            for (JsonElement element : json.getAsJsonArray("members")) {
                final UUID memberId = UUIDFactory.parseUUID(element.getAsString());
                for( Asset a : resolver.getAsset(memberId) ) {
                    builder.add( a.narrow( LittlePrincipal.class ) );
                }
            }
        } catch (Exception ex) {
            throw new JsonParseException("Failed to load member assets", ex);
        }
        return builder;
    }
}
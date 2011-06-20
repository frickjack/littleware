/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.gson;

import littleware.lgo.LgoHelp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.lang.reflect.Type;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetType;

/**
 * Register serializers for core types with GsonBuilder
 */
@Singleton
public class GsonProvider implements Provider<Gson> {
    private static final Logger log = Logger.getLogger( GsonProvider.class.getName() );
    private final GsonBuilder gsonBuilder;
    private final Map<AssetType, JsonSerializer<Asset>> typeMap = new HashMap<AssetType, JsonSerializer<Asset>>();
    private final JsonSerializer<Asset> defaultSerializer = null; //new AbstractAssetAdapter();

    private synchronized JsonSerializer<Asset> lookupSerializer( final AssetType type) {
        for (AssetType scan = type; scan != null;
                scan = scan.getSuperType().getOr(null)) {
            final JsonSerializer<Asset> result = typeMap.get(scan);
            if (null != result) {
                return result;
            }
        }
        return defaultSerializer;
    }

    @Override
    public synchronized Gson get() {
        return gsonBuilder.create();
    }

    /**
     * Utility to allow other modules to register Gson serializers
     */
    public synchronized void registerSerializer(Type type, JsonSerializer<?> serializer) {
        gsonBuilder.registerTypeAdapter(type, serializer);
    }

    public synchronized void registerSerializer(AssetType assetType, JsonSerializer<Asset> serializer) {
        typeMap.put( assetType, serializer );
    }

    /**
     * Delegating serializer delegates to different serializers based on asset-type.
     */
    private class AssetDelegateSerializer implements JsonSerializer<Asset> {

        @Override
        public JsonElement serialize(Asset asset, Type type, JsonSerializationContext jsc) {
            final JsonObject json = new JsonObject();
            json.addProperty("type", asset.getAssetType().toString());
            return lookupSerializer( asset.getAssetType() ).serialize( asset,type, jsc);
        }
    }

    public final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static class HelpSerializer implements JsonSerializer<LgoHelp> {

        @Override
        public JsonElement serialize(LgoHelp help, Type type, JsonSerializationContext jsc) {
            return new JsonPrimitive( help.toString() );
        }
    }


    @Inject
    public GsonProvider(GsonBuilder gsonBuilder) {
        this.gsonBuilder = gsonBuilder;

        gsonBuilder.registerTypeAdapter(UUID.class,
                new JsonSerializer<UUID>() {
                    @Override
                    public JsonElement serialize(UUID t, Type type, JsonSerializationContext jsc) {
                        return new JsonPrimitive(t.toString());
                    }
                });
        gsonBuilder.registerTypeAdapter(Date.class,
                new JsonSerializer<Date>() {
                    @Override
                    public JsonElement serialize(Date t, Type type, JsonSerializationContext jsc) {
                        return new JsonPrimitive( dateFormat.format(t) );
                    }
                });
        gsonBuilder.registerTypeAdapter(AssetPath.class,
                new JsonSerializer<AssetPath>() {
                    @Override
                    public JsonElement serialize(AssetPath t, Type type, JsonSerializationContext jsc) {
                        return new JsonPrimitive(t.toString());
                    }
                });
        gsonBuilder.registerTypeAdapter(AssetType.class,
                new JsonSerializer<AssetType>() {
                    @Override
                    public JsonElement serialize(AssetType t, Type type, JsonSerializationContext jsc) {
                        return new JsonPrimitive(t.toString());
                    }
                });
        gsonBuilder.registerTypeAdapter(LgoHelp.class,
                new HelpSerializer()
                );

        gsonBuilder.registerTypeAdapter(Asset.class,
                new AssetDelegateSerializer()
                );
    }
}

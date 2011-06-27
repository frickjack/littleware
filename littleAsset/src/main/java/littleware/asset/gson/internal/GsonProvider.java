/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.gson.internal;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonParseException;
import littleware.asset.gson.LittleGsonResolver;
import littleware.lgo.LgoHelp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializer;
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
import littleware.asset.gson.GsonAssetAdapter;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.spi.AssetProviderRegistry;

/**
 * Register serializers for core types with GsonBuilder
 */
@Singleton
public class GsonProvider implements LittleGsonFactory {
    private static final Logger log = Logger.getLogger( GsonProvider.class.getName() );

    private final Map<String, GsonAssetAdapter> typeMap = new HashMap<String, GsonAssetAdapter>();
    private final Provider<GsonBuilder> gsonBuilderFactory;
    


    @Override
    public synchronized Gson get() {
        return get( LittleGsonResolver.nullResolver );
    }


    @Override
    public synchronized void registerAdapter(GsonAssetAdapter adapter ) {
        typeMap.put( adapter.getAssetType().getName(), adapter );
    }

    @Override
    public Gson get(LittleGsonResolver resolver) {
        final GsonBuilder gsonBuilder = gsonBuilderFactory.get();
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
                new AssetDelegateSerializer( resolver )
                );
        return gsonBuilder.serializeNulls().create();
    }

    /**
     * Delegating serializer delegates to different serializers based on asset-type.
     */
    private class AssetDelegateSerializer implements JsonSerializer<Asset>, JsonDeserializer<Asset> {
        private final LittleGsonResolver resolver;
        
        public AssetDelegateSerializer( LittleGsonResolver resolver ) {
            this.resolver = resolver;
        }
        
        @Override
        public JsonElement serialize(Asset asset, Type type, JsonSerializationContext jsc) {
            final GsonAssetAdapter adapter = typeMap.get( asset.getAssetType().getName() );
            if ( null == adapter ) {
                throw new IllegalArgumentException( "No gson adapter registered for asset type: " + asset.getAssetType() );
            }
            return adapter.serialize( asset, jsc);
        }

        @Override
        public Asset deserialize(JsonElement je, Type type, JsonDeserializationContext jdc) throws JsonParseException {
            final JsonObject json = je.getAsJsonObject();
            final String typeString = json.get( "type" ).getAsString();
            final GsonAssetAdapter adapter = typeMap.get( typeString );
            if ( null == adapter ) {
                throw new IllegalArgumentException( "No asset adapter registered to deserialize asset of type: " + typeString );
            }
            return adapter.deserialize(json, jdc, resolver).build();
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
    public GsonProvider( 
            Provider<GsonBuilder> gsonBuilderFactory
            ) {
        this.gsonBuilderFactory = gsonBuilderFactory;
    }
}

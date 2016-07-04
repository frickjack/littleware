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
import com.google.inject.Provider;
import com.google.inject.Singleton;
import java.lang.reflect.Type;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetType;
import littleware.asset.gson.GsonAssetAdapter;
import littleware.asset.gson.LittleGsonFactory;


/**
 * Register serializers for core types with GsonBuilder
 */
@Singleton
public class GsonProvider implements LittleGsonFactory {
    private static final Logger log = Logger.getLogger( GsonProvider.class.getName() );

    private final Map<String, GsonAssetAdapter> typeMap = new HashMap<>();
    private final Provider<GsonBuilder> gsonBuilderFactory;
    


    @Override
    public synchronized Gson get() {
        return getBuilder().create();
    }


    @Override
    public synchronized LittleGsonFactory registerAssetAdapter(GsonAssetAdapter adapter ) {
        typeMap.put( adapter.getAssetType().getName(), adapter );
        return this;
    }

    @Override
    public GsonBuilder getBuilder(LittleGsonResolver resolver) {
        final GsonBuilder gsonBuilder = gsonBuilderFactory.get();
        gsonBuilder.registerTypeAdapter(UUID.class, (JsonSerializer<UUID>) (UUID t, Type type, JsonSerializationContext jsc) -> new JsonPrimitive(t.toString()));
        gsonBuilder.registerTypeAdapter(Date.class, (JsonSerializer<Date>) (Date t, Type type, JsonSerializationContext jsc) -> new JsonPrimitive( ZonedDateTime.ofInstant( t.toInstant(), ZoneOffset.ofHours(0) ).toString() ));
        gsonBuilder.registerTypeAdapter(AssetPath.class, (JsonSerializer<AssetPath>) (AssetPath t, Type type, JsonSerializationContext jsc) -> new JsonPrimitive(t.toString()));
        gsonBuilder.registerTypeAdapter(AssetType.class, (JsonSerializer<AssetType>) (AssetType t, Type type, JsonSerializationContext jsc) -> new JsonPrimitive(t.toString()));
        gsonBuilder.registerTypeAdapter(LgoHelp.class,
                new HelpSerializer()
                );

        gsonBuilder.registerTypeAdapter(Asset.class,
                new AssetDelegateSerializer( resolver )
                );
        this.customAdapterList.stream().forEach((adapter) -> {
            gsonBuilder.registerTypeAdapter( adapter.clazz, adapter.adapter);
        });
        return gsonBuilder.serializeNulls();
    }

    @Override
    public Gson get(LittleGsonResolver resolver ) {
        return getBuilder( resolver ).create();
    }
    
    @Override
    public GsonBuilder getBuilder() {
        return getBuilder( LittleGsonResolver.nullResolver );
    }

    private static class AdapterPair {
        public final Type clazz;
        public final Object adapter;
        
        public AdapterPair( Type clazz, Object adapter ) {
            this.clazz = clazz;
            this.adapter = adapter;
        }
    }
    
    private final List<AdapterPair> customAdapterList = new ArrayList<>();
    
    @Override
    public LittleGsonFactory registerTypeAdapter(Type clazz, Object adapter) {
        customAdapterList.add( new AdapterPair( clazz, adapter ));
        return this;
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
            final String typeString = json.get( "assetType" ).getAsJsonObject().get( "name" ).getAsString();
            final GsonAssetAdapter adapter = typeMap.get( typeString );
            if ( null == adapter ) {
                throw new IllegalArgumentException( "No asset adapter registered to deserialize asset of type: " + typeString );
            }
            return adapter.deserialize(json, jdc, resolver).build();
        }
    }

    //public final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static class HelpSerializer implements JsonSerializer<LgoHelp> {

        @Override
        public JsonElement serialize(LgoHelp help, Type type, JsonSerializationContext jsc) {
            return new JsonPrimitive( help.toString() );
        }
    }


    public GsonProvider() {
        this.gsonBuilderFactory = () -> new GsonBuilder();
    }
    
}

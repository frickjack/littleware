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

import littleware.lgo.LgoHelp;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
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
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetPath;
import littleware.asset.AssetType;
import littleware.asset.spi.AbstractAsset;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;

/**
 * Register serializers for core types with GsonBuilder
 */
@Singleton
public class GsonProvider implements Provider<Gson> {
    private static final Logger log = Logger.getLogger( GsonProvider.class.getName() );
    private final GsonBuilder gsonBuilder;
    private final Map<AssetType, JsonSerializer<Asset>> typeMap = new HashMap<AssetType, JsonSerializer<Asset>>();
    private final JsonSerializer<Asset> defaultSerializer = new AssetSerializer();

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
            return lookupSerializer( asset.getAssetType() ).serialize( asset,type, jsc);
        }
    }

    private final static DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

    public static class AssetSerializer implements JsonSerializer<Asset> {

        public DateFormat getDateFormat() {
            return dateFormat;
        }

        public String toStringOrNull(Date in) {
            return toStringOrNull(in, getDateFormat());
        }

        /**
         * Little utility: return (null == in) ? null : in;
         */
        public String toStringOrNull(UUID in) {
            return (null == in) ? null : in.toString();
        }

        public String toStringOrNull(Date in, DateFormat format) {
            return (null == in) ? null : format.format(in);
        }

        @Override
        public JsonObject serialize(Asset assetIn, Type type, JsonSerializationContext jsc) {
            final AbstractAsset asset = (AbstractAsset) assetIn;
            final JsonObject result = new JsonObject();
            result.addProperty("name", asset.getName());
            result.addProperty("type", asset.getAssetType().toString());
            result.addProperty("id", toStringOrNull(asset.getId()));
            result.addProperty("home", toStringOrNull(asset.getHomeId()));
            result.addProperty("acl", toStringOrNull(asset.getAclId()));
            result.addProperty("from", toStringOrNull(asset.getFromId()));
            result.addProperty("to", toStringOrNull(asset.getToId()));
            result.addProperty("owner", toStringOrNull(asset.getOwnerId()));
            result.addProperty("creator", toStringOrNull(asset.getCreatorId()));
            result.addProperty("createDate", toStringOrNull(asset.getCreateDate()));
            result.addProperty("updater", toStringOrNull(asset.getLastUpdaterId()));
            result.addProperty("updateDate", toStringOrNull(asset.getLastUpdateDate()));
            result.addProperty( "updateComment", asset.getLastUpdate() );
            result.addProperty("startDate", toStringOrNull(asset.getStartDate()));
            result.addProperty("endDate", toStringOrNull(asset.getEndDate()));
            result.addProperty("transaction", Long.toString(asset.getTimestamp()));
            result.addProperty("comment", asset.getComment());
            result.addProperty("value", asset.getValue());
            result.addProperty("state", asset.getState());
            result.addProperty("data", asset.getData());
            final Iterator<String>  itLabel = Arrays.asList( "attrMap", "linkMap", "dateMap" ).iterator();
            for( Map<String,?> dataMap : Arrays.asList( asset.getAttributeMap(),
                    asset.getLinkMap(), asset.getDateMap())
                    ) {
                final JsonObject obj = new JsonObject();
                for( Map.Entry<String,?> entry : dataMap.entrySet() ) {
                    obj.add(entry.getKey(), jsc.serialize( entry.getValue() ) );
                }
                obj.add( itLabel.next(), obj );
            }
            if (asset.getAssetType().isA(LittleGroup.GROUP_TYPE)) {
                final JsonArray members = new JsonArray();
                for (LittlePrincipal member : ((LittleGroup) asset).getMembers()) {
                    members.add(new JsonPrimitive(member.getName()));
                }
                result.add("members", members);
            }

            return result;
        }
    }
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

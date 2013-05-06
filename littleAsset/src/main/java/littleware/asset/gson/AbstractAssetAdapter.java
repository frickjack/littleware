/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.gson;

import littleware.asset.gson.internal.GsonProvider;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.inject.Provider;
import java.text.DateFormat;
import java.text.ParseException;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.UUIDFactory;

/**
 * Base class for GSon asset adapters
 */
public abstract class AbstractAssetAdapter implements GsonAssetAdapter {

    private final AssetType assetType;
    private final Provider<? extends AssetBuilder> builderFactory;

    protected AbstractAssetAdapter(AssetType assetType, Provider<? extends AssetBuilder> builderFactory) {
        this.assetType = assetType;
        this.builderFactory = builderFactory;
    }

    public DateFormat getDateFormat() {
        return GsonProvider.dateFormat;
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

    public UUID toIdOrNull( JsonElement in) {
        return ( (null == in) || in.isJsonNull() ) ? null : UUIDFactory.parseUUID(in.getAsString() );
    }

    public String toStringOrNull(Date in, DateFormat format) {
        return (null == in) ? null : format.format(in);
    }

    public Date toDateOrNull(JsonElement in) {
        try {
            return (null == in  || in.isJsonNull()) ? null : getDateFormat().parse(in.getAsString());
        } catch (ParseException ex) {
            throw new AssertionFailedException("Failed to parse date: " + in, ex);
        }
    }
    
    public String toStringOrEmpty( JsonElement in ) {
        return (null == in  || in.isJsonNull()) ? "" : in.getAsString();
    }

    @Override
    public JsonObject serialize(Asset assetIn,   
        JsonSerializationContext jsc
            ) {
        final JsonObject json = new JsonObject();
        json.addProperty("type", assetIn.getAssetType().toString());
        final AbstractAsset asset = (AbstractAsset) assetIn;
        json.addProperty("name", asset.getName());
        json.addProperty("id", toStringOrNull(asset.getId()));
        json.addProperty("home", toStringOrNull(asset.getHomeId()));
        json.addProperty("acl", toStringOrNull(asset.getAclId()));
        json.addProperty("from", toStringOrNull(asset.getFromId()));
        json.addProperty("to", toStringOrNull(asset.getToId()));
        json.addProperty("owner", toStringOrNull(asset.getOwnerId()));
        json.addProperty("creator", toStringOrNull(asset.getCreatorId()));
        json.addProperty("createDate", toStringOrNull(asset.getCreateDate()));
        json.addProperty("updater", toStringOrNull(asset.getLastUpdaterId()));
        json.addProperty("updateDate", toStringOrNull(asset.getLastUpdateDate()));
        json.addProperty("updateComment", asset.getLastUpdate());
        json.addProperty("startDate", toStringOrNull(asset.getStartDate()));
        json.addProperty("endDate", toStringOrNull(asset.getEndDate()));
        json.addProperty("timestamp", Long.toString(asset.getTimestamp()));
        json.addProperty("comment", asset.getComment());
        json.addProperty("value", asset.getValue());
        json.addProperty("state", asset.getState());
        json.addProperty("data", asset.getData());
        final Iterator<String> itLabel = Arrays.asList("attrMap", "linkMap", "dateMap").iterator();
        for (Map<String, ?> dataMap : Arrays.asList(asset.getAttributeMap(), asset.getLinkMap(), asset.getDateMap())) {
            final JsonObject obj = new JsonObject();
            for (Map.Entry<String, ?> entry : dataMap.entrySet()) {
                obj.add(entry.getKey(), jsc.serialize(entry.getValue()));
            }
            obj.add(itLabel.next(), obj);
        }
        return json;
    }

    @Override
    public AssetType getAssetType() {
        return assetType;
    }

    @Override
    public AssetBuilder deserialize( JsonObject json, JsonDeserializationContext jdc, LittleGsonResolver resolver
          ) throws JsonParseException {
        final AbstractAssetBuilder builder = (AbstractAssetBuilder) builderFactory.get();
        builder.setName(json.get("name").getAsString());
        builder.setId(toIdOrNull(json.get("id")));
        resolver.markInProcess( builder.getId() );
        builder.setHomeId(toIdOrNull(json.get("home")));
        builder.setAclId(toIdOrNull(json.get("acl")));
        builder.setFromId(toIdOrNull(json.get("from")));
        builder.setToId(toIdOrNull(json.get("to")));
        builder.setOwnerId(toIdOrNull(json.get("owner")));
        builder.setCreatorId(toIdOrNull(json.get("creator")));
        builder.setLastUpdaterId(toIdOrNull(json.get("updater")));
        builder.setCreateDate(toDateOrNull(json.get("createDate")));
        builder.setLastUpdateDate(toDateOrNull(json.get("updateDate")));
        builder.setLastUpdate( toStringOrEmpty( json.get("updateComment") ) );
        builder.setStartDate(toDateOrNull(json.get("startDate")));
        builder.setEndDate(toDateOrNull(json.get("endDate")));
        builder.setTimestamp(json.get("timestamp").getAsLong());
        builder.setComment( toStringOrEmpty( json.get("comment") ) );
        builder.setValue(json.get("value").getAsFloat());
        builder.setState(json.get("state").getAsInt());
        builder.setData( toStringOrEmpty( json.get("data") ) );

        final JsonObject empty = new JsonObject();
        
        for (Map.Entry<String, JsonElement> entry : 
                Maybe.something( json.getAsJsonObject("attrMap") ).getOr( empty ).entrySet()) {
            builder.putAttribute(entry.getKey(), entry.getValue().getAsString());
        }
        for (Map.Entry<String, JsonElement> entry : 
                Maybe.something( json.getAsJsonObject("linkMap") ).getOr( empty ).entrySet()) {
            builder.putLink(entry.getKey(), UUIDFactory.parseUUID( entry.getValue().getAsString()) );
        }
        for( Map.Entry<String, JsonElement> entry : 
                Maybe.something( json.getAsJsonObject("dateMap") ).getOr( empty ).entrySet() ) {
            try {
                builder.putDate( entry.getKey(), getDateFormat().parse( entry.getValue().getAsString() ) );
            } catch ( Exception ex ) {
                throw new AssertionFailedException( "Failed to parse date attribute: " + entry.getValue().getAsString() );
            }
        }

        return (AssetBuilder) builder;
    }

    @Override
    public Provider<? extends AssetBuilder> getBuilderFactory() {
        return builderFactory;
    }
}

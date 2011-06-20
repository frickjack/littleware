/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.gson;

import com.google.gson.JsonArray;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
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
import littleware.base.UUIDFactory;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;


/**
 * Base class for GSon asset adapters
 */
public abstract class AbstractAssetAdapter implements GsonAssetAdapter {
    private final AssetType assetType;
    private final Provider<? extends AssetBuilder> builderFactory;

    protected AbstractAssetAdapter( AssetType assetType, Provider<? extends AssetBuilder> builderFactory ) {
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
    
    public UUID toIdOrNull( String in ) {
        return (null == in) ? null : UUIDFactory.parseUUID( in );
    } 

    public String toStringOrNull(Date in, DateFormat format) {
        return (null == in) ? null : format.format(in);
    }
    
    public Date toDateOrNull( String in ) {
        try {
            return (null == in) ? null : getDateFormat().parse(in);
        } catch (ParseException ex) {
            throw new AssertionFailedException( "Failed to parse date: " + in, ex );
        }
    }

    @Override
    public JsonObject serialize(Asset assetIn, JsonObject json, JsonSerializationContext jsc) {
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
        if (asset.getAssetType().isA(LittleGroup.GROUP_TYPE)) {
            final JsonArray members = new JsonArray();
            for (LittlePrincipal member : ((LittleGroup) asset).getMembers()) {
                members.add(new JsonPrimitive(member.getName()));
            }
            json.add("members", members);
        }
        return json;
    }

    @Override
    public AssetType getAssetType() {
        return assetType;
    }

    @Override
    public AssetBuilder deserialize( AssetBuilder assetBuilder, JsonObject json, JsonDeserializationContext jdc) throws JsonParseException {
        final AbstractAssetBuilder builder = (AbstractAssetBuilder) assetBuilder;
        builder.setName( json.get("name" ).getAsString() );
        builder.setId( toIdOrNull( json.get("id" ).getAsString() ) );
        builder.setHomeId( toIdOrNull( json.get( "home" ).getAsString() ));
        builder.setAclId( toIdOrNull( json.get( "acl" ).getAsString() ));
        builder.setFromId( toIdOrNull( json.get( "from" ).getAsString() ));
        builder.setToId( toIdOrNull( json.get( "to" ).getAsString() ));
        builder.setOwnerId( toIdOrNull( json.get( "owner" ).getAsString() ));
        builder.setCreatorId( toIdOrNull( json.get( "creator" ).getAsString() ));
        builder.setLastUpdaterId( toIdOrNull( json.get( "updater" ).getAsString() ));
        builder.setCreateDate( toDateOrNull( json.get( "createDate" ).getAsString() ));
        builder.setLastUpdateDate( toDateOrNull( json.get( "updateDate" ).getAsString() ));
        builder.setLastUpdate( json.get( "updateComment" ).getAsString() );
        builder.setStartDate( toDateOrNull( json.get( "startDate" ).toString() ) );
        builder.setEndDate( toDateOrNull( json.get( "endDate" ).toString() ) );
        builder.setTimestamp( json.get( "timestamp" ).getAsLong() );
        json.addProperty("comment", asset.getComment());
        json.addProperty("value", asset.getValue());
        json.addProperty("state", asset.getState());
        json.addProperty("data", asset.getData());

        return assetBuilder;
    }

    @Override
    public Provider<? extends AssetBuilder> getBuilderFactory() {
        return builderFactory;
    }
    
}

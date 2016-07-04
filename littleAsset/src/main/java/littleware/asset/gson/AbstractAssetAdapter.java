package littleware.asset.gson;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.inject.Provider;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.Arrays;
import java.util.Date;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.asset.spi.AbstractAsset;
import littleware.asset.spi.AbstractAssetBuilder;
import littleware.base.AssertionFailedException;
import littleware.base.Options;
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

    /*
    public DateFormat getDateFormat() {
        return GsonProvider.dateFormat;
    }
    */

    public String toStringOrNull(Date in) {
        return ZonedDateTime.ofInstant( in.toInstant(), ZoneOffset.systemDefault() ).toString();
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

    /*
    public String toStringOrNull(Date in, DateFormat format) {
        return (null == in) ? null : format.format(in);
    }
    */

    public Date toDateOrNull(JsonElement in) {
        return (null == in  || in.isJsonNull()) ? null : Date.from( ZonedDateTime.parse(in.getAsString()).toInstant() );
    }
    
    public String toStringOrEmpty( JsonElement in ) {
        return (null == in  || in.isJsonNull()) ? "" : in.getAsString();
    }

    @Override
    public JsonObject serialize(Asset assetIn,   
        JsonSerializationContext jsc
            ) {
        final JsonObject json = new JsonObject();
        final JsonObject jsType = new JsonObject();
        jsType.addProperty( "id", assetIn.getAssetType().getObjectId().toString() );
        jsType.addProperty( "name", assetIn.getAssetType().getName() );
        json.add("assetType", jsType );
        final AbstractAsset asset = (AbstractAsset) assetIn;
        json.addProperty("name", asset.getName());
        json.addProperty("id", toStringOrNull(asset.getId()));
        json.addProperty("homeId", toStringOrNull(asset.getHomeId()));
        json.addProperty("aclId", toStringOrNull(asset.getAclId()));
        json.addProperty("fromId", toStringOrNull(asset.getFromId()));
        json.addProperty("toId", toStringOrNull(asset.getToId()));
        json.addProperty("ownerId", toStringOrNull(asset.getOwnerId()));
        json.addProperty("creatorId", toStringOrNull(asset.getCreatorId()));
        json.addProperty("dateCreated", toStringOrNull(asset.getCreateDate()));
        json.addProperty("updaterId", toStringOrNull(asset.getLastUpdaterId()));
        json.addProperty("dateUpdated", toStringOrNull(asset.getLastUpdateDate()));
        json.addProperty("updateComment", asset.getLastUpdate());
        json.addProperty("startDate", toStringOrNull(asset.getStartDate()));
        json.addProperty("endDate", toStringOrNull(asset.getEndDate()));
        json.addProperty("timestamp", asset.getTimestamp() );
        json.addProperty("comment", asset.getComment());
        json.addProperty("value", asset.getValue());
        json.addProperty("state", asset.getState());
        json.addProperty("data", asset.getData());
        final Iterator<String> itLabel = Arrays.asList("otherProps", "linkMap", "dateMap").iterator();
        Arrays.asList(asset.getAttributeMap(), asset.getLinkMap(), asset.getDateMap()).stream().map((dataMap) -> {
            final JsonObject obj = new JsonObject();
            dataMap.entrySet().stream().forEach((entry) -> {
                obj.add(entry.getKey(), jsc.serialize(entry.getValue()));
            });
            return obj;
        }).forEach((obj) -> {
            json.add(itLabel.next(), obj);
        });
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
        {
            final UUID jsonId = toIdOrNull( json.get("id" ) );
            if ( null != jsonId ) {
                builder.setId(jsonId);
            } // else - leave randomly assigned builder id unchanged
        }
        resolver.markInProcess( builder.getId() );
        builder.setHomeId(toIdOrNull(json.get("homeId")));
        builder.setAclId(toIdOrNull(json.get("aclId")));
        builder.setFromId(toIdOrNull(json.get("fromId")));
        builder.setToId(toIdOrNull(json.get("toId")));
        builder.setOwnerId(toIdOrNull(json.get("ownerId")));
        builder.setCreatorId(toIdOrNull(json.get("creatorId")));
        builder.setLastUpdaterId(toIdOrNull(json.get("updaterId")));
        builder.setCreateDate(toDateOrNull(json.get("dateCreated")));
        builder.setLastUpdateDate(toDateOrNull(json.get("dateUpdated")));
        builder.setLastUpdate( toStringOrEmpty( json.get("updateComment") ) );
        builder.setStartDate(toDateOrNull(json.get("startDate")));
        builder.setEndDate(toDateOrNull(json.get("endDate")));
        builder.setTimestamp(json.get("timestamp").getAsLong());
        builder.setComment( toStringOrEmpty( json.get("comment") ) );
        builder.setValue(json.get("value").getAsFloat());
        builder.setState(json.get("state").getAsInt());
        builder.setData( toStringOrEmpty( json.get("data") ) );

        final JsonObject empty = new JsonObject();

        Optional.ofNullable( json.getAsJsonObject("otherProps") ).ifPresent( 
                (obj) -> obj.entrySet().stream().forEach( 
            (entry) -> {
                builder.putAttribute(entry.getKey(), entry.getValue().getAsString());
            }
                )
        );
        
        for (Map.Entry<String, JsonElement> entry : 
                Options.some( json.getAsJsonObject("linkMap") ).getOr( empty ).entrySet()) {
            builder.putLink(entry.getKey(), UUIDFactory.parseUUID( entry.getValue().getAsString()) );
        }
        for( Map.Entry<String, JsonElement> entry : 
                Options.some( json.getAsJsonObject("dateMap") ).getOr( empty ).entrySet() ) {
            try {
                builder.putDate( entry.getKey(), toDateOrNull( entry.getValue() ) );
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

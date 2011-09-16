/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.google.inject.Inject;
import littleware.asset.AssetBuilder;
import java.util.Date;
import java.text.ParseException;
import littleware.base.BaseException;
import littleware.asset.spi.AbstractAssetBuilder;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.google.common.collect.ImmutableMap;
import java.sql.SQLException;
import java.util.Map;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.UUIDFactory;
import littleware.db.DbReader;


/**
 * Handler to load an asset with a given id
 */
public class DbAssetLoader implements DbReader<Asset, UUID> {

    private final AmazonSimpleDB db;
    private final AwsConfig config;
    private final AssetProviderRegistry assetRegistry;

    @Inject
    public DbAssetLoader(AmazonSimpleDB db, AwsConfig config,
            AssetProviderRegistry assetRegistry) {
        this.db = db;
        this.config = config;
        this.assetRegistry = assetRegistry;
    }

    public UUID toUUID(Attribute attr) {
        return (null != attr) ? UUIDFactory.parseUUID(attr.getValue()) : null;
    }

    public String toString(Attribute attr) {
        if (null == attr) {
            return "";
        } else {
            return attr.getValue();
        }
    }

    public Date toDate(Attribute attr) throws ParseException {
        if (null == attr) {
            return null;
        } else {
            return SimpleDBUtils.decodeDate(attr.getValue());
        }
    }

    /**
     * Build an asset from the given index of SimpleDB item attributes
     */
    public Asset itemToAsset(Map<String, Attribute> attrIndex) throws BaseException, ParseException {
        final AbstractAssetBuilder<? extends AssetBuilder> builder = (AbstractAssetBuilder<? extends AssetBuilder>) this.assetRegistry.getService(
                AssetType.getMember(UUIDFactory.parseUUID(attrIndex.get("typeId").getValue()))).get();
        builder.id(UUIDFactory.parseUUID(attrIndex.get("id").getValue())).name(attrIndex.get("name").getValue());
        builder.value(Float.parseFloat(attrIndex.get("value").getValue())).timestamp(Long.parseLong(attrIndex.get("timestamp").getValue()));
        builder.state(Integer.parseInt(attrIndex.get("state").getValue())).comment(toString(attrIndex.get("comment"))).lastUpdate(toString(attrIndex.get("updateComment")));
        builder.createDate(toDate(attrIndex.get("createDate"))).lastUpdateDate(toDate(attrIndex.get("updateDate")));
        builder.startDate(toDate(attrIndex.get("startDate")));
        builder.endDate(toDate(attrIndex.get("endDate")));
        builder.setHomeId(toUUID(attrIndex.get("homeId")));
        builder.setFromId(toUUID(attrIndex.get("fromId")));
        builder.setToId(toUUID(attrIndex.get("toId")));
        builder.setOwnerId(toUUID(attrIndex.get("ownerId")));
        builder.setCreatorId(toUUID(attrIndex.get("creatorId")));
        builder.setLastUpdaterId(toUUID(attrIndex.get("updaterId")));
        builder.setAclId(toUUID(attrIndex.get("aclId")));
        for (Attribute attr : attrIndex.values()) {
            final String key = attr.getName();
            if (key.startsWith("attr:")) {
                builder.putAttribute(key.substring(5), attr.getValue());
            } else if (key.startsWith("link:")) {
                builder.putLink(key.substring(5), UUIDFactory.parseUUID(attr.getValue()));
            } else if (key.startsWith("date:")) {
                builder.putDate(key.substring(5), SimpleDBUtils.decodeDate(attr.getValue()));
            }
        }
        return builder.build();
    }

    @Override
    public Asset loadObject(UUID id) throws SQLException {
        try {
            final Map<String, Attribute> attrIndex;
            {
                final ImmutableMap.Builder<String, Attribute> builder = ImmutableMap.builder();
                for (Attribute attr : db.getAttributes(new GetAttributesRequest( config.getDbDomain(), UUIDFactory.makeCleanString(id)).withConsistentRead(Boolean.TRUE)).getAttributes()) {
                    builder.put(attr.getName(), attr);
                }
                attrIndex = builder.build();
            }
            if ( attrIndex.isEmpty() ) {
                return null;
            }
            return itemToAsset( attrIndex );
        } catch ( Exception ex) {
            throw new SQLException( "Failed asset load", ex );
        }
    }
}

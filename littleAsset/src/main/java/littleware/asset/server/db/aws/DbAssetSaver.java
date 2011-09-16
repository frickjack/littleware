/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.model.Attribute;
import com.amazonaws.services.simpledb.model.DeleteAttributesRequest;
import com.amazonaws.services.simpledb.model.GetAttributesRequest;
import com.amazonaws.services.simpledb.model.PutAttributesRequest;
import com.amazonaws.services.simpledb.model.ReplaceableAttribute;
import com.amazonaws.services.simpledb.model.ReplaceableItem;
import com.amazonaws.services.simpledb.util.SimpleDBUtils;
import com.google.inject.Inject;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.spi.AbstractAsset;
import littleware.base.UUIDFactory;
import littleware.db.DbWriter;

/**
 * Handler to save a given asset
 */
public class DbAssetSaver implements DbWriter<Asset> {
    private static final Logger log = Logger.getLogger( DbAssetSaver.class.getName() );
    
    private final AmazonSimpleDB db;
    private final AwsConfig config;

    @Inject
    public DbAssetSaver(AmazonSimpleDB db, AwsConfig config) {
        this.db = db;
        this.config = config;
    }

    /**
     * Utility class for assembling an AWS item
     */
    public static class ItemBuilder {

        private final List<ReplaceableAttribute> attrList = new ArrayList<ReplaceableAttribute>();

        public List<ReplaceableAttribute> getAttributes() {
            return attrList;
        }

        /**
         * Add the given id to the attrList with the given attribute name in a clean UUID format 
         * if id is not null,
         * otherwise add name to the null attribute set
         */
        public ItemBuilder add(String name, UUID value) {
            if (null != value) {
                attrList.add(new ReplaceableAttribute(name, UUIDFactory.makeCleanString(value), true));
            }
            return this;
        }

        /**
         * Add the given (name,value) to the attr list if value is not null,
         * otherwise add name to the null attribute set
         */
        public ItemBuilder add(String name, String value) {
            if ((null != value) && value.length() > 0) {
                attrList.add(new ReplaceableAttribute(name, value, true));
            }
            return this;
        }

        /**
         * Add the given (name,value) to the attr list with properly formatted value
         * if value is not null, otherwise add name to the null attribute set
         */
        public ItemBuilder add(String name, Date value) {
            if (null != value) {
                attrList.add(new ReplaceableAttribute(name, SimpleDBUtils.encodeDate(value), true));
            }
            return this;
        }

        public ItemBuilder add(ReplaceableAttribute attr) {
            attrList.add(attr);
            return this;
        }
    }
    
    public static String encodeState( int state ) {
        return SimpleDBUtils.encodeZeroPadding(state, 10);
    }

    public static ReplaceableItem assetToItem(AbstractAsset asset) {
        final ItemBuilder builder = new ItemBuilder();

        builder.add("id", asset.getId());
        builder.add("typeId", asset.getAssetType().getObjectId());
        builder.add("toId", asset.getToId());
        builder.add("fromId", asset.getFromId());
        builder.add("ownerId", asset.getOwnerId());
        builder.add("creatorId", asset.getCreatorId());
        builder.add("updaterId", asset.getLastUpdaterId());
        builder.add("aclId", asset.getAclId());
        builder.add("homeId", asset.getHomeId());
        builder.add(new ReplaceableAttribute("timestamp", SimpleDBUtils.encodeZeroPadding(asset.getTimestamp(), 20), true));
        builder.add(new ReplaceableAttribute("state", encodeState( asset.getState() ), true));
        builder.add("name", asset.getName());
        builder.add(new ReplaceableAttribute("value", asset.getValue().toString(), true));
        builder.add("data", asset.getData());
        builder.add("comment", asset.getData());
        builder.add("updateComment", asset.getLastUpdate());
        builder.add("createDate", asset.getCreateDate());
        builder.add("updateDate", asset.getLastUpdateDate());
        builder.add("endDate", asset.getEndDate());
        builder.add("startDate", asset.getStartDate());

        for (Map.Entry<String, String> entry : asset.getAttributeMap().entrySet()) {
            builder.add("attr:" + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, UUID> entry : asset.getLinkMap().entrySet()) {
            builder.add("link:" + entry.getKey(), entry.getValue());
        }
        for (Map.Entry<String, Date> entry : asset.getDateMap().entrySet()) {
            builder.add("date:" + entry.getKey(), entry.getValue());
        }
        return new ReplaceableItem(UUIDFactory.makeCleanString(asset.getId()), builder.getAttributes());
    }

    @Override
    public void saveObject(Asset asset) throws SQLException {
        try {
            final ReplaceableItem item = DbAssetSaver.assetToItem((AbstractAsset) asset);
            final List<Attribute> oldAttrs = db.getAttributes(new GetAttributesRequest(config.getDbDomain(), item.getName()).withConsistentRead(Boolean.TRUE)).getAttributes();

            //db.deleteAttributes( new DeleteAttributesRequest( config.getDbDomain(), UUIDFactory.makeCleanString( asset.getId() ) ) );
            db.putAttributes(new PutAttributesRequest(config.getDbDomain(), item.getName(),
                    item.getAttributes()));

            if (!oldAttrs.isEmpty()) {
                final Set<String> newAttrs = new HashSet<String>();
                for (ReplaceableAttribute attr : item.getAttributes()) {
                    newAttrs.add(attr.getName());
                }
                final List<Attribute> deleteList = new ArrayList<Attribute>();
                for (Attribute oldAttr : oldAttrs) {
                    if (!newAttrs.contains(oldAttr.getName())) {
                        deleteList.add(oldAttr);
                    }
                }
                if (!deleteList.isEmpty()) {
                    log.log( Level.FINE, "Cleaning up old attributes: {0}", deleteList);
                    db.deleteAttributes(new DeleteAttributesRequest(config.getDbDomain(), item.getName(), deleteList));
                } else {
                    log.log( Level.FINE, "No attribute cleanup necessary on save" );
                }
            } 
        } catch (RuntimeException ex) {
            throw new SQLException("Failed to save asset", ex);
        }
    }

    /*
    {
    final Map<String,AssetAttribute> oldMap = new HashMap<String,AssetAttribute>();
    for ( AssetAttribute scan : this.getAttributeSet() ) {
    oldMap.put( scan.getKey(), scan);
    }
    final Set<AssetAttribute> clean = new HashSet<AssetAttribute>();
    for ( final Map.Entry<String,String> scan : asset.getAttributeMap().entrySet() ) {
    final AssetAttribute old = oldMap.get(scan.getKey() );
    if ( null == old ) {
    clean.add( AssetAttribute.build( this, scan.getKey(), scan.getValue() ));
    } else {
    old.setValue( scan.getValue() );
    clean.add( old );
    }
    }
    this.setAttributeSet( clean );
    }
    {
    final Map<String,AssetLink> oldMap = new HashMap<String,AssetLink>();
    for ( AssetLink scan : this.getLinkSet() ) {
    oldMap.put( scan.getKey(), scan);
    }
    
    final Set<AssetLink> clean = new HashSet<AssetLink>();
    for ( final Map.Entry<String,UUID> scan : asset.getLinkMap().entrySet() ) {
    final AssetLink old = oldMap.get( scan.getKey() );
    if ( null == old ) {
    clean.add( AssetLink.build( this, scan.getKey(), UUIDFactory.makeCleanString(scan.getValue() )));
    } else {
    old.setValue( UUIDFactory.makeCleanString( scan.getValue() ) );
    clean.add( old );
    }
    }
    this.setLinkSet(clean);
    }
    {
    final Map<String,AssetDate> oldMap = new HashMap<String,AssetDate>();
    for ( AssetDate scan : this.getDateSet() ) {
    oldMap.put( scan.getKey(), scan);
    }
    
    final Set<AssetDate> clean = new HashSet<AssetDate>();
    for ( final Map.Entry<String,Date> scan : asset.getDateMap().entrySet() ) {
    final AssetDate old = oldMap.get( scan.getKey () );
    if ( null == old ) {
    clean.add( AssetDate.build( this, scan.getKey(), scan.getValue() ) );
    } else {
    old.setValue( scan.getValue() );
    clean.add( old );
    }
    }
    this.setDateSet( clean );
    }
    
    }
    
     */
}

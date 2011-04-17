/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.spi;

import littleware.base.validate.ValidationException;
import littleware.base.validate.AbstractValidator;
import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.util.*;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetType;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;


import littleware.asset.validate.AssetBuilderValidator;
import littleware.asset.validate.SimpleABValidator;
import littleware.base.*;

/**
 * AssetBuilder structural base class.
 * Subtypes should implement an appropriate AssetBuilder interface.
 */
public abstract class AbstractAssetBuilder<B extends AssetBuilder> extends AbstractValidator {

    private static final Factory<UUID> uuidFactory = UUIDFactory.getFactory();
    private static final Logger log = Logger.getLogger(AbstractAssetBuilder.class.getName());
    /** Limit on the size of the data block */
    public static final int DATA_LIMIT = 1024;
    private UUID id = uuidFactory.create();
    private UUID creatorId = null;
    private UUID lastUpdater = null;
    private UUID aclId = null;
    private final AssetType assetType;
    private String comment = "";
    private String lastUpdate = "";
    private String name = "";
    private float value = 0;
    private UUID ownerId = null;
    private String data = "";
    private UUID homeId = null;
    private UUID fromId = null;
    private UUID toId = null;
    private Date startDate = null;
    private Date endDate = null;
    private Date createDate = new Date();
    private Date updateDate = new Date();
    private int state = 0;
    private PropertyChangeSupport propSupport = new PropertyChangeSupport(this);
    private long timestamp = -1L;
    private final AssetBuilderValidator validatorBuilder;
    private final Map<String, UUID> linkMap = new HashMap<String, UUID>();
    private final Map<String, UUID> roLinkMap = Collections.unmodifiableMap(linkMap);
    private final Map<String, String> attributeMap = new HashMap<String, String>();
    private final Map<String, String> roAttributeMap = Collections.unmodifiableMap(attributeMap);
    private final Map<String, Date> dateMap = new HashMap<String, Date>();
    private final Map<String, Date> roDateMap = Collections.unmodifiableMap(dateMap);

    /** 
     * Wrapper fires property change if property changes, and
     * updates inSync property if necessary.
     * 
     * @param sProp property that changes
     * @param oldValue
     * @param newValue
     */
    protected void firePropertyChange(String sProp, Object oldValue, Object newValue) {
        propSupport.firePropertyChange(sProp, oldValue, newValue);
    }

    public void addPropertyChangeListener(
            PropertyChangeListener listen_props) {
        propSupport.addPropertyChangeListener(listen_props);
    }

    public void removePropertyChangeListener(
            PropertyChangeListener listen_props) {
        propSupport.removePropertyChangeListener(listen_props);
    }

    /**
     * Give subtypes direct access to the PropertyChangeSupport.
     * In general subtypes should prefer to call
     *     AbstractAssetBuilder.firePropertyChangeSupport
     * which also takes care of updating InSync
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
        return propSupport;
    }

    /**
     * Constructor does some basic initialization.
     * Sets type to GenericAsset.GENERIC, object-id to new id,
     * default AssetBuilderValidator
     */
    public AbstractAssetBuilder(AssetType assetType) {
        this(assetType, new SimpleABValidator());
    }

    /**
     * Constructor with validator override
     */
    public AbstractAssetBuilder(AssetType assetType, AssetBuilderValidator validatorBuilder) {
        this.assetType = assetType;
        this.validatorBuilder = validatorBuilder;
    }

    public String getName() {
        return name;
    }

    public UUID getCreatorId() {
        return creatorId;
    }

    public UUID getLastUpdaterId() {
        return lastUpdater;
    }

    public UUID getAclId() {
        return aclId;
    }

    public AssetType getAssetType() {
        return assetType;
    }

    public String getComment() {
        return comment;
    }

    public String getLastUpdate() {
        return lastUpdate;
    }

    public String getData() {
        return data;
    }

    public UUID getFromId() {
        return fromId;
    }

    public UUID getToId() {
        return toId;
    }

    public UUID getHomeId() {
        return homeId;
    }

    public UUID getOwnerId() {
        return ownerId;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public Date getCreateDate() {
        return createDate;
    }

    public Date getLastUpdateDate() {
        return updateDate;
    }

    public Float getValue() {
        return value;
    }

    /**
     * Name may not contain /
     *
     * @throws AssetException if given an illegal name
     */
    public final void setName(String value) {
        name(value);
    }

    public B name(String value) {
        if ((-1 < value.indexOf('/')) || value.startsWith("..")) {
            throw new IllegalArgumentException("Illegal asset name: " + value);
        }
        final String sOld = name;
        name = value;
        firePropertyChange(value, sOld, value);
        return (B) this;
    }

    public final void setCreatorId(UUID value) {
        creatorId(value);
    }

    public B creatorId(UUID value) {
        final UUID old = creatorId;
        creatorId = value;
        firePropertyChange("creatorId", old, value);
        return (B) this;
    }

    public final void setLastUpdaterId(UUID value) {
        lastUpdaterId(value);
    }

    public B lastUpdaterId(UUID value) {
        final UUID old = lastUpdater;
        lastUpdater = value;
        firePropertyChange("lastUpdaterId", old, lastUpdater);
        return (B) this;
    }

    public final void setAclId(UUID value) {
        aclId(value);
    }

    public B aclId(UUID value) {
        final UUID old = aclId;
        aclId = value;
        firePropertyChange("aclId", old, aclId);
        return (B) this;
    }

    /** Create a new owner object with x_owner as the only non-admin member */
    public final void setOwnerId(UUID value) {
        ownerId(value);
    }

    public B ownerId(UUID value) {
        final UUID old = ownerId;
        ownerId = value;
        firePropertyChange("owner", old, ownerId);
        return (B) this;
    }

    public final void setComment(String value) {
        comment(value);
    }

    public B comment(String value) {
        final String old = comment;
        comment = value;
        firePropertyChange("comment", old, comment);
        return (B) this;
    }

    public final void setLastUpdate(String value) {
        lastUpdate(value);
    }

    public B lastUpdate(String value) {
        final String old = lastUpdate;
        lastUpdate = value;
        firePropertyChange("lastUpdate", old, value);
        return (B) this;
    }

    public final void setData(String value) {
        data(value);
    }

    public B data(String value) {
        if (value.length() > DATA_LIMIT) {
            throw new ValidationException("Data exceeds 1024 characters");
        }
        final String old = data;
        data = value;
        firePropertyChange("data", old, value);
        return (B) this;
    }

    public final void setHomeId(UUID value) {
        homeId(value);
    }

    public B homeId(UUID value) {
        final UUID old = homeId;
        homeId = value;
        firePropertyChange("homeId", old, homeId);
        return (B) this;
    }

    public final void setFromId(UUID value) {
        fromId(value);
    }

    public B fromId(UUID value) {
        final UUID old = fromId;
        fromId = value;
        firePropertyChange("fromId", old, fromId);
        return (B) this;
    }


    public UUID getParentId() {
        return getFromId();
    }


    public final void setParentId(UUID value) {
        parentId( value );
    }



    public B parentId(UUID value) {
        return fromId( value );
    }


    public final void setToId(UUID value) {
        toId(value);
    }

    public B toId(UUID value) {
        final UUID old = toId;
        toId = value;
        firePropertyChange("toId", old, toId);
        return (B) this;
    }

    public final void setStartDate(Date value) {
        startDate(value);
    }

    public B startDate(Date value) {
        final Date old = startDate;
        startDate = value;
        firePropertyChange("startDate", old, startDate);
        return (B) this;
    }

    public final void setEndDate(Date value) {
        endDate(value);
    }

    public B endDate(Date value) {
        final Date old = endDate;
        endDate = value;
        firePropertyChange("endDate", old, endDate);
        return (B) this;
    }

    public final void setCreateDate(Date value) {
        createDate(value);
    }

    public B createDate(Date value) {
        final Date old = createDate;
        createDate = value;
        firePropertyChange("createDate", old, createDate);
        return (B) this;
    }

    public final void setLastUpdateDate(Date value) {
        lastUpdateDate(value);
    }

    public B lastUpdateDate(Date value) {
        final Date old = updateDate;
        updateDate = value;
        firePropertyChange("lastUpdateDate", old, updateDate);
        return (B) this;
    }

    public final void setValue(float value) {
        value(value);
    }

    public B value(float value) {
        final float old = value;
        this.value = value;
        firePropertyChange("value", old, value);
        return (B) this;
    }

    public final void setTimestamp(long value) {
        timestamp(value);
    }

    public B timestamp(long value) {
        final long old = timestamp;
        timestamp = value;
        firePropertyChange("timestampCount", old, value);
        return (B) this;
    }

    public long getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return "AssetBuilder " + this.getAssetType() + " "
                + this.getName() + " (" + this.getId() + ")";
    }

    private static <T> void copyMap(Map<String, T> source, Map<String, T> dest) {
        dest.clear();
        for (Map.Entry<String, T> entry : source.entrySet()) {
            dest.put(entry.getKey(), entry.getValue());
        }
    }

    protected AssetBuilder genericCopy( AbstractAsset source) {
        if ((!source.getAssetType().isA(getAssetType()))
                && (!getAssetType().isA(source.getAssetType()))) {
            throw new IllegalArgumentException("Asset type mismatch - " + getAssetType() + " builder cannot copy " + source.getAssetType());
        }
        setId(source.getId());
        setTimestamp(source.getTimestamp());
        setCreatorId(source.getCreatorId());
        setLastUpdaterId(source.getLastUpdaterId());
        setAclId(source.getAclId());
        setComment(source.getComment());
        setLastUpdate(source.getLastUpdate());
        setName(source.getName());
        setValue(source.getValue());
        setState(source.getState());
        setOwnerId(source.getOwnerId());
        setData(source.getData());
        setHomeId(source.getHomeId());
        setFromId(source.getFromId());
        setToId(source.getToId());

        setStartDate(source.getStartDate());
        setEndDate(source.getEndDate());

        setCreateDate(source.getCreateDate());
        setLastUpdateDate(source.getLastUpdateDate());
        copyMap(source.getLinkMap(), linkMap);
        copyMap(source.getDateMap(), dateMap);
        copyMap(source.getAttributeMap(), attributeMap);
        return (B) this;
    }

    public Integer getState() {
        return state;
    }

    public final void setState(int value) {
        state(value);
    }

    public B state(int value) {
        final int old = state;
        state = value;
        firePropertyChange("state", old, value);
        return (B) this;
    }

    public final void setId(UUID value) {
        id(value);
    }

    public B id(UUID value) {
        final UUID old = value;
        id = value;
        firePropertyChange("id", old, value);
        return (B) this;
    }

    public B putLink(String name, UUID value) {
        linkMap.put(name, value);
        return (B) this;
    }

    public Map<String, UUID> getLinkMap() {
        return roLinkMap;
    }

    public B putDate(String name, Date value) {
        dateMap.put(name, value);
        return (B) this;
    }

    public Map<String, Date> getDateMap() {
        return roDateMap;
    }

    public B putAttribute(String name, String value) {
        attributeMap.put(name, value);
        return (B) this;
    }

    public Map<String, String> getAttributeMap() {
        return roAttributeMap;
    }

    public B removeLink(String name) {
        linkMap.remove(name);
        return (B) this;
    }

    public B removeDate(String name) {
        dateMap.remove(name);
        return (B) this;
    }

    public B removeAttribute(String name) {
        attributeMap.remove(name);
        return (B) this;
    }

    public <T extends AssetBuilder> T narrow(Class<T> type) {
        return type.cast(this);
    }

    public <T extends AssetBuilder> T narrow() {
        return (T) this;
    }

    public abstract Asset build();
    public B copy( Asset value ) {
        return (B) genericCopy( (AbstractAsset) value );
    }

    @Override
    public Collection<String> checkIfValid() {
        return validatorBuilder.build( (B) this).checkIfValid();
    }

    public UUID getId() {
        return id;
    }

    private B parentInternal( Asset parent ) {
        fromId(parent.getId()).homeId(parent.getHomeId()).aclId(parent.getAclId());
        if (parent.getAssetType().equals(LittleHome.HOME_TYPE)) {
            homeId(parent.getId());
        }
        return (B) this;
    }

    public B parent(TreeNode parent) {
        return parentInternal( parent );
    }
    public B parent( LittleHome parent ) {
        return parentInternal( parent );
    }
}

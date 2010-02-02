/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.beans.PropertyChangeSupport;
import java.beans.PropertyChangeListener;
import java.io.Serializable;
import java.util.*;
import java.util.logging.Logger;


import littleware.asset.validate.AssetBuilderValidator;
import littleware.asset.validate.SimpleABValidator;
import littleware.base.*;

/**
 * Asset data-bucket base-class.
 */
public class SimpleAssetBuilder implements AssetBuilder {
    private static final Factory<UUID> uuidFactory = UUIDFactory.getFactory();
    private static final Logger log = Logger.getLogger(SimpleAssetBuilder.class.getName());
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
    private long transaction = -1L;
    private final AssetBuilderValidator validator;

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

    @Override
    public void addPropertyChangeListener(
            PropertyChangeListener listen_props) {
        propSupport.addPropertyChangeListener(listen_props);
    }

    @Override
    public void removePropertyChangeListener(
            PropertyChangeListener listen_props) {
        propSupport.removePropertyChangeListener(listen_props);
    }

    /**
     * Give subtypes direct access to the PropertyChangeSupport.
     * In general subtypes should prefer to call
     *     SimpleAssetBuilder.firePropertyChangeSupport
     * which also takes care of updating InSync
     */
    protected PropertyChangeSupport getPropertyChangeSupport() {
        return propSupport;
    }

    /**
     * Constructor does some basic initialization.
     * Sets type to AssetType.GENERIC, object-id to new id,
     * default AssetBuilderValidator
     */
    public SimpleAssetBuilder(AssetType assetType) {
        this( assetType, new SimpleABValidator() );
    }

    /**
     * Constructor with validator override
     */
    public SimpleAssetBuilder(AssetType assetType, AssetBuilderValidator validator ) {
        this.assetType = assetType;
        this.validator = validator;
    }


    @Override
    public String getName() {
        return name;
    }

    @Override
    public UUID getCreatorId() {
        return creatorId;
    }

    @Override
    public UUID getLastUpdaterId() {
        return lastUpdater;
    }

    @Override
    public UUID getAclId() {
        return aclId;
    }

    @Override
    public AssetType getAssetType() {
        return assetType;
    }

    @Override
    public String getComment() {
        return comment;
    }

    @Override
    public String getLastUpdate() {
        return lastUpdate;
    }

    @Override
    public String getData() {
        return data;
    }

    @Override
    public UUID getFromId() {
        return fromId;
    }

    @Override
    public UUID getToId() {
        return toId;
    }

    @Override
    public UUID getHomeId() {
        return homeId;
    }

    @Override
    public UUID getOwnerId() {
        return ownerId;
    }

    @Override
    public Date getStartDate() {
        return startDate;
    }

    @Override
    public Date getEndDate() {
        return endDate;
    }

    @Override
    public Date getCreateDate() {
        return createDate;
    }

    @Override
    public Date getLastUpdateDate() {
        return updateDate;
    }

    @Override
    public Float getValue() {
        return value;
    }

    /**
     * Name may not contain /
     *
     * @exception AssetException if given an illegal name
     */
    @Override
    public final void setName(String value) {
        name( value );
    }

    @Override
    public AssetBuilder name( String value ) {
        if ((-1 < value.indexOf('/')) || value.startsWith("..")) {
            throw new IllegalArgumentException("Illegal asset name: " + value);
        }
        final String sOld = name;
        name = value;
        firePropertyChange(value, sOld, value);
        return this;
    }

    @Override
    public final void setCreatorId(UUID value) {
        creatorId( value );
    }
    @Override
    public AssetBuilder creatorId( UUID value ) {
        final UUID old = creatorId;
        creatorId = value;
        firePropertyChange("creatorId", old, value);
        return this;
    }

    @Override
    public final void setLastUpdaterId(UUID value) {
        lastUpdaterId( value );
    }
    @Override
    public AssetBuilder lastUpdaterId( UUID value ) {
        final UUID old = lastUpdater;
        lastUpdater = value;
        firePropertyChange("lastUpdaterId", old, lastUpdater);
        return this;
    }

    @Override
    public final void setAclId(UUID value) {
        aclId( value );
    }
    @Override
    public AssetBuilder aclId( UUID value ) {
        final UUID old = aclId;
        aclId = value;
        firePropertyChange("aclId", old, aclId);
        return this;
    }

    /** Create a new owner object with x_owner as the only non-admin member */
    @Override
    public final void setOwnerId(UUID value) {
        ownerId( value );
    }
    @Override
    public AssetBuilder ownerId( UUID value ) {
        final UUID old = ownerId;
        ownerId = value;
        firePropertyChange("owner", old, ownerId);
        return this;
    }

    @Override
    public final void setComment(String value) {
        comment( value );
    }
    @Override
    public AssetBuilder comment( String value ) {
        final String old = comment;
        comment = value;
        firePropertyChange("comment", old, comment);
        return this;
    }

    @Override
    public final void setLastUpdate(String value) {
        lastUpdate( value );
    }
    @Override
    public AssetBuilder lastUpdate( String value ) {
        final String old = lastUpdate;
        lastUpdate = value;
        firePropertyChange("lastUpdate", old, value);
        return this;
    }

    @Override
    public final void setData(String value) {
        data( value );
    }
    @Override
    public AssetBuilder data( String value ) {
        if (value.length() > DATA_LIMIT) {
            throw new ValidationException("Data exceeds 1024 characters");
        }
        final String old = data;
        data = value;
        firePropertyChange("data", old, value);
        return this;
    }

    @Override
    public final void setHomeId(UUID value) {
        homeId( value );
    }
    @Override
    public AssetBuilder homeId( UUID value ) {
        final UUID old = homeId;
        homeId = value;
        firePropertyChange("homeId", old, homeId);
        return this;
    }

    @Override
    public final void setFromId(UUID value) {
        fromId( value );
    }
    @Override
    public AssetBuilder fromId( UUID value ) {
        final UUID old = fromId;
        fromId = value;
        firePropertyChange("fromId", old, fromId);
        return this;
    }

    @Override
    public final void setToId(UUID value) {
        toId( value );
    }
    @Override
    public AssetBuilder toId(UUID value) {
        final UUID old = toId;
        toId = value;
        firePropertyChange("toId", old, toId);
        return this;
    }

    @Override
    public final void setStartDate(Date value) {
        startDate( value );
    }
    @Override
    public AssetBuilder startDate( Date value ) {
        final Date old = startDate;
        startDate = value;
        firePropertyChange("startDate", old, startDate);
        return this;
    }

    @Override
    public final void setEndDate(Date value) {
        endDate( value );
    }
    @Override
    public AssetBuilder endDate( Date value ) {
        final Date old = endDate;
        endDate = value;
        firePropertyChange("endDate", old, endDate);
        return this;
    }

    @Override
    public final void setCreateDate(Date value) {
        createDate( value );
    }
    @Override
    public AssetBuilder createDate( Date value ) {
        final Date old = createDate;
        createDate = value;
        firePropertyChange("createDate", old, createDate);
        return this;
    }

    @Override
    public final void setLastUpdateDate(Date value) {
        lastUpdateDate( value );
    }
    @Override
    public AssetBuilder lastUpdateDate( Date value ) {
        final Date old = updateDate;
        updateDate = value;
        firePropertyChange("lastUpdateDate", old, updateDate);
        return this;
    }

    @Override
    public final void setValue(float value) {
        value( value );
    }
    @Override
    public AssetBuilder value( float value ) {
        final float old = value;
        this.value = value;
        firePropertyChange("value", old, value);
        return this;
    }

    @Override
    public final void setTransaction(long value) {
        transaction( value );
    }
    @Override
    public AssetBuilder transaction( long value ) {
        final long old = transaction;
        transaction = value;
        firePropertyChange("transactionCount", old, value);
        return this;
    }

    @Override
    public long getTransaction() {
        return transaction;
    }

    @Override
    public String toString() {
        return "Asset " + this.getAssetType() + " " +
                this.getName() + " (" + this.getId() + ")";
    }

    @Override
    public AssetBuilder copy(Asset source) {
        setId(source.getId());
        setTransaction(source.getTransaction());
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
        return this;
    }

    @Override
    public Integer getState() {
        return state;
    }

    @Override
    public final void setState(int value) {
        state( value );
    }
    @Override
    public AssetBuilder state( int value ) {
        final int old = state;
        state = value;
        firePropertyChange("state", old, value);
        return this;
    }

    @Override
    public final void setId(UUID value) {
        id( value );
    }
    @Override
    public AssetBuilder id( UUID value ) {
        final UUID old = value;
        id = value;
        firePropertyChange("id", old, value);
        return this;
    }


    public static class SimpleAsset extends SimpleCacheableObject implements Asset, Serializable {
        private UUID homeId;
        private UUID ownerId;
        private UUID fromId;
        private UUID toId;
        private UUID aclId;
        private String name;
        private Integer state;
        private Date createDate;
        private Date endDate;
        private Date startDate;
        private UUID creatorId;
        private String comment;
        private Date lastUpdateDate;
        private UUID lastUpdaterId;
        private String lastUpdate;
        private AssetType type;
        private Float value;
        private String data;

        /** Do nothing constructor for serilization */
        protected SimpleAsset() {}

        public SimpleAsset(AssetType type, UUID id, UUID homeId, UUID ownerId, UUID fromId, UUID toId,
                UUID aclId, long transaction, String name, Integer state,
                Date createDate, UUID creatorId, String comment,
                Date lastUpdateDate, UUID lastUpdaterId, String lastUpdate,
                Date startDate, Date endDate, Float value, String data ) {
            super( id, transaction );
            this.type = type;
            this.homeId = homeId;
            this.ownerId = ownerId;
            this.fromId = fromId;
            this.toId = toId;
            this.aclId = aclId;
            this.name = name;
            this.state = state;
            this.createDate = createDate;
            this.creatorId = creatorId;
            this.comment = comment;
            this.lastUpdateDate = lastUpdateDate;
            this.lastUpdaterId = lastUpdaterId;
            this.lastUpdate = lastUpdate;
            this.startDate = startDate;
            this.endDate = endDate;
            this.value = value;
            this.data = data;
        }

        /** Extract properties from builder */
        public SimpleAsset( AssetBuilder builder ) {
            this(builder.getAssetType(), builder.getId(), builder.getHomeId(),
                    builder.getOwnerId(), builder.getFromId(),
                    builder.getToId (),
                builder.getAclId(), builder.getTransaction(), builder.getName (),
                builder.getState(),
                builder.getCreateDate(), builder.getCreatorId(),
                builder.getComment(),
                builder.getLastUpdateDate(), builder.getLastUpdaterId(),
                builder.getLastUpdate(),
                builder.getStartDate(), builder.getEndDate(),
                builder.getValue(), builder.getData()
                );
        }


        /**
         * @return the homeId
         */
        @Override
        public UUID getHomeId() {
            return homeId;
        }

        /**
         * @return the ownerId
         */
        @Override
        public UUID getOwnerId() {
            return ownerId;
        }

        /**
         * @return the fromId
         */
        @Override
        public UUID getFromId() {
            return fromId;
        }

        /**
         * @return the toId
         */
        @Override
        public UUID getToId() {
            return toId;
        }

        /**
         * @return the aclId
         */
        @Override
        public UUID getAclId() {
            return aclId;
        }


        /**
         * @return the name
         */
        @Override
        public String getName() {
            return name;
        }

        /**
         * @return the state
         */
        @Override
        public Integer getState() {
            return state;
        }

        /**
         * @return the createDate
         */
        @Override
        public Date getCreateDate() {
            return createDate;
        }

        /**
         * @return the endDate
         */
        @Override
        public Date getEndDate() {
            return endDate;
        }

        /**
         * @return the startDate
         */
        @Override
        public Date getStartDate() {
            return startDate;
        }

        /**
         * @return the creatorId
         */
        @Override
        public UUID getCreatorId() {
            return creatorId;
        }

        /**
         * @return the comment
         */
        @Override
        public String getComment() {
            return comment;
        }

        /**
         * @return the lastUpdateDate
         */
        @Override
        public Date getLastUpdateDate() {
            return lastUpdateDate;
        }

        /**
         * @return the lastUpdaterId
         */
        @Override
        public UUID getLastUpdaterId() {
            return lastUpdaterId;
        }

        /**
         * @return the lastUpdate
         */
        @Override
        public String getLastUpdate() {
            return lastUpdate;
        }

        @Override
        public AssetType getAssetType() {
            return type;
        }

        @Override
        public String getData() {
            return data;
        }

        @Override
        public Float getValue() {
            return value;
        }

        @Override
        public <T extends Asset> T narrow(Class<T> type) {
            return type.cast(this);
        }

        @Override
        public <T extends Asset> T narrow() {
            return (T) this;
        }

        @Override
        public AssetBuilder copy() {
            return getAssetType().create().copy(this);
        }
        
    }

    @Override
    public Asset build() {
        validate();
        return new SimpleAsset( getAssetType(), getId(), getHomeId(), getOwnerId(),
                getFromId(), getToId(), getAclId(), getTransaction(),
                getName(), getState(),
                getCreateDate(), getCreatorId(), getComment(),
                getLastUpdateDate(), getLastUpdaterId(), getLastUpdate(),
                getStartDate(), getEndDate(), getValue(), getData() );
    }


    @Override
    public void validate() {
        validator.build(this).validate();
    }

    @Override
    public UUID getId() {
        return id;
    }

    @Override
    public AssetBuilder parent(Asset parent) {
        fromId(parent.getId()).homeId(parent.getHomeId()).aclId(parent.getAclId());
        if ( parent.getAssetType().equals( AssetType.HOME ) ) {
            homeId( parent.getId() );
        }
        return this;
    }
}


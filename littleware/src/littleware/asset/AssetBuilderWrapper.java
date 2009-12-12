/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset;

import java.beans.PropertyChangeListener;
import java.util.Date;
import java.util.UUID;

/**
 * Wrapper class to allow override of
 * arbitrary builder's properties in different
 * environments.  Default implementation of
 * every method just calls through to wrapped builder.
 */
public abstract class AssetBuilderWrapper implements AssetBuilder {
    private final AssetBuilder builder;

    protected AssetBuilderWrapper( AssetBuilder builder ) {
        this.builder = builder;
    }

    @Override
    public UUID getId() {
        return builder.getId();
    }

    @Override
    public void setId(UUID value) {
        builder.setId( value );
    }

    @Override
    public AssetBuilder id(UUID value) {
        return builder.id( value );
    }

    @Override
    public String getName() {
        return builder.getName();
    }

    @Override
    public void setName(String value) {
        builder.setName( value );
    }

    @Override
    public AssetBuilder name(String value) {
        return builder.name( value );
    }

    @Override
    public UUID getCreatorId() {
        return builder.getCreatorId();
    }

    @Override
    public void setCreatorId(UUID value) {
        builder.setCreatorId( value );
    }

    @Override
    public AssetBuilder creatorId(UUID value) {
        return builder.creatorId( value );
    }

    @Override
    public UUID getLastUpdaterId() {
        return builder.getLastUpdaterId();
    }

    @Override
    public void setLastUpdaterId(UUID value) {
        builder.setLastUpdaterId( value );
    }

    @Override
    public AssetBuilder lastUpdaterId(UUID value) {
        return builder.lastUpdaterId(value);
    }

    @Override
    public UUID getAclId() {
        return builder.getAclId();
    }

    @Override
    public void setAclId(UUID value) {
        builder.setAclId( value );
    }

    @Override
    public AssetBuilder aclId(UUID value) {
        return builder.aclId( value );
    }

    @Override
    public UUID getOwnerId() {
        return builder.getOwnerId();
    }

    @Override
    public void setOwnerId(UUID value) {
        builder.setOwnerId( value );
    }

    @Override
    public AssetBuilder ownerId(UUID value) {
        return builder.ownerId( value );
    }

    @Override
    public String getComment() {
        return builder.getComment();
    }

    @Override
    public void setComment(String value) {
        builder.setComment( value );
    }

    @Override
    public AssetBuilder comment(String value) {
        return builder.comment( value );
    }

    @Override
    public AssetType getAssetType() {
        return builder.getAssetType();
    }

    @Override
    public String getLastUpdate() {
        return builder.getLastUpdate();
    }

    @Override
    public void setLastUpdate(String value) {
        builder.setLastUpdate( value );
    }

    @Override
    public AssetBuilder lastUpdate(String value) {
        return builder.lastUpdate( value );
    }

    @Override
    public void setData(String value) {
        builder.setData( value );
    }

    @Override
    public String getData() {
        return builder.getData();
    }

    @Override
    public AssetBuilder data(String value) {
        return builder.data( value );
    }

    @Override
    public UUID getHomeId() {
        return builder.getHomeId();
    }

    @Override
    public void setHomeId(UUID value) {
        builder.setHomeId( value );
    }

    @Override
    public AssetBuilder homeId(UUID value) {
        return builder.homeId( value );
    }

    @Override
    public UUID getFromId() {
        return builder.getFromId();
    }

    @Override
    public void setFromId(UUID value) {
        builder.setFromId( value );
    }

    @Override
    public AssetBuilder fromId(UUID value) {
        return builder.fromId( value );
    }

    @Override
    public UUID getToId() {
        return builder.getToId();
    }

    @Override
    public void setToId(UUID value) {
        builder.setToId( value );
    }

    @Override
    public AssetBuilder toId(UUID value) {
        return builder.toId(value);
    }

    @Override
    public Date getStartDate() {
        return builder.getStartDate();
    }

    @Override
    public void setStartDate(Date value) {
        builder.setStartDate( value );
    }

    @Override
    public AssetBuilder startDate(Date value) {
        return builder.startDate( value );
    }

    @Override
    public Date getEndDate() {
        return builder.getEndDate();
    }

    @Override
    public void setEndDate(Date value) {
        builder.setEndDate( value );
    }

    @Override
    public AssetBuilder endDate(Date value) {
        return endDate( value );
    }

    @Override
    public Date getCreateDate() {
        return builder.getCreateDate();
    }

    @Override
    public void setCreateDate(Date value) {
        builder.setCreateDate( value );
    }

    @Override
    public AssetBuilder createDate(Date value) {
        return builder.createDate( value );
    }

    @Override
    public Date getLastUpdateDate() {
        return builder.getLastUpdateDate();
    }

    @Override
    public void setLastUpdateDate(Date value) {
        builder.setLastUpdateDate( value );
    }

    @Override
    public AssetBuilder lastUpdateDate(Date value) {
        return builder.lastUpdateDate(value);
    }

    @Override
    public Float getValue() {
        return builder.getValue();
    }

    @Override
    public void setValue(float value) {
        builder.setValue( value );
    }

    @Override
    public AssetBuilder value(float value) {
        return builder.value(value);
    }

    @Override
    public Integer getState() {
        return builder.getState();
    }

    @Override
    public void setState(int value) {
        builder.setState( value );
    }

    @Override
    public AssetBuilder state(int value) {
        return builder.state( value );
    }

    @Override
    public long getTransaction() {
        return builder.getTransaction();
    }

    @Override
    public void setTransaction(long value) {
        builder.setTransaction( value );
    }

    @Override
    public AssetBuilder transaction(long value) {
        return builder.transaction( value );
    }

    @Override
    public AssetBuilder copy(Asset value) {
        return builder.copy( value );
    }

    @Override
    public AssetBuilder parent(Asset parent) {
        return builder.parent( parent );
    }

    @Override
    public abstract Asset build();

    @Override
    public void addPropertyChangeListener(PropertyChangeListener listen_props) {
        builder.addPropertyChangeListener( listen_props );
    }

    @Override
    public void removePropertyChangeListener(PropertyChangeListener listen_props) {
        builder.removePropertyChangeListener( listen_props );
    }

    @Override
    public boolean validate() {
        return builder.validate();
    }

    @Override
    public void validateOrFail() {
        builder.validateOrFail();
    }

}

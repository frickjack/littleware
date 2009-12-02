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

import java.util.Date;
import java.util.UUID;
import littleware.base.LittleBean;
import littleware.base.Validator;


public interface AssetBuilder extends LittleBean, Validator {
    public UUID getId();
    public void setId( UUID value );
    public AssetBuilder  id( UUID value );

    /**
     * Name may not contain /
     *
     * @exception IllegalArgumentException if given an illegal name
     */
    public String getName();
    public void setName(String value);
    public AssetBuilder name( String value );

    public UUID getCreatorId();
    public void setCreatorId(UUID value);
    public AssetBuilder creatorId( UUID value );

    public UUID getLastUpdaterId();
    public void setLastUpdaterId(UUID value);
    public AssetBuilder lastUpdaterId( UUID value );

    public UUID getAclId();
    public void setAclId(UUID value);
    public AssetBuilder aclId( UUID value );
    
    public UUID getOwnerId();
    public void setOwnerId(UUID value);
    public AssetBuilder ownerId( UUID value );

    public String getComment();
    public void setComment(String value);
    public AssetBuilder comment( String value );

    public AssetType getAssetType();

    public String getLastUpdate();
    public void setLastUpdate(String value);
    public AssetBuilder lastUpdate( String value );

    /**
     * Set the data blob attached to this asset.
     *
     * @exception IllegalArgumentException if data is in invalid format
     *                for asset type, or if length exceeds 1024 characters
     */
    public void setData(String value);
    public String getData();
    public AssetBuilder data( String value );

    public UUID getHomeId();
    public void setHomeId(UUID value);
    public AssetBuilder homeId( UUID value );

    public UUID getFromId();
    public void setFromId(UUID value);
    public AssetBuilder fromId( UUID value );

    public UUID getToId();
    public void setToId(UUID value);
    public AssetBuilder toId( UUID value );

    public Date getStartDate();
    public void setStartDate(Date value);
    public AssetBuilder startDate( Date value );

    public Date getEndDate();
    public void setEndDate(Date value);
    public AssetBuilder endDate( Date value );

    public Date getCreateDate();
    public void setCreateDate(Date value);
    public AssetBuilder createDate( Date value );

    public Date getLastUpdateDate();
    public void setLastUpdateDate(Date value);
    public AssetBuilder lastUpdateDate( Date value );

    public Float getValue();
    public void setValue(float value);
    public AssetBuilder value( float value );

    public Integer getState();
    public void setState(int value);
    public AssetBuilder state( int value );

    public long getTransaction();
    public void setTransaction( long value );
    public AssetBuilder transaction( long value );

    /**
     * Copy all the builder properties from the given
     * template asset
     *
     * @exception IllegalArgumentException if ! value.getAssetType().equals( getAssetType() )
     */
    public AssetBuilder copy( Asset value );

    /**
     * Sets fromId to parent and copies parent's homeId and aclId
     */
    public AssetBuilder parent( Asset parent );

    /**
     * Validate then build asset then reset id
     *
     * @return new asset
     */
    public Asset build();
}

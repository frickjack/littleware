/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import java.io.Serializable;
import java.util.Date;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import javax.persistence.*;

import littleware.asset.Asset;
import littleware.asset.AssetBuilder;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;

/**
 * JPA Entity wrapper for an Asset
 */
@Entity(name = "Asset")
@Table(name = "asset" )
public class AssetEntity implements Serializable {
    private static final long serialVersionUID = 3354255899393564321L;
    private String osId = null;
    private String osName = null;
    private String osCreator = null;
    private String osLastUpdater = null;
    private String osAclId = null;
    private String osHomeId = null;
    private String osTypeId = null;
    private long olTransaction = 0;
    private String osOwnerId = null;
    private float ofValue = 0.0F;
    private int oiState = 0;
    private String osComment = null;
    private String osLastChange = null;
    private String osData = null;
    private String osFromId = null;
    private String osToId = null;
    private Date otCreate = null;
    private Date otUpdate = null;
    private Date otAccess = null;
    private Date otStart = null;
    private Date otEnd = null;

    private Set<AssetAttribute> attributeSet;

    @OneToMany(targetEntity=AssetAttribute.class,
            cascade=CascadeType.ALL,
            mappedBy="assetId")
    public Set<AssetAttribute> getAttributeSet() {
        return attributeSet;
    }
    public void setAttributeSet( Set<AssetAttribute> value ) {
        this.attributeSet = value;
    }
    

    private Set<AssetLink> linkSet;

    @OneToMany(targetEntity=AssetLink.class,
            cascade=CascadeType.ALL,
            mappedBy="assetId")
    public Set<AssetLink> getLinkSet() {
        return linkSet;
    }
    public void setLinkSet( Set<AssetLink> value ) {
        this.linkSet = value;
    }

    private Set<AssetDate> dateSet;

    @OneToMany(targetEntity=AssetDate.class,
            cascade=CascadeType.ALL,
            mappedBy="assetId")
    public Set<AssetDate> getDateSet() {
        return dateSet;
    }
    public void setDateSet( Set<AssetDate> value ) {
        this.dateSet = value;
    }


    @Id
    @Column(name = "s_id", length = 32)
    public String getObjectId() {
        return osId;
    }

    public void setObjectId(String sId) {
        osId = sId;
    }

    @Column(name = "s_name", length = 80)
    public String getName() {
        return osName;
    }

    public void setName(String sName) {
        osName = sName;
    }

    @Column(name = "s_id_creator", length = 32)
    public String getCreatorId() {
        return osCreator;
    }

    public void setCreatorId(String sCreator) {
        osCreator = sCreator;
    }

    @Column(name = "s_id_updater", length = 32)
    public String getLastUpdaterId() {
        return osLastUpdater;
    }

    public void setLastUpdaterId(String sLastUpdater) {
        osLastUpdater = sLastUpdater;
    }

    @Column(name = "s_id_acl", length = 32)
    public String getAclId() {
        return osAclId;
    }

    public void setAclId(String sAclId) {
        osAclId = sAclId;
    }

    @Column(name = "s_id_home", length = 32)
    public String getHomeId() {
        return osHomeId;
    }

    public void setHomeId(String sHomeId) {
        osHomeId = sHomeId;
    }

    @Column(name = "s_id_owner", length = 32)
    public String getOwnerId() {
        return osOwnerId;
    }

    public void setOwnerId(String sOwnerId) {
        osOwnerId = sOwnerId;
    }

    @Column(name = "s_pk_type", length = 32)
    public String getTypeId() {
        return osTypeId;
    }

    public void setTypeId(String sTypeId) {
        osTypeId = sTypeId;
    }

    @Column(name = "l_last_transaction")
    public long getLastTransaction() {
        return olTransaction;
    }

    public void setLastTransaction(long lTransaction) {
        olTransaction = lTransaction;
    }

    @Column(name = "f_value", precision = 16, scale = 4)
    public float getValue() {
        return ofValue;
    }

    public void setValue(float fValue) {
        ofValue = fValue;
    }

    @Column(name = "i_state")
    public int getState() {
        return oiState;
    }

    public void setState(int iState) {
        oiState = iState;
    }

    @Column(name = "s_comment", length = 256)
    public String getComment() {
        return osComment;
    }

    public void setComment(String sComment) {
        osComment = sComment;
    }

    @Column(name = "s_last_change", length = 128)
    public String getLastChange() {
        return osLastChange;
    }

    public void setLastChange(String sLastChange) {
        osLastChange = sLastChange;
    }

    @Column(name = "s_data", length = 128)
    public String getData() {
        return osData;
    }

    public void setData(String sData) {
        osData = sData;
    }

    @Column(name = "s_id_from", length = 32)
    public String getFromId() {
        return osFromId;
    }

    public void setFromId(String sFromId) {
        osFromId = sFromId;
    }

    @Column(name = "s_id_to", length = 32)
    public String getToId() {
        return osToId;
    }

    public void setToId(String sToId) {
        osToId = sToId;
    }

    @Column(name = "t_created")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimeCreated() {
        return otCreate;
    }

    public void setTimeCreated(Date tCreate) {
        otCreate = tCreate;
    }

    @Column(name = "t_updated")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getTimeUpdated() {
        return otUpdate;
    }

    public void setTimeUpdated(Date tUpdate) {
        otUpdate = tUpdate;
    }


    @Column(name = "t_start")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getStart() {
        return otStart;
    }

    public void setStart(Date tStart) {
        otStart = tStart;
    }

    @Column(name = "t_end")
    @Temporal(TemporalType.TIMESTAMP)
    public Date getEnd() {
        return otEnd;
    }

    public void setEnd(Date tEnd) {
        otEnd = tEnd;
    }

    private static UUID uuidOrNull(String sId) {
        if (null == sId) {
            return null;
        }
        return UUIDFactory.parseUUID(sId);
    }

    private static String stringOrNull(Object x) {
        if (null == x) {
            return null;
        }
        if (x instanceof UUID) {
            return UUIDFactory.makeCleanString((UUID) x);
        }
        return x.toString();
    }

    public Asset buildAsset() throws AssetException {
        final AssetBuilder builder = AssetType.getMember(UUIDFactory.parseUUID(getTypeId())).create();

        try {
            builder.setId(UUIDFactory.parseUUID(getObjectId()));
            builder.setName(getName());
            builder.setValue(getValue());
            builder.setState(getState());
            builder.setData(getData());
            builder.setFromId(uuidOrNull(getFromId()));
            builder.setToId(uuidOrNull(getToId()));
            builder.setAclId(uuidOrNull(getAclId()));
            builder.setComment(getComment());
            builder.setCreatorId(UUIDFactory.parseUUID(getCreatorId()));
            builder.setLastUpdaterId(UUIDFactory.parseUUID(getLastUpdaterId()));
            builder.setOwnerId(UUIDFactory.parseUUID(getOwnerId()));
            builder.setLastUpdate(getLastChange());
            builder.setHomeId(UUIDFactory.parseUUID(getHomeId()));
            builder.setCreateDate(getTimeCreated());
            builder.setEndDate(getEnd());
            builder.setStartDate(getStart());
            builder.setLastUpdateDate(getTimeUpdated());
            builder.setTransaction(getLastTransaction());
            for( AssetAttribute scan : getAttributeSet() ) {
                builder.putAttribute( scan.getKey(), scan.getValue() );
            }
            for( AssetLink scan : getLinkSet() ) {
                builder.putLink( scan.getKey(), UUIDFactory.parseUUID(scan.getValue()) );
            }
            for( AssetDate scan : getDateSet() ) {
                builder.putDate( scan.getKey(), scan.getValue() );
            }
            return builder.build();
        } catch (Exception ex) {
            throw new AssetBuildException("Not enough data to build asset", ex);
        }
    }

    /**
     * Build an entity from the given asset.
     * Usually workflow call when preparing to save an asset.
     *
     * @param asset to map to an entity to persist
     * @return AssetEntity ready to persist
     */
    public static AssetEntity buildEntity(Asset asset) {
        final AssetEntity entity = new AssetEntity();
        entity.setObjectId(UUIDFactory.makeCleanString(asset.getId()));
        entity.setTypeId(UUIDFactory.makeCleanString(asset.getAssetType().getObjectId()));
        entity.setName(asset.getName());
        entity.setValue(asset.getValue());
        entity.setState(asset.getState());
        entity.setData(asset.getData());
        entity.setFromId(stringOrNull(asset.getFromId()));
        entity.setToId(stringOrNull(asset.getToId()));
        entity.setAclId(stringOrNull(asset.getAclId()));
        entity.setComment(asset.getComment());
        entity.setCreatorId(stringOrNull(asset.getCreatorId()));
        entity.setLastUpdaterId(stringOrNull(asset.getLastUpdaterId()));
        entity.setOwnerId(UUIDFactory.makeCleanString(asset.getOwnerId()));
        entity.setLastChange(asset.getLastUpdate());
        entity.setHomeId(UUIDFactory.makeCleanString(asset.getHomeId()));
        entity.setTimeCreated(asset.getCreateDate());
        entity.setEnd(asset.getEndDate());
        entity.setStart(asset.getStartDate());
        entity.setTimeUpdated(asset.getLastUpdateDate());
        entity.setLastTransaction(asset.getTransaction());
        {
            final Set<AssetAttribute> attributeSet = new HashSet<AssetAttribute>();
            for ( final Map.Entry<String,String> scan : asset.getAttributeMap().entrySet() ) {
                attributeSet.add( AssetAttribute.build( scan.getKey(), scan.getValue() ));
            }
            entity.setAttributeSet( attributeSet );
        }
        {
            final Set<AssetLink> linkSet = new HashSet<AssetLink>();
            for ( final Map.Entry<String,UUID> scan : asset.getLinkMap().entrySet() ) {
                linkSet.add( AssetLink.build( scan.getKey(), UUIDFactory.makeCleanString(scan.getValue() )));
            }
            entity.setLinkSet(linkSet);
        }
        {
            final Set<AssetDate> dateSet = new HashSet<AssetDate>();
            for ( final Map.Entry<String,Date> scan : asset.getDateMap().entrySet() ) {
                dateSet.add( AssetDate.build( scan.getKey(), scan.getValue() ) );
            }
            entity.setDateSet( dateSet );
        }
        return entity;
    }
}

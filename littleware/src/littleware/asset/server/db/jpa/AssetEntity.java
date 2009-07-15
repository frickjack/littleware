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
import java.util.UUID;
import javax.persistence.*;

import littleware.asset.Asset;
import littleware.asset.AssetException;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;

/**
 * JPA Entity wrapper for an Asset
 */
@Entity(name="Asset")
@Table( name="asset", schema="littleware" )
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
    private int    oiState = 0;
    private String osComment = null;
    private String osLastChange = null;
    private String osData = null;
    private String osFromId = null;
    private String osToId = null;
    private Date   otCreate = null;
    private Date   otUpdate = null;
    private Date   otAccess = null;
    private Date   otStart = null;
    private Date   otEnd = null;

    
    @Id
    @Column( name="s_id", length=32 )
	public String      getObjectId () { return osId; }
    public void setObjectId ( String sId ) { osId = sId; }

    @Column( name="s_name", length=80 )
	public String      getName () { return osName; }
    public void        setName( String sName ) { osName = sName; }

    @Column( name="s_id_creator", length=32 )
	public String        getCreatorId () { return osCreator; }
    public void          setCreatorId( String sCreator ) {
        osCreator = sCreator;
    }

    @Column( name="s_id_updater", length=32 )
	public String        getLastUpdaterId () { return osLastUpdater; }
    public void          setLastUpdaterId( String sLastUpdater ) {
        osLastUpdater = sLastUpdater;
    }
    
    @Column( name="s_id_acl", length=32 )
	public String        getAclId () { return osAclId; }
    public void          setAclId( String sAclId ) { osAclId = sAclId; }

    @Column( name="s_id_home", length=32 )
	public String        getHomeId () { return osHomeId; }
    public void          setHomeId( String sHomeId ) { osHomeId = sHomeId; }

    @Column( name="s_id_owner", length=32 )
	public String        getOwnerId () { return osOwnerId; }
    public void          setOwnerId( String sOwnerId ) { osOwnerId = sOwnerId; }

    @Column( name="s_pk_type", length=32 )
	public String        getTypeId () { return osTypeId; }
    public void          setTypeId( String sTypeId ) { osTypeId = sTypeId; }


    @Column( name="l_last_transaction" )
    public long          getLastTransaction() {
        return olTransaction;
    }
    public void          setLastTransaction( long lTransaction ) {
        olTransaction = lTransaction;
    }

    @Column( name="f_value", precision=16, scale=4 )
    public float getValue () {
        return ofValue;
    }
    public void setValue( float fValue ) {
        ofValue = fValue;
    }

    @Column( name="i_state" )
    public int getState () {
        return oiState;
    }
    public void setState( int iState ) {
        oiState = iState;
    }


    @Column( name="s_comment", length=256 )
	public String        getComment () { return osComment; }
    public void          setComment( String sComment ) {
        osComment = sComment;
    }

    @Column( name="s_last_change", length=128 )
	public String        getLastChange () { return osLastChange; }
    public void          setLastChange( String sLastChange ) {
        osLastChange = sLastChange;
    }

    @Column( name="s_data", length=128 )
	public String        getData () { return osData; }
    public void          setData( String sData ) { osData = sData; }

    @Column( name="s_id_from", length=32 )
	public String        getFromId () { return osFromId; }
    public void          setFromId( String sFromId ) {
        osFromId = sFromId;
    }

    @Column( name="s_id_to", length=32 )
	public String        getToId () { return osToId; }
    public void          setToId( String sToId ) {
        osToId = sToId;
    }

    @Column( name="t_created" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getTimeCreated() {
        return otCreate;
    }
    public void    setTimeCreated( Date tCreate ) {
        otCreate = tCreate;
    }

    @Column( name="t_updated" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getTimeUpdated() {
        return otUpdate;
    }
    public void       setTimeUpdated ( Date tUpdate ) {
        otUpdate = tUpdate;
    }

    @Column( name="t_last_accessed" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getTimeAccessed() {
        return otAccess;
    }
    public void setTimeAccessed( Date tAccess ) {
        otAccess = tAccess;
    }

    @Column( name="t_start" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getStart() {
        return otStart;
    }
    public void   setStart( Date tStart ) {
        otStart = tStart;
    }

    @Column( name="t_end" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getEnd() {
        return otEnd;
    }
    public void   setEnd( Date tEnd ) {
        otEnd = tEnd;
    }

    private static UUID uuidOrNull( String sId ) {
        if ( null == sId ) {
            return null;
        }
        return UUIDFactory.parseUUID( sId );
    }
    private static String stringOrNull( Object x ) {
        if ( null == x ) {
            return null;
        }
        if ( x instanceof UUID ) {
            return UUIDFactory.makeCleanString( (UUID) x );
        }
        return x.toString();
    }

    public Asset buildAsset() throws AssetException {
        final Asset     aNew = AssetType.getMember( UUIDFactory.parseUUID( getTypeId() ) ).create();

        try {
            aNew.setObjectId( UUIDFactory.parseUUID( getObjectId() ) );
            aNew.setName( getName() );
            aNew.setValue( getValue() );
            aNew.setState( getState() );
            aNew.setData( getData() );
            aNew.setFromId( uuidOrNull( getFromId() ) );
            aNew.setToId( uuidOrNull( getToId() ) );
            aNew.setAclId( uuidOrNull( getAclId() ) );
            aNew.setComment( getComment() );
            aNew.setCreatorId( UUIDFactory.parseUUID( getCreatorId() ) );
            aNew.setLastUpdaterId( UUIDFactory.parseUUID( getLastUpdaterId() ) );
            aNew.setOwnerId( UUIDFactory.parseUUID( getOwnerId() ) );
            aNew.setLastUpdate( getLastChange() );
            aNew.setHomeId( UUIDFactory.parseUUID( getHomeId() ) );
            aNew.setCreateDate( getTimeCreated() );
            aNew.setEndDate( getEnd() );
            aNew.setStartDate( getStart() );
            aNew.setLastUpdateDate( getTimeUpdated() );
            aNew.setLastAccessDate( getTimeAccessed() );
            aNew.setTransactionCount( getLastTransaction() );
            aNew.setDirty( false );
            return aNew;
        } catch ( Exception ex ) {
            throw new AssetBuildException( "Not enough data to build asset", ex );
        }
    }

    /**
     * Build an entity from the given asset.
     * Usually workflow call when preparing to save an asset.
     *
     * @param aImport to map to an entity to persist
     * @return AssetEntity ready to persist
     */
    public static AssetEntity buildEntity( Asset aImport ) {
        final AssetEntity     entity = new AssetEntity();
        entity.setObjectId( UUIDFactory.makeCleanString( aImport.getObjectId() ) );
        entity.setTypeId( UUIDFactory.makeCleanString( aImport.getAssetType().getObjectId() ) );
        entity.setName(aImport.getName());
        entity.setValue(aImport.getValue());
        entity.setState( aImport.getState() );
        entity.setData(aImport.getData());
        entity.setFromId(stringOrNull(aImport.getFromId()));
        entity.setToId(stringOrNull(aImport.getToId()));
        entity.setAclId(stringOrNull(aImport.getAclId()));
        entity.setComment(aImport.getComment());
        entity.setCreatorId(stringOrNull(aImport.getCreatorId()));
        entity.setLastUpdaterId(stringOrNull(aImport.getLastUpdaterId()));
        entity.setOwnerId( UUIDFactory.makeCleanString( aImport.getOwnerId() ) );
        entity.setLastChange( aImport.getLastUpdate() );
        entity.setHomeId( UUIDFactory.makeCleanString( aImport.getHomeId() ) );
        entity.setTimeCreated(aImport.getCreateDate());
        entity.setEnd(aImport.getEndDate());
        entity.setStart(aImport.getStartDate());
        entity.setTimeUpdated(aImport.getLastUpdateDate());
        entity.setTimeAccessed(aImport.getLastAccessDate());
        entity.setLastTransaction( aImport.getTransactionCount() );
        return entity;
    }
}

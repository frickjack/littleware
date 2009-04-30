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
import javax.persistence.*;

import littleware.asset.Asset;

/**
 * JPA Entity wrapper for an Asset
 */
@Entity(name="Asset")
@Table( name="asset", schema="littleware" )
public class AssetEntity implements Serializable {
    private static final long serialVersionUID = 3354255899393564321L;
    private String osId;
    private String osName;
    private String osCreator;
    private String osLastUpdater;
    private String osAclId;
    private String osHomeId;
    private String osTypeId;
    private long olTransaction;
    private String osOwnerId;
    private float ofValue;
    private String osComment;
    private String osLastChange;
    private String osData;
    private String osFromId;
    private String osToId;
    private Date   otCreate;
    private Date   otUpdate;
    private Date   otAccess;
    private Date   otStart;
    private Date   otEnd;

    
    @Id
    @Column( name="s_id", length=32 )
	public String      getObjectId () { return osId; }

    @Column( name="s_name", length=80 )
	public String      getName () { return osName; }

    @Column( name="s_id_creator", length=32 )
	public String        getCreatorId () { return osCreator; }

    @Column( name="s_id_updater", length=32 )
	public String        getLastUpdaterId () { return osLastUpdater; }

    @Column( name="s_id_acl", length=32 )
	public String        getAclId () { return osAclId; }
    @Column( name="s_id_home", length=32 )
	public String        getHomeId () { return osHomeId; }
    @Column( name="s_id_owner", length=32 )
	public String        getOwnerId () { return osOwnerId; }
    @Column( name="s_pk_type", length=32 )
	public String        getTypeId () { return osTypeId; }
    @Column( name="l_last_transaction" )
    public long          getLastTransaction() {
        return olTransaction;
    }
    @Column( name="f_value", precision=16, scale=4 )
    public float getValue () {
        return ofValue;
    }
    @Column( name="s_comment", length=256 )
	public String        getComment () { return osComment; }
    @Column( name="s_last_change", length=128 )
	public String        getLastChange () { return osLastChange; }
    @Column( name="s_data", length=128 )
	public String        getData () { return osData; }
    @Column( name="s_id_from", length=32 )
	public String        getFromId () { return osFromId; }
    @Column( name="s_id_to", length=32 )
	public String        getToId () { return osToId; }
    @Column( name="t_created" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getTimeCreated() {
        return otCreate;
    }
    @Column( name="t_updated" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getTimeUpdated() {
        return otUpdate;
    }
    @Column( name="t_last_accessed" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getTimeAccessed() {
        return otAccess;
    }
    @Column( name="t_start" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getStart() {
        return otStart;
    }
    @Column( name="t_end" )
    @Temporal( TemporalType.TIMESTAMP )
    public Date       getEnd() {
        return otEnd;
    }



    public Asset buildAsset() {
        throw new UnsupportedOperationException( "Frickjack" );
    }

    /**
     * Build an entity from the given asset.
     * Usually workflow call when preparing to save an asset.
     *
     * @param aImport to map to an entity to persist
     * @return AssetEntity ready to persist
     */
    public static AssetEntity buildEntity( Asset aImport ) {
        return null;
    }
}

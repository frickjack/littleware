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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import javax.persistence.*;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;

/**
 * Read-only entity corresponds to AssetType representation in database
 */
@Entity(name = "AssetType")
@Table(name = "x_asset_type" )
public class AssetTypeEntity implements Serializable {

    private static final long serialVersionUID = -5941689038183213001L;
    private String osId;
    private String osName;
    private List<AssetTypeEntity> ovSubtype;
    private String osComment;
    private boolean obNameUnique;
    private String osParent;

    public AssetTypeEntity () {}

    /**
     * Used by AssetEntity.buildEntity
     *
     * @param atype to setup the entity with
     */
    public AssetTypeEntity( AssetType atype ) {
        osId = UUIDFactory.makeCleanString( atype.getObjectId() );
        osName = atype.getName();
    }

    @Id
    @Column(name = "s_id", length = 32)
    public String getObjectId() {
        return osId;
    }

    public void setObjectId(String sId) {
        osId = sId;
    }

    @Column(name = "s_name", length = 32)
    public String getName() {
        return osName;
    }

    public void setName(String sName) {
        osName = sName;
    }

    @Column(name = "b_name_unique")
    public boolean getNameUnique() {
        return obNameUnique;
    }

    public void setNameUnique(boolean value) {
        obNameUnique = value;
    }


    @Column(name = "s_comment", length = 128)
    public String getComment() {
        return osComment;
    }

    public void setComment(String value) {
        osComment = value;
    }

    @Column(name = "x_parent_type", length = 32)
    public String getParentType() {
        return osParent;
    }

    public void setParentType(String value) {
        osParent = value;
    }

    @ManyToMany()
    @JoinTable(name = "x_asset_type_tree", 
    joinColumns = {@JoinColumn(name = "s_ancestor_id")},
    inverseJoinColumns = {@JoinColumn(name = "s_descendent_id")})
    public List<AssetTypeEntity> getSubtypeList() {
        return ovSubtype;
    }

    public void setSubtypeList(List<AssetTypeEntity> vSubtype) { 
        if ( null != vSubtype ) {
            Whatever.get().check( "Setting consistent subtypes", 
                (new HashSet<>( vSubtype )).size() == vSubtype.size() 
                );
        }
        ovSubtype = vSubtype;
    }

    @Override
    public boolean equals( Object other ) {
        return (null != other) 
                && (other instanceof AssetTypeEntity)
                && ((AssetTypeEntity) other).getObjectId().equals( getObjectId() );
    }

    @Override
    public int hashCode() {
        int hash = 5;
        hash = 79 * hash + (this.osId != null ? this.osId.hashCode() : 0);
        return hash;
    }

    public static AssetTypeEntity buildEntity( AssetType assetType, Collection<AssetTypeEntity> subType ) {
        final AssetTypeEntity entity = new AssetTypeEntity();
        entity.setName( assetType.getName() );
        entity.setObjectId( UUIDFactory.makeCleanString(assetType.getObjectId() ) );
        final List subtypeList = new ArrayList<AssetTypeEntity>();
        subtypeList.addAll( subType );
        entity.setSubtypeList( subtypeList );
        entity.setComment( "Auto created by app engine" );
        entity.setNameUnique( assetType.isNameUnique() );
        if( assetType.getSuperType().isSet() ) {
            entity.setParentType( UUIDFactory.makeCleanString( assetType.getSuperType().get().getObjectId() ));
        } else {
            entity.setParentType( null );
        }
        return entity;
    }
}

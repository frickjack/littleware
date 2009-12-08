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
import java.util.List;
import javax.persistence.*;
import littleware.asset.AssetType;
import littleware.base.UUIDFactory;

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

    @OneToMany()
    @JoinTable(name = "x_asset_type_tree", 
    joinColumns = {@JoinColumn(name = "s_ancestor_id")},
    inverseJoinColumns = {@JoinColumn(name = "s_descendent_id")})
    public List<AssetTypeEntity> getSubtypeList() {
        return ovSubtype;
    }

    public void setSubtypeList(List<AssetTypeEntity> vSubtype) {
        ovSubtype = vSubtype;
    }
}

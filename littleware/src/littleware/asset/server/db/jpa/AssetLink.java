/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.jpa;

import javax.persistence.*;

@Entity(name = "AssetLink")
@Table(name = "asset_link")
public class AssetLink {

    @Id
    @Column(name = "i_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @Column( name="s_asset_id", length=32 )
    private String assetId;

    @Column( name="s_key", length=20)
    private String key;

    @Column( name="s_value", length=32 )
    private String value;

    public AssetLink() {}
    private AssetLink( String key, String value ) {
        this.key = key;
        this.value = value;
    }

    public static AssetLink build( String key, String value ) {
        return new AssetLink( key, value );
    }


    public long getId() { return id; }
    public String getAssetId() { return assetId; }
    public String getKey() { return key; }
    public String getValue() { return value; }


    @Override
    public boolean equals(Object obj) {
        if( (obj != null) && (obj instanceof AssetLink) ) {
            final AssetLink other = (AssetLink) obj;
            return (this.id == other.id) && this.key.equals(other.key);
        }
        return false;
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (int) (this.id ^ (this.id >>> 32));
        hash = 29 * hash + (this.key != null ? this.key.hashCode() : 0);
        return hash;
    }
}

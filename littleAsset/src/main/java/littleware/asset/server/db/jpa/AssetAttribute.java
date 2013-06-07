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

import java.io.Serializable;
import javax.persistence.*;

@Entity(name = "AssetAttribute")
@Table(name = "asset_attr")
public class AssetAttribute implements Serializable {

    @Id
    @Column(name = "i_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn( name="s_asset_id", nullable=false )
    private AssetEntity asset;

    @Column( name="s_key", length=20)
    private String key;

    @Column( name="s_value", length=128 )
    private String value;

    public AssetAttribute() {}
    private AssetAttribute( AssetEntity asset, String key, String value ) {
        this.asset = asset;
        this.key = key;
        this.value = value;
    }

    public static AssetAttribute build( AssetEntity asset, String key, String value ) {
        return new AssetAttribute( asset, key, value );
    }

    
    public long getId() { return id; }
    public String getKey() { return key; }
    public String getValue() { return value; }
    public void   setValue( String value ) { this.value = value; }


    @Override
    public boolean equals(Object obj) {
        if( (obj != null) && (obj instanceof AssetAttribute) ) {
            final AssetAttribute other = (AssetAttribute) obj;
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

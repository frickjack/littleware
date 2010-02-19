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

import java.util.Date;
import javax.persistence.*;

@Entity(name = "AssetDate")
@Table(name = "asset_date")
public class AssetDate {

    @Id
    @Column(name = "i_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    @ManyToOne
    @JoinColumn( name="s_asset_id" )
    private AssetEntity asset;

    @Column( name="s_key", length=20)
    private String key;

    @Column( name="t_value" )
    @Temporal(TemporalType.TIMESTAMP)
    private Date value;

    public AssetDate() {}
    private AssetDate( String key, Date value ) {
        this.key = key;
        this.value = value;
    }

    public static AssetDate build( String key, Date value ) {
        return new AssetDate( key, value );
    }


    public long getId() { return id; }
    public String getKey() { return key; }
    public Date getValue() { return value; }


    @Override
    public boolean equals(Object obj) {
        if( (obj != null) && (obj instanceof AssetDate) ) {
            final AssetDate other = (AssetDate) obj;
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

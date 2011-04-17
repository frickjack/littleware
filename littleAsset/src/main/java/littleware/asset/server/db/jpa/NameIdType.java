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

import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetType;
import littleware.asset.GenericAsset;
import littleware.base.UUIDFactory;

/**
 * Simple POJO to fascilitate JPLQ queries that return (name,id) pairs.
 */
public class NameIdType {
    private static final Logger log = Logger.getLogger( NameIdType.class.getName() );
    private final String osName;
    private final UUID   ouId;
    private AssetType oatype;

    /**
     * Inject the name and id (UUID string rep) properties
     */
    public NameIdType( String sName, String sId, String sTypeId ) {
        osName = sName;
        ouId = UUIDFactory.parseUUID( sId );
        try {
            oatype = AssetType.getMember( UUIDFactory.parseUUID( sTypeId ) );
        } catch ( Exception ex ) {
            // Don't freak out! ... throw new IllegalArgumentException( "Invalid asset type", ex );
            log.log( Level.WARNING, "Loading unknown type as GenericAsset.GENERIC: {0}", sTypeId);
            oatype = GenericAsset.GENERIC;
        }
    }

    public String getName() { return osName; }
    public UUID   getId() { return ouId; }
    public AssetType getType() { return oatype; }

    @Override
    public boolean equals( Object xOther ) {
        return (xOther instanceof NameIdType)
                && ((NameIdType)xOther).getName().equals(getName ())
                && ((NameIdType)xOther).getId().equals(getId());
    }

    @Override
    public int hashCode() {
        int hash = 7;
        hash = 29 * hash + (this.osName != null ? this.osName.hashCode() : 0);
        hash = 29 * hash + (this.ouId != null ? this.ouId.hashCode() : 0);
        return hash;
    }
    
}

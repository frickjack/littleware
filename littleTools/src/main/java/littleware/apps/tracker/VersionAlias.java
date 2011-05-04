/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker;

import java.util.Date;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.asset.AssetType;
import littleware.asset.LinkAsset;
import littleware.base.UUIDFactory;



public interface VersionAlias extends LinkAsset {
    /**
     * Alias for getFromId
     */
    public UUID getProductId();
    /**
     * Alias for getToId - returns id of version or version-alias node
     */
    public UUID getVersionId();

    @Override
    public VABuilder copy();

    //----------------------------------------------------------------

    public interface VABuilder extends LinkAsset.LinkBuilder {
        @Override
        public VABuilder name( String value );

        /** Type-safe alias for parent */
        public VABuilder product( Product value );
        /** Id of version to reference - identical to toId */
        public VABuilder version( UUID value );
        public VABuilder version( Version value );
        
        @Override
        public VABuilder copy( Asset source );
        
        @Override
        public VersionAlias build();
        
        @Override
        public VABuilder creatorId(UUID value);

        @Override
        public VABuilder lastUpdaterId(UUID value);

        @Override
        public VABuilder aclId(UUID value);

        @Override
        public VABuilder ownerId(UUID value);

        @Override
        public VABuilder comment(String value);

        @Override
        public VABuilder lastUpdate(String value);

        @Override
        public VABuilder homeId(UUID value);


        @Override
        public VABuilder createDate(Date value);

        @Override
        public VABuilder lastUpdateDate(Date value);


        @Override
        public VABuilder timestamp(long value);

        @Override
        public VABuilder fromId( UUID value );
        @Override
        public VABuilder from( Asset value );


    }
    
    //----------------------------------------------------------------

    public static final AssetType VA_TYPE = new AssetType(
            UUIDFactory.parseUUID("1CD26A5FDBD141D2904AACCEC3D0B3F2"),
            "littleware.VersionAlias", LinkAsset.LINK_TYPE
            );
    
}

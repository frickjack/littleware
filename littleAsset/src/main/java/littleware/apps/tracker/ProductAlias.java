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
import littleware.base.Maybe;
import littleware.base.UUIDFactory;

/**
 *
 * @author pasquini
 */
public interface ProductAlias extends LinkAsset {
    /**
     * Alias for getToId - reference to product or product alias
     */
    public UUID getProductId();
    
    @Override
    public PABuilder copy();
    
    public interface PABuilder extends LinkAsset.LinkBuilder {
        @Override
        public PABuilder name( String value );
        
        /**
         * Alias for setToId
         */
        public PABuilder product( Product value );
        public PABuilder product( ProductAlias value );
        
        @Override
        public PABuilder copy( Asset source );
        
        @Override
        public ProductAlias build();

        @Override
        public PABuilder creatorId(UUID value);

        @Override
        public PABuilder lastUpdaterId(UUID value);

        @Override
        public PABuilder aclId(UUID value);

        @Override
        public PABuilder ownerId(UUID value);

        @Override
        public PABuilder comment(String value);

        @Override
        public PABuilder lastUpdate(String value);

        @Override
        public PABuilder homeId(UUID value);

        @Override
        public PABuilder createDate(Date value);

        @Override
        public PABuilder lastUpdateDate(Date value);

        @Override
        public PABuilder fromId( UUID value );

        @Override
        public PABuilder timestamp(long value);

    }
    
    public static final AssetType PAType = new AssetType(UUIDFactory.parseUUID("1A827A6E61AE45939DE46B62F69B93B2"),
            "littleware.ProductAlias", LinkAsset.LINK_TYPE );
}

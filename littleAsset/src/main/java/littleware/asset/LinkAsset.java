/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.UUID;
import littleware.base.UUIDFactory;

/**
 * Specialized asset that just links from one asset to another
 */
public interface LinkAsset extends Asset {

    /**
     * Destination of directed link - may be null
     */
    public UUID getToId();

    /**
     * Shortcut for a.getAssetType().create().copy( a )
     */
    @Override
    public LinkBuilder copy();

    /** LINK assset-type */
    public static final AssetType LINK_TYPE = new AssetType(UUIDFactory.parseUUID("926D122F82FE4F28A8F5C790E6733665"),
            "littleware.LINK");


    //------------------------------------------------------

    public interface LinkBuilder extends AssetBuilder {

        public UUID getFromId();

        public void setFromId(UUID value);

        public LinkBuilder fromId(UUID value);

        public UUID getToId();

        public void setToId(UUID value);

        public LinkBuilder toId(UUID value);

        @Override
        public LinkAsset build();

        @Override
        public LinkBuilder id(UUID value);

        @Override
        public LinkBuilder name(String value);

        @Override
        public LinkBuilder creatorId(UUID value);

        @Override
        public LinkBuilder lastUpdaterId(UUID value);

        @Override
        public LinkBuilder aclId(UUID value);

        @Override
        public LinkBuilder ownerId(UUID value);

        @Override
        public LinkBuilder lastUpdate(String value);

        @Override
        public LinkBuilder homeId(UUID value);

        @Override
        public LinkBuilder timestamp(long value);

    }
}

/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset;

import java.util.UUID;

/**
 * Specialized asset that just links from one asset to another
 */
public interface LinkAsset extends Asset {

    /**
     * Source of directed link this asset represents - may only be null
     * for HOME type assets
     */
    public UUID getFromId();

    /**
     * Destination of directed link - may be null
     */
    public UUID getToId();

    /**
     * Shortcut for a.getAssetType().create().copy( a )
     */
    @Override
    public LinkBuilder copy();

    /** Cast this to the specified asset type ... little safer than simple cast */
    public <T extends LinkAsset> T narrow(Class<T> type);

    public <T extends LinkAsset> T narrow();


    public interface LinkBuilder extends AssetBuilder {

        public UUID getFromId();

        public void setFromId(UUID value);

        public AssetBuilder fromId(UUID value);

        public UUID getToId();

        public void setToId(UUID value);

        public AssetBuilder toId(UUID value);

        @Override
        public LinkAsset build();
    }
}

/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.image.client;

import com.google.inject.ImplementedBy;
import java.awt.image.BufferedImage;
import java.util.UUID;
import littleware.apps.image.ImageManager;
import littleware.apps.image.ThumbManager;
import littleware.apps.image.ThumbManager.Thumb;
import littleware.base.Maybe;

/**
 * Manager for combination in-memory and disk image cache.
 */
@ImplementedBy(SimpleImageCache.class)
public interface ImageCache {
    public interface CacheEntry {
        public UUID getAssetId();
        public ImageManager.SizeOption  getImageSize();
        /**
         * true if the entry was placed into the cache,
         * false if cache does not contain an entry for this key
         */
        public boolean    isInCache();
        /**
         * Throws IllegalStateException if isInCache == false
         */
        public Maybe<BufferedImage>  getImage();
    }

    public CacheEntry cacheGet( UUID assetId, ImageManager.SizeOption size );

    /**
     * Return maybe.isEmpty if thumb is not in cache
     */
    public Maybe<ThumbManager.Thumb>  getThumb( UUID assetId );
    /**
     * Add a non-null thumb to the cache
     *
     * @param thumb must be a valid thumbnail
     */
    public void putThumb( UUID assetId, Thumb thumb );
    public void cachePut( UUID assetId, ImageManager.SizeOption size, Maybe<BufferedImage> maybeImage );
    public void remove( UUID assetId );
    public void remove( UUID assetId, ImageManager.SizeOption size );
}

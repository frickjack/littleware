/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.misc.client;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.Arrays;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import littleware.apps.misc.ImageManager;
import littleware.apps.misc.ImageManager.SizeOption;
import littleware.asset.AssetSearchManager;
import littleware.base.Cache;
import littleware.base.Maybe;
import littleware.base.PropertiesLoader;
import littleware.base.SimpleCache;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;

@Singleton
public class SimpleImageCache implements ImageCache {

    private static final Logger log = Logger.getLogger(SimpleImageCache.class.getName());

    @Override
    public void remove(UUID assetId) {
        for (ImageManager.SizeOption size : ImageManager.SizeOption.values()) {
            remove(assetId, size);
        }
    }

    @Override
    public void remove(UUID assetId, SizeOption size) {
        removeFromCache(new CacheKey(assetId, size, dirCache));
    }

    private static class CacheKey {

        public final UUID id;
        public final ImageManager.SizeOption size;
        public final File infoFile;
        public final File pngFile;

        public CacheKey(UUID id, ImageManager.SizeOption size, File cacheDirectory) {
            this.id = id;
            this.size = size;
            infoFile = new File(cacheDirectory, UUIDFactory.makeCleanString(id) + size + ".info");
            pngFile = new File(cacheDirectory, UUIDFactory.makeCleanString(id) + size + ".png");
        }

        @Override
        public boolean equals(Object other) {
            return (other instanceof CacheKey)
                    && ((CacheKey) other).id.equals(id)
                    && ((CacheKey) other).size.equals(size);
        }

        @Override
        public int hashCode() {
            int hash = 7;
            hash = 67 * hash + (this.id != null ? this.id.hashCode() : 0);
            hash = 67 * hash + (this.size != null ? this.size.hashCode() : 0);
            return hash;
        }

        @Override
        public String toString() {
            return "(" + id + "," + size + ")";
        }
    }

    private static class SimpleEntry implements CacheEntry {

        private final Maybe<BufferedImage> maybeImage;
        private final CacheKey key;
        private final boolean isInCache;

        public SimpleEntry(CacheKey key) {
            this.key = key;
            this.isInCache = false;
            this.maybeImage = Maybe.empty();
        }

        public SimpleEntry(CacheKey key, Maybe<BufferedImage> maybeImage) {
            this.key = key;
            this.maybeImage = maybeImage;
            this.isInCache = true;
        }

        @Override
        public UUID getAssetId() {
            return key.id;
        }

        @Override
        public SizeOption getImageSize() {
            return key.size;
        }

        @Override
        public boolean isInCache() {
            return isInCache;
        }

        @Override
        public Maybe<BufferedImage> getImage() {
            if (isInCache) {
                return maybeImage;
            }
            throw new IllegalStateException("CacheEntry is not in cache");
        }
    }
    private final Cache<CacheKey, Maybe<BufferedImage>> cache =
            new SimpleCache<CacheKey, Maybe<BufferedImage>>(900, 1000);
    private final File dirCache = new File(PropertiesLoader.get().getLittleHome().getOr(new File(System.getProperty("java.io.tmpdir"))), "imageCache");

    {
        if (!dirCache.exists()) {
            dirCache.mkdirs();
        }
    }
    private final Whatever whatever = Whatever.get();
    private final AssetSearchManager search;

    @Inject
    public SimpleImageCache(AssetSearchManager search) {
        this.search = search;
    }

    /**
     * Remove from both in-memory and on-disk cache
     */
    private void removeFromCache(CacheKey key) {
        cache.remove(key);
        for (File file : Arrays.asList(key.infoFile, key.pngFile)) {
            try {
                if (file.exists()) {
                    //log.log( Level.FINE, "Deleting cache file: " + file );
                    file.delete();
                }
            } catch (Exception ex) {
            }
        }
    }

    @Override
    public CacheEntry cacheGet(UUID assetId, SizeOption size) {
        final CacheKey key = new CacheKey(assetId, size, dirCache);
        { // check in-memory cache
            final Maybe<BufferedImage> maybe = cache.get(key);

            if ( null != maybe ) {
                return new SimpleEntry(key, maybe);
            }
        }

        // check disk cache
        if ((!key.infoFile.canRead()) || (!key.pngFile.canRead())) {
            return new SimpleEntry(key);
        }
        long transaction = -1;
        // load transaction expiration from .info file
        {
            BufferedReader reader = null;
            try {
                reader = new BufferedReader(new FileReader(key.infoFile));
                transaction = Long.parseLong(reader.readLine().trim());
                whatever.close(reader);
            } catch (IOException ex) {
                log.log(Level.WARNING, "Failed to process cache data file: " + key.infoFile);
                whatever.close(reader);
            }
        }

        Maybe<BufferedImage> maybe = Maybe.empty();
        if (transaction > 0) {
            // check image-save transaction against asset's current transaction value
            try {
                if (search.getAsset(assetId).get().getTransaction() <= transaction) {
                    maybe = Maybe.emptyIfNull(ImageIO.read(key.pngFile));
                }
            } catch (Exception ex) {
                log.log(Level.INFO, "Failed to verify image cache file " + key.pngFile, ex);
            }
        }
        if (maybe.isSet()) {
            // add disk data to in-memory cache, return cache-hit
            cache.put(key, maybe);
            return new SimpleEntry(key, maybe);
        } else {
            // erase disk data, return cache-miss
            removeFromCache(key);
            return new SimpleEntry(key);
        }
    }

    @Override
    public void cachePut(UUID assetId, SizeOption size, Maybe<BufferedImage> maybe) {
        final CacheKey key = new CacheKey(assetId, size, dirCache);
        removeFromCache(key);  // clear memory and disk cache
        if (!dirCache.exists()) { // user may have deleted this
            dirCache.mkdirs();
        }

        if (maybe.isEmpty()) {
            cache.put(key, maybe);
            return;
        }
        cache.put(key, maybe);

        if (maybe.isEmpty()) {
            return;
        }

        try { // write out file
            final Writer writer = new FileWriter(key.infoFile);
            try {
                writer.write("" + search.getAsset(assetId).get().getTransaction() + Whatever.NEWLINE);
            } finally {
                writer.close();
            }
            ImageIO.write(maybe.get(), "png", key.pngFile);
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to update cache for " + key, ex);
        }
    }
}

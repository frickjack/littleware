/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.image.test;

import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import littleware.apps.image.ImageManager;
import littleware.apps.image.client.ImageCache;
import littleware.base.Maybe;
import littleware.test.LittleTest;

/**
 * Test internal ImageCache
 */
public class ImageCacheTester extends LittleTest {

    private static final Logger log = Logger.getLogger(ImageCacheTester.class.getName());
    private final ImageCache cache;

    @Inject
    public ImageCacheTester(ImageCache cache) {
        setName("testImageCache");
        this.cache = cache;
    }

    public void testImageCache() {
        final UUID id = UUID.randomUUID();
        try {
            assertTrue("Test id not in cache",
                    !cache.cacheGet(id, ImageManager.SizeOption.r64x64).isInCache());
            cache.cachePut(id, ImageManager.SizeOption.r64x64, Maybe.empty(BufferedImage.class));
            {
                final ImageCache.CacheEntry entry = cache.cacheGet(id, ImageManager.SizeOption.r64x64);
                assertTrue("Test id in cache",
                        entry.isInCache());
                assertTrue("No image in cache",
                        entry.getImage().isEmpty());
                assertTrue("Non-test resolution not in cache",
                        !cache.cacheGet(id, ImageManager.SizeOption.r128x128).isInCache());
            }

            final BufferedImage testImage = ImageIO.read(ThumbManagerTester.class.getClassLoader().getResource("littleware/apps/misc/test/testImage.png"));
            cache.cachePut(id, ImageManager.SizeOption.r64x64, Maybe.something(testImage));
            {
                final ImageCache.CacheEntry entry = cache.cacheGet(id, ImageManager.SizeOption.r64x64);
                assertTrue("Test id in cache",
                        entry.isInCache());
                assertTrue("Image in cache",
                        entry.getImage().isSet());
                assertTrue("Non-test resolution not in cache",
                        !cache.cacheGet(id, ImageManager.SizeOption.r128x128).isInCache());
            }

            cache.remove(id);
            {
                final ImageCache.CacheEntry entry = cache.cacheGet(id, ImageManager.SizeOption.r64x64);
                assertTrue("Image removed from cache",
                        !entry.isInCache());
                try {
                    entry.getImage();
                    fail("Attempt to getImage on entry not in cache should throw exception");
                } catch (Exception ex) {
                }
            }

        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        } finally {
            cache.remove(id);
        }
    }
}

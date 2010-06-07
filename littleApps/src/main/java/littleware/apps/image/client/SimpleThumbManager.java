/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.image.client;

import com.google.inject.Inject;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import littleware.apps.image.ImageManager;
import littleware.apps.image.ThumbManager;
import littleware.asset.AssetSearchManager;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.PropertiesLoader;

/**
 * SimpleImageManager based implementation of ThumbManager
 */
public class SimpleThumbManager implements ThumbManager {

    private static final Logger log = Logger.getLogger(SimpleThumbManager.class.getName());
    private final AssetSearchManager search;

    protected static class SimpleThumb implements ThumbManager.Thumb {

        private final boolean isFallback;
        private final BufferedImage img;
        private Maybe<ImageIcon> maybeIcon = Maybe.empty();

        private synchronized ImageIcon renderIcon() {
            if (maybeIcon.isSet()) {
                return maybeIcon.get();
            }
            maybeIcon = Maybe.something(new ImageIcon(img));
            return maybeIcon.get();
        }

        public SimpleThumb(BufferedImage img) {
            this.img = img;
            isFallback = false;
        }

        public SimpleThumb(BufferedImage img, boolean bFallback) {
            this.img = img;
            isFallback = bFallback;
        }

        @Override
        public boolean isFallback() {
            return isFallback;
        }

        @Override
        public BufferedImage getThumb() {
            return img;
        }

        @Override
        public ImageIcon getIcon() {
            if (maybeIcon.isSet()) {
                return maybeIcon.get();
            }
            return renderIcon();
        }
    }

    protected static class CacheInfo {

        private final Thumb thumb;
        private final Date birth;

        public Date getBirth() {
            return birth;
        }

        public Thumb getThumb() {
            return thumb;
        }

        public CacheInfo(Thumb thumb, Date birth) {
            this.thumb = thumb;
            this.birth = birth;
        }
    }
    private final ImageCache cache;
    private final ImageManager imageManager;
    private final File dirCache = new File(PropertiesLoader.get().getLittleHome().getOr(new File(System.getProperty("java.io.tmpdir"))), "thumbCache");
    private BufferedImage defaultImage;

    {
        try {
            defaultImage =
                    ImageIO.read(SimpleThumbManager.class.getClassLoader().getResource("littleware/apps/misc/client/defaultThumb.png"));
        } catch (IOException ex) {
            throw new AssertionFailedException("Failed to load default image", ex);
        }
    }
    private final int oiWidth = ImageManager.SizeOption.r64x64.getWidth();
    private final int oiHeight = ImageManager.SizeOption.r64x64.getHeight();
    private ThumbManager.Thumb defaultThumb = new SimpleThumb(defaultImage, true);

    @Inject
    public SimpleThumbManager(ImageManager mgrImage, AssetSearchManager search, ImageCache cache) {
        imageManager = mgrImage;
        this.search = search;
        this.cache = cache;

        try {
            final Maybe<BufferedImage> maybeDefault = imageManager.loadImage(search.getHomeAssetIds().get("littleware.home"), ImageManager.SizeOption.r64x64);
            if (maybeDefault.isSet()) {
                setDefault(maybeDefault.get());
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed to load default thumb off littleware.home asset", ex);
        }
    }

    @Override
    public void clearCache(UUID assetId) {
        cache.remove(assetId);
    }

    @Override
    public Thumb loadThumb(UUID assetId) throws BaseException, GeneralSecurityException, IOException {
        {
            final Maybe<Thumb> maybeThumb = cache.getThumb(assetId);
            if (maybeThumb.isSet()) {
                return maybeThumb.get();
            }
        }

        final Maybe<BufferedImage> maybeImage = imageManager.loadImage(assetId, ImageManager.SizeOption.r64x64);
        if (maybeImage.isSet()) {
            final ThumbManager.Thumb thumb = new SimpleThumb(maybeImage.get());
            cache.putThumb(assetId, thumb);
            return thumb;
        }
        return defaultThumb;
    }

    @Override
    public Thumb getDefault() {
        return defaultThumb;
    }

    @Override
    public void setDefault(BufferedImage imgDefault) {
        defaultImage = imgDefault;
        defaultThumb = new SimpleThumb(defaultImage,
                true);
    }

    @Override
    public int getWidth() {
        return oiWidth;
    }

    @Override
    public int getHeight() {
        return oiHeight;
    }
}

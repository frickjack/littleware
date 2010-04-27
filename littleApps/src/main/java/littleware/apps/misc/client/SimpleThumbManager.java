/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.misc.client;

import com.google.inject.Inject;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.RenderedImage;
import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.Date;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import littleware.apps.misc.ImageManager;
import littleware.apps.misc.ThumbManager;
import littleware.asset.AssetSearchManager;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Cache;
import littleware.base.Maybe;
import littleware.base.PropertiesLoader;
import littleware.base.SimpleCache;

/**
 * SimpleImageManager based implementation of ThumbManager
 */
public class SimpleThumbManager implements ThumbManager {

    private static final Logger log = Logger.getLogger(SimpleThumbManager.class.getName());
    private final AssetSearchManager search;

    @Override
    public void clearCache(UUID assetId) {
        ocache.remove(assetId);
    }

    protected static class SimpleThumb implements ThumbManager.Thumb {

        private final boolean isFallback;
        private final Image img;
        private Maybe<ImageIcon> maybeIcon = Maybe.empty();

        private synchronized ImageIcon renderIcon() {
            if ( maybeIcon.isSet() ) {
                return maybeIcon.get();
            }
            maybeIcon = Maybe.something( new ImageIcon( img ) );
            return maybeIcon.get();
        }

        public SimpleThumb(Image img) {
            this.img = img;
            isFallback = false;
        }

        public SimpleThumb(Image img, boolean bFallback) {
            this.img = img;
            isFallback = bFallback;
        }

        @Override
        public boolean isFallback() {
            return isFallback;
        }

        @Override
        public Image getThumb() {
            return img;
        }

        @Override
        public ImageIcon getIcon() {
            if ( maybeIcon.isSet() ) {
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
        public CacheInfo( Thumb thumb, Date birth ) {
            this.thumb = thumb;
            this.birth = birth;
        }
    }
    
    private final Cache<UUID, CacheInfo> ocache =
            new SimpleCache<UUID, CacheInfo>(900, 1000 );
    private final ImageManager imageManager;
    private final File dirCache = new File(PropertiesLoader.get().getLittleHome().getOr( new File( System.getProperty( "java.io.tmpdir" ) )), "thumbCache");
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
    private ThumbManager.Thumb defaultThumb = new SimpleThumb( defaultImage, true );

    @Inject
    public SimpleThumbManager(ImageManager mgrImage, AssetSearchManager search) {
        imageManager = mgrImage;
        this.search = search;

        try {
            final Maybe<BufferedImage> maybeDefault = imageManager.loadImage(search.getHomeAssetIds().get( "littleware.home" ), ImageManager.SizeOption.r64x64);
            if ( maybeDefault.isSet() ) {
                setDefault( maybeDefault.get() );
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed to load default thumb off littleware.home asset", ex );
        }
    }


    /** Check the in-memory and on-disk cache */
    protected Maybe<CacheInfo> cacheGet(UUID id)
            throws BaseException, GeneralSecurityException, IOException {
        Maybe<CacheInfo> maybeThumb = Maybe.emptyIfNull(ocache.get(id));
        if (maybeThumb.isSet()) {
            return maybeThumb;
        }
        return Maybe.empty();
    }

    /**
     * Allow subtypes to override a cache entry
     * @param id
     * @param maybe
     * @return
     */
    protected Maybe<CacheInfo> cachePut( UUID id, Maybe<? extends RenderedImage> maybe )
            throws BaseException,
            GeneralSecurityException, IOException {
        return cachePut( id, maybe, new Date() );
    }
    
    /**
     * Cache the given image for the given asset
     *
     * @param id
     * @param maybe may be empty if cacheing fact that no thumb is available
     * @param birth when image was created - can be used to decide if
     *                 some other image represents an update or not
     * @return
     * @throws BaseException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    private Maybe<CacheInfo> cachePut( UUID id, Maybe<? extends RenderedImage> maybe, Date birth )
            throws BaseException,
            GeneralSecurityException, IOException {
        if (maybe.isEmpty()) {
            ocache.put(id, new CacheInfo( defaultThumb, new Date() ));
            return Maybe.empty();
        }
        final RenderedImage img = maybe.get();
        final Maybe<CacheInfo> result = Maybe.something( new CacheInfo( (Thumb) new SimpleThumb( (Image) img ), birth ) );
        ocache.put(id, result.get());
        return result;
    }



    @Override
    public Thumb loadThumb(UUID assetId) throws BaseException, GeneralSecurityException, IOException {
        Maybe<CacheInfo> maybeThumb = cacheGet(assetId);
        if (maybeThumb.isEmpty()) {
            final Maybe<CacheInfo> maybeCache = cachePut(assetId, imageManager.loadImage(assetId, ImageManager.SizeOption.r64x64));
            if ( maybeCache.isSet() ) {
                return maybeCache.get().getThumb();
            }
            return defaultThumb;
        } else {
            return defaultThumb;
        }
    }

    @Override
    public Thumb getDefault() {
        return defaultThumb;
    }

    @Override
    public void setDefault(BufferedImage imgDefault) {
        defaultImage = imgDefault;
        defaultThumb = new SimpleThumb( defaultImage,
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

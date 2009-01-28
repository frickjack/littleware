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
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import littleware.apps.misc.ImageManager;
import littleware.apps.misc.ThumbManager;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Cache;
import littleware.base.Maybe;
import littleware.base.SimpleCache;

/**
 * SimpleImageManager based implementation of ThumbManager
 */
public class SimpleThumbManager implements ThumbManager {
    private static final Logger  olog = Logger.getLogger( SimpleThumbManager.class.getName() );

    private static class SimpleThumb implements ThumbManager.Thumb {
        private final boolean       obFallback;
        private final Image  oimg;
        
        public SimpleThumb( Image img ) {
            oimg = img;
            obFallback = false;
        }
        public SimpleThumb( Image img, boolean bFallback ) {
            oimg = img;
            obFallback = bFallback;
        }
        
        public boolean isFallback() {
            return obFallback;
        }

        public Image getThumb() {
            return oimg;
        }
        
    }

    private final Cache<UUID,ThumbManager.Thumb>  ocache =
            new SimpleCache<UUID,ThumbManager.Thumb>();

    private final ImageManager  omgrImage;

    private Image oimgDefault;
    {
        try {
            oimgDefault =
                ImageIO.read( SimpleThumbManager.class.getClassLoader().getResource( "littleware/apps/misc/client/defaultThumb.jpg"));
        } catch ( IOException ex ) {
            throw new AssertionFailedException( "Failed to load default image", ex );
        }
    }
    private int oiWidth = 100;
    private int oiHeight = 100;
    private ThumbManager.Thumb  othumbDefault =
            new SimpleThumb( oimgDefault.getScaledInstance(oiWidth, oiHeight, Image.SCALE_DEFAULT),
            true );

    @Inject
    public SimpleThumbManager( ImageManager mgrImage
            ) {
        omgrImage = mgrImage;
    }

    public Thumb loadThumb(UUID u_asset) throws BaseException, GeneralSecurityException, IOException {
        Thumb thumbCache = ocache.get( u_asset );
        if ( null != thumbCache ) {
            return thumbCache;
        }
        Maybe<BufferedImage> maybe = omgrImage.loadImage(u_asset);
        if ( maybe.isSet() ) {
            Thumb thumb = new SimpleThumb( (Image) maybe.getOr( null ).getScaledInstance(oiWidth, oiHeight, Image.SCALE_DEFAULT));
            ocache.put( u_asset, thumb );
            return thumb;
        }
        return othumbDefault;
    }

    public Image getDefault() {
        return oimgDefault;
    }

    public void setDefault( Image imgDefault) {
        oimgDefault = imgDefault;
    }

    public int getWidth() {
        return oiWidth;
    }

    public void setWidth(int iWidth) {
        if ( iWidth != oiWidth ) {
            oiWidth = iWidth;
            ocache.clear ();
        }
    }

    public int getHeight() {
        return oiHeight;
    }

    public void setHeight(int iHeight) {
        if ( iHeight != oiHeight ) {
            oiHeight = iHeight;
            ocache.clear ();
        }
    }

    public void clearCache(UUID u_asset) {
        ocache.remove( u_asset );
    }

}

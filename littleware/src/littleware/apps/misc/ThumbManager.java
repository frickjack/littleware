/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.misc;

import java.awt.Image;
import java.awt.image.RenderedImage;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import javax.swing.ImageIcon;
import littleware.base.BaseException;

/**
 * Thumbnail manager - just loads and caches thumbnail-rez
 * version of images attached to different assets
 * on demand.
 */
public interface ThumbManager {


    public static interface Thumb {
        /**
         * True if the getThumb thumbnail is a fallback
         * to the default, because no image is associated
         * with the asset.
         */
        public boolean isFallback ();

        public Image getThumb();

        /** ImageIcon generated on demand first time called */
        public ImageIcon getIcon();
    }

    /**
     * Load a thumb-res image for the given asset id.
     * If no image is associated with that asset, then
     * just return the default thumbnail.
     * 
     * @param u_asset
     * @return the thumbnail or default thumb
     * @throws BaseException
     * @throws GeneralSecurityException
     * @throws IOException
     */
    public Thumb loadThumb( UUID u_asset
            ) throws BaseException, GeneralSecurityException, IOException;


    public RenderedImage  getDefault ();
    public void setDefault( RenderedImage thumb );

    /** Thumbnail width property */
    public int getWidth();
    public void setWidth( int i_width );

    /** Thumbnail height property */
    public int getHeight ();
    public void setHeight( int i_height );

    /**
     * Clear data for asset id u_asset out of the cache.
     * Hopefully eventually automatically handled handled
     * by an AssetModelCache synchronization mechanism.
     *
     * @param u_asset id to clear out of cache if necessary
     */
    public void clearCache( UUID u_asset );

}

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
import java.awt.image.BufferedImage;
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
     */
    public Thumb loadThumb( UUID assetId
            ) throws BaseException, GeneralSecurityException, IOException;


    public Thumb  getDefault ();
    public void setDefault( BufferedImage thumb );

    /** Thumbnail width property */
    public int getWidth();

    /** Thumbnail height property */
    public int getHeight ();

    /**
     * Clear the given entry out of the cache - used
     * in testing and app-specific specialization
     */
    public void clearCache( UUID assetId );

}

/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.misc;

import java.awt.image.BufferedImage;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import littleware.asset.Asset;
import littleware.base.BaseException;
import littleware.base.Maybe;

/**
 * Save/load an image out of the filebucket associated with some asset.
 * Maintain a cache of images on the client.
 */
public interface ImageManager {
    public static final int    iMaxWidth = 500;
    public static final int    iMaxHeight = 500;

    /**
     * Load the image associated with the given asset
     * @param u_asset
     * @return
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     */
    public Maybe<BufferedImage>   loadImage( UUID u_asset
            ) throws BaseException, GeneralSecurityException, RemoteException, IOException;

    /**
     * Save the given image to a standard location under the
     * image filebucket, and returned the updated asset
     * (filebucket mods update the asset transaction count).
     *
     * @param a_save asset to associate image with
     * @param img to save
     * @return updated asset
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     * @throws IllegalArgumentException if img width/height
     *          exceed iMaxWidth or iMaxHeight.
     */
    public Asset   saveImage( Asset a_save, BufferedImage img, String s_update_comment
            ) throws BaseException, GeneralSecurityException, RemoteException, IOException;

    /**
     * Delete the asset associated with the given Image.
     * NOOP if image not associated.
     *
     * @param a_save asset to remove image from
     * @return asset post-update
     * @throws littleware.base.BaseException
     * @throws java.security.GeneralSecurityException
     * @throws java.rmi.RemoteException
     * @throws java.io.IOException
     */
    public Asset   deleteImage( Asset a_save, String s_update_comment
            ) throws BaseException, GeneralSecurityException, RemoteException, IOException;

}

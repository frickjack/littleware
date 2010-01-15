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
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import littleware.apps.filebucket.Bucket;
import littleware.apps.filebucket.BucketManager;
import littleware.apps.misc.ImageManager;
import littleware.asset.Asset;
import littleware.base.BaseException;
import littleware.base.Cache;
import littleware.base.Maybe;
import littleware.base.SimpleCache;

/**
 * Simple implementation of ImageManager - just calls
 * out to AssetSearchManager, BucketManager
 * under the hood.  Keeps a simple internal cache.
 */
public class SimpleImageManager implements ImageManager {
    private static final Logger olog = Logger.getLogger( SimpleImageManager.class.getName() );
    private static final String osReservedPath = "Image123.jpg";

    private final BucketManager      omgrBucket;
    private final Cache<UUID,Maybe<BufferedImage>>  ocache =
                    new SimpleCache<UUID,Maybe<BufferedImage>>();

    @Inject
    public SimpleImageManager( BucketManager mgrBucket ) {
        omgrBucket = mgrBucket;
    }

    private static final Maybe<BufferedImage> oempty = Maybe.empty();

    @Override
    public Maybe<BufferedImage> loadImage(UUID u_asset) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        final Maybe<BufferedImage> maybe_cache = ocache.get( u_asset );
        if ( null != maybe_cache ) {
            return maybe_cache;
        }
        // Check the server
        Bucket bucket = omgrBucket.getBucket(u_asset);
        if ( ! bucket.getPaths ().contains( osReservedPath ) ) {
            ocache.put( u_asset, oempty );
            return oempty;
        }
        final byte[] data = omgrBucket.readBytesFromBucket(u_asset, osReservedPath);
        final BufferedImage img = ImageIO.read( new ByteArrayInputStream( data ) );
        final Maybe<BufferedImage> maybe_result = Maybe.something( img );
        if ( (img.getWidth() < 100) && (img.getHeight() < 100) ) {
            // don't cache fat images in memory!
            //ocache.put( u_asset, maybe_result );
        }
        return maybe_result;
    }

    @Override
    public <T extends Asset> T saveImage(T a_save, BufferedImage img, String s_update_comment ) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        ImageIO.write(img, "jpg", stream);
        stream.close();
        T a_result = omgrBucket.writeToBucket(a_save, osReservedPath, stream.toByteArray(), s_update_comment);
        //ocache.put( a_save.getId(), Maybe.something( img ) );
        ocache.remove( a_save.getId() );
        return a_result;
    }

    @Override
    public <T extends Asset> T   deleteImage( T a_save, String s_update_comment
            ) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        ocache.remove( a_save.getId() );
        // Check the server
        Bucket bucket = omgrBucket.getBucket( a_save.getId () );
        if ( ! bucket.getPaths ().contains( osReservedPath ) ) {
            return a_save;
        }
        return omgrBucket.eraseFromBucket(a_save, osReservedPath, s_update_comment);
    }

    @Override
    public void clearCache( UUID u_asset ) {
        ocache.remove(u_asset);
    }
}
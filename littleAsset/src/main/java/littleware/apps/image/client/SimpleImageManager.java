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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
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
import littleware.apps.filebucket.BucketUtil;
import littleware.apps.image.ImageManager;
import littleware.asset.Asset;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.feedback.Feedback;
import littleware.base.feedback.NullFeedback;

/**
 * Simple implementation of ImageManager - just calls
 * out to AssetSearchManager, BucketManager
 * under the hood.  Keeps a simple internal cache.
 */
public class SimpleImageManager implements ImageManager {

    private static final Logger log = Logger.getLogger(SimpleImageManager.class.getName());

    private static String buildBucketPath(ImageManager.SizeOption size) {
        return "Image123" + size + ".png";
    }
    private final BucketUtil bucketUtil;
    private final ImageCache cache;
    private final BucketManager bucketMgr;
    private final Feedback      feedback = new NullFeedback();

    @Inject
    public SimpleImageManager(BucketUtil bucketUtil, BucketManager bucketMgr, 
            ImageCache cache ) {
        this.bucketUtil = bucketUtil;
        this.bucketMgr = bucketMgr;
        this.cache = cache;
    }

    /**
     * Return the given image scaled to fit within getWidth/getHeight
     *
     * @param img
     * @param widthIn width of square image output
     * @return img itself if scaling not needed or scaled image
     */
    protected static BufferedImage scaleImage(BufferedImage img, final int widthIn ) {
        if ( (widthIn >= img.getWidth()) && (widthIn >= img.getHeight()) ) {
            return img;
        }

        final int width;
        final int height;

        if ( img.getWidth() > img.getHeight() ) {
            width = widthIn;
            height = widthIn * img.getHeight() / img.getWidth();
        } else {
            height = widthIn;
            width = widthIn * img.getWidth() / img.getHeight();
        }
        if (img.getWidth() == width && img.getHeight() == height) {
            return img;
        }

        //final ImageFilter filter = new ReplicateScaleFilter( width, height );
        //final ImageProducer source = new FilteredImageSource( img.getSource(), filter );

        final BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);
        final Graphics2D graphics = (Graphics2D) result.getGraphics();
        graphics.drawImage((Image) img, 0, 0, width, height, java.awt.Color.black,
                new ImageObserver() {

                    @Override
                    public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                        graphics.dispose();
                        return false;
                    }
                });
        return result;
    }
    private static final Maybe<BufferedImage> empty = Maybe.empty();

    @Override
    public Maybe<BufferedImage> loadImage(UUID id, ImageManager.SizeOption size) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        final ImageCache.CacheEntry entry = cache.cacheGet(id, size);
        if (entry.isInCache()) {
            return entry.getImage();
        }
        // Check the server
        final Bucket bucket = bucketMgr.getBucket(id);
        final String path = buildBucketPath(size);
        if (!bucket.getPaths().contains(path)) {
            cache.cachePut(id, size, empty);
            return empty;
        }
        final byte[] data = bucketUtil.readAll(id, path, feedback );
        final BufferedImage img = ImageIO.read(new ByteArrayInputStream(data));
        final Maybe<BufferedImage> result = Maybe.something(img);
        cache.cachePut(id, size, result);
        return result;
    }

    @Override
    public <T extends Asset> T saveImage(T asset, BufferedImage img, String updateComment) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        T result = asset;
        for (ImageManager.SizeOption size : ImageManager.SizeOption.values()) {
            final BufferedImage scaledImage = scaleImage(img, size.getHeight() );
            final ByteArrayOutputStream stream = new ByteArrayOutputStream();
            ImageIO.write(scaledImage, "png", stream);
            stream.close();
            result = bucketUtil.writeAll(result, buildBucketPath(size), stream.toByteArray(),
                                            updateComment, feedback
                                            );
            cache.cachePut(asset.getId(), size, Maybe.something(scaledImage));
        }
        return result;
    }

    @Override
    public <T extends Asset> T deleteImage(T asset, String updateComment) throws BaseException, GeneralSecurityException, RemoteException, IOException {
        cache.remove(asset.getId());
        T result = asset;
        // Check the server
        final Bucket bucket = bucketMgr.getBucket(asset.getId());

        for (ImageManager.SizeOption size : ImageManager.SizeOption.values()) {
            final String path = buildBucketPath(size);

            if (bucket.getPaths().contains(path)) {
                result = bucketMgr.eraseFromBucket(result, path, updateComment);
            }
        }
        return result;
    }

    @Override
    public void clearCache(UUID id) {
        cache.remove(id);
    }
}

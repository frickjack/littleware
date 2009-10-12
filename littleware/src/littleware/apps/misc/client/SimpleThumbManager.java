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
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.awt.image.ImageObserver;
import java.awt.image.RenderedImage;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.security.GeneralSecurityException;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import littleware.apps.misc.ImageManager;
import littleware.apps.misc.ThumbManager;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.base.Cache;
import littleware.base.Maybe;
import littleware.base.PropertiesLoader;
import littleware.base.SimpleCache;
import littleware.base.UUIDFactory;
import littleware.base.Whatever;

/**
 * SimpleImageManager based implementation of ThumbManager
 */
public class SimpleThumbManager implements ThumbManager {

    private static final Logger olog = Logger.getLogger(SimpleThumbManager.class.getName());
    private final AssetSearchManager search;

    protected static class SimpleThumb implements ThumbManager.Thumb {

        private final boolean obFallback;
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
            obFallback = false;
        }

        public SimpleThumb(Image img, boolean bFallback) {
            this.img = img;
            obFallback = bFallback;
        }

        @Override
        public boolean isFallback() {
            return obFallback;
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
    private final Cache<UUID, ThumbManager.Thumb> ocache =
            new SimpleCache<UUID, ThumbManager.Thumb>(100000, 10);
    private final ImageManager omgrImage;
    private final File dirCache = new File(PropertiesLoader.get().getLittleHome().getOr( new File( System.getProperty( "java.io.tmpdir" ) )), "thumbCache");
    private RenderedImage oimgDefault;

    {
        try {
            oimgDefault =
                    ImageIO.read(SimpleThumbManager.class.getClassLoader().getResource("littleware/apps/misc/client/defaultThumb.jpg"));
        } catch (IOException ex) {
            throw new AssertionFailedException("Failed to load default image", ex);
        }
    }
    private int oiWidth = 80;
    private int oiHeight = 60;
    private ThumbManager.Thumb othumbDefault = new SimpleThumb( scaleImage(oimgDefault, oiWidth, oiHeight), true );

    @Inject
    public SimpleThumbManager(ImageManager mgrImage, AssetSearchManager search) {
        omgrImage = mgrImage;
        this.search = search;
    }

    /** Check the in-memory and on-disk cache */
    protected Maybe<Thumb> cacheGet(UUID id)
            throws BaseException, GeneralSecurityException, IOException {
        Maybe<Thumb> maybeThumb = Maybe.emptyIfNull(ocache.get(id));
        if (maybeThumb.isSet()) {
            return maybeThumb;
        }
        // check local disk cache first
        Maybe<RenderedImage> maybe = Maybe.empty();
        final File infoFile = new File(dirCache, UUIDFactory.makeCleanString(id) + ".info");
        final File jpgFile = new File(dirCache, UUIDFactory.makeCleanString(id) + ".jpg");
        boolean bLocal = false;
        if (infoFile.canRead() && jpgFile.canRead()) {
            final BufferedReader reader = new BufferedReader(new FileReader(infoFile));
            long transaction = -1;
            try {
                transaction = Long.parseLong(reader.readLine().trim());
            } catch (Exception ex) {
                olog.log(Level.WARNING, "Failed to parse " + infoFile);
            } finally {
                reader.close();
            }
            if (transaction >= 0) {
                // check timeout
                final Maybe<Asset> maybeAsset = search.getAsset(id);
                if (maybeAsset.isSet() && (maybeAsset.get().getTransactionCount() <= transaction)) {
                    maybe = Maybe.emptyIfNull( (RenderedImage) ImageIO.read(jpgFile));
                }
            }
        }
        return cachePut(id, maybe);
    }

    protected Maybe<Thumb> cachePut(UUID id, Maybe<? extends RenderedImage> maybe)
            throws BaseException,
            GeneralSecurityException, IOException {
        if (maybe.isEmpty()) {
            ocache.put(id, othumbDefault);
            return Maybe.empty();
        }
        final RenderedImage img = maybe.get();
        final Maybe<Thumb> result;
        if ((img.getWidth() > getWidth()) || (img.getHeight() > getHeight())) {
            final Image scale = scaleImage(img, getWidth(), getHeight() );
            result = Maybe.something((Thumb) new SimpleThumb(scale));
        } else {
            result = Maybe.something((Thumb) new SimpleThumb( (Image) img));
        }
        ocache.put(id, result.get());
        // Save to file
        if (!dirCache.exists()) {
            dirCache.mkdirs();
        }
        { // write out file
            final Image imgWrite = result.get().getThumb();
            final File infoFile = new File(dirCache, UUIDFactory.makeCleanString(id) + ".info");
            final File jpgFile = new File(dirCache, UUIDFactory.makeCleanString(id) + ".jpg");
            final Maybe<Asset> maybeAsset = search.getAsset(id);
            final Writer writer = new FileWriter(infoFile);
            try {
                if (maybeAsset.isSet()) {
                    writer.write("" + maybeAsset.get().getTransactionCount() + Whatever.NEWLINE);
                } else {
                    writer.write("0" + Whatever.NEWLINE);
                }
            } finally {
                writer.close();
            }
            ImageIO.write( (RenderedImage) result.get().getThumb(), "jpg", jpgFile);
        }
        return result;
    }


    /**
     * Return the given image scaled to fit within getWidth/getHeight
     * @param img
     * @param widthIn may be -1 to preserve aspect ratio relative to height
     * @param heightIn may be -1 to preserve aspect ratio relative to width
     * @return img itself if scaling not needed or scaled image
     */
    protected static Image scaleImage(RenderedImage img, int widthIn, int heightIn ) {
        if ( widthIn <= 0 && heightIn <= 0 ) {
            throw new IllegalArgumentException( "width and height both negative" );
        }

        final int width;
        final int height;

        if ( widthIn > 0 ) {
            width = widthIn;
        } else {
            width = (int) (img.getWidth() * (heightIn / (double) img.getHeight()));
        }
        if ( heightIn > 0 ) {
            height = heightIn;
        } else {
            height = (int) (img.getHeight() * (widthIn / (double) img.getWidth()));
        }

        if ( img.getWidth() == width && img.getHeight() == height ) {
            return (Image) img;
        }

        //final ImageFilter filter = new ReplicateScaleFilter( width, height );
        //final ImageProducer source = new FilteredImageSource( img.getSource(), filter );

        final BufferedImage result = new BufferedImage( width, height, BufferedImage.TYPE_INT_RGB );
        final Graphics2D graphics = (Graphics2D) result.getGraphics();
        graphics.drawImage( (Image) img,0,0,width,height,java.awt.Color.black,
                new ImageObserver() {
            @Override
            public boolean imageUpdate(Image img, int infoflags, int x, int y, int width, int height) {
                graphics.dispose();
                return false;
            }
        });
        return result;
    }

    @Override
    public Thumb loadThumb(UUID u_asset) throws BaseException, GeneralSecurityException, IOException {
        Maybe<Thumb> maybeThumb = cacheGet(u_asset);
        if (maybeThumb.isEmpty()) {
            return cachePut(u_asset, omgrImage.loadImage(u_asset)).getOr(othumbDefault);
        } else {
            return othumbDefault;
        }
    }

    @Override
    public Thumb getDefault() {
        return othumbDefault;
    }

    @Override
    public void setDefault(RenderedImage imgDefault) {
        oimgDefault = imgDefault;
        othumbDefault = new SimpleThumb( scaleImage( oimgDefault,oiWidth, oiHeight),
                true);
    }

    @Override
    public int getWidth() {
        return oiWidth;
    }

    @Override
    public void setWidth(int iWidth) {
        if (iWidth != oiWidth) {
            oiWidth = iWidth;
            ocache.clear();
            othumbDefault = new SimpleThumb( scaleImage( oimgDefault,oiWidth, oiHeight),
                    true);
        }
    }

    @Override
    public int getHeight() {
        return oiHeight;
    }

    @Override
    public void setHeight(int iHeight) {
        if (iHeight != oiHeight) {
            oiHeight = iHeight;
            ocache.clear();
            othumbDefault = new SimpleThumb(scaleImage(oimgDefault,oiWidth, oiHeight),
                    true);

        }
    }

    @Override
    public void clearCache(UUID u_asset) {
        ocache.remove(u_asset);
    }
}

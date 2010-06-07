/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import littleware.apps.image.ImageManager;
import littleware.asset.Asset;
import littleware.base.Maybe;
import littleware.security.auth.LittleSession;
import littleware.test.LittleTest;

/**
 * TestCase for ImageManager implementations.
 * Just tries to save and load an image under the
 * active Session for the given SessionHelper.
 */
public class ImageManagerTester extends LittleTest {
    private static final Logger olog = Logger.getLogger( ImageManagerTester.class.getName() );

    private final LittleSession osession;
    private final ImageManager  omgrImage;

    @Override
    public void setUp () {
    }

    @Override
    public void tearDown() {
    }

    /**
     * Inject dependencies, and register testImageBase
     * test method
     * 
     * @param helper
     * @param mgrImage
     */
    @Inject
    public ImageManagerTester( LittleSession session,
            ImageManager mgrImage
            )
    {
        super.setName( "testBasicImage" );
        osession = session;
        omgrImage = mgrImage;
    }

    public void testBasicImage () {
        try {
            BufferedImage img = ImageIO.read(ImageManagerTester.class.getClassLoader().getResource("littleware/apps/misc/test/testImage.png"));
            Asset         a_test = osession.copy().transaction(-1).build();

            final Maybe<BufferedImage> maybe_load1 = omgrImage.loadImage( a_test.getId (), ImageManager.SizeOption.r128x128 );
            a_test = omgrImage.saveImage(a_test, img, "saving new reference image" );
            assertTrue( "Able to load image after save",
                    omgrImage.loadImage( a_test.getId(), ImageManager.SizeOption.r128x128 ).isSet()
                    );
            omgrImage.deleteImage(a_test, "cleaning up test image" );
        } catch (Exception ex) {
            olog.log(Level.SEVERE, "Failed test", ex);
            assertTrue( "Exception on test", false );
        }
    }


    /*...
     * Too much trouble to mock out a BucketManager for now - ugh ...
     *
     * Return an ImageManagerTester configured to test
     * a SimpleImageManagerTester instance injected with
     * Mock object BucketManager and AssetSearchManager
     * dependencies.
     *
     *
    public ImageManagerTester buildMockTest () {
        final BufferedImage img = new BufferedImage( 100, 100, BufferedImage.TYPE_INT_ARGB);
        final LittleSession session = SecurityAssetType.SESSION.create();
        BucketManager  mockBucket = createMock( BucketManager.class );

        return null;
    }
     */
}

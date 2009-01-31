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

package littleware.apps.misc.test;

import com.google.inject.Inject;
import java.awt.image.BufferedImage;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import junit.framework.TestCase;
import littleware.apps.misc.ImageManager;
import littleware.asset.Asset;
import littleware.base.Maybe;
import littleware.security.auth.SessionHelper;

/**
 * TestCase for ImageManager implementations.
 * Just tries to save and load an image under the
 * active Session for the given SessionHelper.
 */
public class ImageManagerTester extends TestCase {
    private static final Logger olog = Logger.getLogger( ImageManagerTester.class.getName() );

    private final SessionHelper ohelper;
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
    public ImageManagerTester( SessionHelper helper,
            ImageManager mgrImage
            )
    {
        super( "testBasicImage" );
        ohelper = helper;
        omgrImage = mgrImage;
    }

    public void testBasicImage () {
        try {
            BufferedImage img = ImageIO.read(ImageManagerTester.class.getClassLoader().getResource("littleware/apps/misc/test/testImage.jpg"));
            Asset         a_test = ohelper.getSession();

            // First try to load an image under a_test
            final Maybe<BufferedImage> maybe_load1 = omgrImage.loadImage( a_test.getObjectId () );
            a_test = omgrImage.saveImage(a_test, img, "saving new reference image" );
            assertTrue( "Able to load image after save",
                    omgrImage.loadImage( a_test.getObjectId() ).isSet()
                    );
            omgrImage.deleteImage(a_test, "cleaning up test image" );
        } catch (Exception ex) {
            olog.log(Level.SEVERE, "Failed test", ex);
            assertTrue( "Exception on test", false );
        }
    }
}

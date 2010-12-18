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
import littleware.apps.image.ThumbManager;
import littleware.base.AssertionFailedException;
import static littleware.apps.image.ThumbManager.Thumb;
import littleware.security.auth.LittleSession;
import littleware.test.LittleTest;

/**
 * Test ThumbManager implementation
 */
public class ThumbManagerTester extends LittleTest {
    private static final Logger olog = Logger.getLogger( ThumbManagerTester.class.getName () );

    private LittleSession session;
    private final ThumbManager  thumbMgr;
    private final ImageManager  imageMgr;

    /** Erase any image under the injected session asset */
    @Override
    public void setUp() {
        try {
            session = imageMgr.deleteImage(session.copy().transaction(-1).build(),
                    "Setting up thumb test"
                    ).narrow();
            thumbMgr.clearCache(session.getId() );
        } catch ( Exception ex ) {
            throw new AssertionFailedException( "Failed test setup", ex );
        }
    }

    /**
     * Inject dependencies
     * 
     * @param session to setup a thumb under
     * @param mgrThumb to test
     * @param mgrImage test setup an image that thumb should key off of
     */
    @Inject
    public ThumbManagerTester( LittleSession session,
            ThumbManager mgrThumb, ImageManager mgrImage ) {
        this.session = session;
        this.thumbMgr = mgrThumb;
        this.imageMgr = mgrImage;
        super.setName( "testBasicThumb" );
    }

    public void testBasicThumb () {
        try {
            final Thumb thumb_default = thumbMgr.loadThumb( session.getId () );
            assertTrue ( "Loaded defualt thumb", thumb_default.isFallback () );

            /*... do not popup viewer - busts HudsonTestSuite ...
            assertTrue( "Default thumb ok",
                    JLittleDialog.showTestDialog(
                        new JLabel( new ImageIcon( thumb_default.getThumb() ) ),
                        "Verify default thumbnail"
                                )
                    );
             */
            // ok - frick things up a bit
            final BufferedImage imgTest = ImageIO.read(ThumbManagerTester.class.getClassLoader().getResource("littleware/apps/image/test/testImage.png") );
            imageMgr.saveImage(session,
                    imgTest,
                    "Setting up thumb test image"
                    );
            thumbMgr.clearCache( session.getId() );
            assertTrue( "Thumb no longer default", ! thumbMgr.loadThumb( session.getId() ).isFallback() );
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Test failed on exception", ex );
            assertTrue( "Cuaght unexpected: " + ex, false );
        }
    }
}

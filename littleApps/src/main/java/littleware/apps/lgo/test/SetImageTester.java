/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo.test;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.lgo.SetImageCommand;
import littleware.apps.misc.ImageManager;
import littleware.base.feedback.LoggerFeedback;
import littleware.test.LittleTest;

/**
 * Test SetImageCommand by associating an image with littleware.test_home
 */
public class SetImageTester extends LittleTest {
    private static final Logger olog = Logger.getLogger( SetImageTester.class.getName() );
    private final SetImageCommand ocomTest;
    private final ImageManager omgrImage;

    @Inject
    public SetImageTester(
            ImageManager mgrImage,
            SetImageCommand comTest
            ) {
        setName( "testSetImage" );
        omgrImage = mgrImage;
        ocomTest = comTest;
    }

    public void testSetImage() {
        try {
            ocomTest.processArgs( Arrays.asList(
                    "-path", getTestHome(),
                    "-image", "../littleware/marilyn.jpg"
                    ));
            ocomTest.runSafe( new LoggerFeedback(), "" );
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught unexpected: " + ex, false );
        }
    }
}

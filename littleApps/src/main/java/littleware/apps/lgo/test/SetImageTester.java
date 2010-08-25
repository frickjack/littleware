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
import littleware.apps.image.ImageManager;
import littleware.base.feedback.LoggerFeedback;
import littleware.test.LittleTest;

/**
 * Test SetImageCommand by associating an image with littleware.test_home
 */
public class SetImageTester extends LittleTest {
    private static final Logger log = Logger.getLogger( SetImageTester.class.getName() );
    private final SetImageCommand.Builder builder;
    private final ImageManager imageMgr;

    @Inject
    public SetImageTester(
            ImageManager mgrImage,
            SetImageCommand.Builder commandBuilder
            ) {
        setName( "testSetImage" );
        imageMgr = mgrImage;
        builder = commandBuilder;
    }

    public void testSetImage() {
        try {
            builder.buildFromArgs( Arrays.asList(
                    "-path", getTestHome(),
                    "-image", "../littleware/marilyn.jpg"
                    )
            ).runCommand( new LoggerFeedback() );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught unexpected: " + ex, false );
        }
    }
}

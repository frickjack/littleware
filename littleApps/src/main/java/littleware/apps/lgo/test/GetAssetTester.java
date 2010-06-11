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
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.lgo.GetAssetCommand;
import littleware.lgo.LgoException;
import littleware.base.feedback.LoggerFeedback;
import littleware.test.LittleTest;

/**
 * Test the GetAssetCommand
 */
public class GetAssetTester extends LittleTest {
    private static final Logger  olog = Logger.getLogger( GetAssetTester.class.getName() );

    private final GetAssetCommand ocomTest;


    @Inject
    public GetAssetTester( GetAssetCommand comTest ) {
        setName( "testGetAsset" );
        ocomTest = comTest;
    }

    public void testGetAsset() {
        try {
            String sResult = ocomTest.runCommandLine( new LoggerFeedback(),
                    "/" + getTestHome()
                    );
            olog.log( Level.INFO, "Test home: " + sResult );
            assertTrue( "Asset info includes asset name", sResult.contains( getTestHome() ) );
        } catch ( LgoException ex ) {
            olog.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }

}

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
import littleware.apps.lgo.GetByNameCommand;
import littleware.lgo.LgoException;
import littleware.asset.Asset;
import littleware.base.feedback.LoggerFeedback;
import littleware.security.SecurityAssetType;
import littleware.test.LittleTest;

/**
 * Test GetByNameCommand
 */
public class GetByNameTester extends LittleTest {
    private static final Logger   olog = Logger.getLogger( GetByNameTester.class.getName() );
    private final GetByNameCommand    ocomTest;


    @Inject
    public GetByNameTester( GetByNameCommand comTest ) {
        setName( "testByName" );
        ocomTest = comTest;
    }

    public void testByName() {
        try {
            ocomTest.processArgs( Arrays.asList( "-name", "littleware.test_user",
                    "-type", "user"
                    ) );
            final Asset aUser = ocomTest.runSafe( new LoggerFeedback(), "bla" );
            assertTrue( "Got expected asset: " + aUser.getName(),
                    aUser.getAssetType().equals( SecurityAssetType.USER )
                    && aUser.getName().equals( "littleware.test_user" )
                    );
        } catch ( LgoException ex ) {
            olog.log( Level.WARNING, "Test failed", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }
}

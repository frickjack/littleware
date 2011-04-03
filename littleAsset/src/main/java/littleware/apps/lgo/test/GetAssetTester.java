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
import littleware.apps.lgo.GetAssetCommand;
import littleware.asset.test.AbstractAssetTest;
import littleware.base.feedback.LoggerFeedback;

/**
 * Test the GetAssetCommand
 */
public class GetAssetTester extends AbstractAssetTest {
    private static final Logger  log = Logger.getLogger( GetAssetTester.class.getName() );

    private final GetAssetCommand.Builder commandBuilder;


    @Inject
    public GetAssetTester( GetAssetCommand.Builder commandBuilder ) {
        setName( "testGetAsset" );
        this.commandBuilder = commandBuilder;
    }

    public void testGetAsset() {
        try {
            final String sResult = commandBuilder.buildFromArgs(
                    Arrays.asList( "-path", "/" + getTestHome() )
                    ).runCommandLine( new LoggerFeedback()
                    );
            log.log( Level.INFO, "Test home: {0}", sResult);
            assertTrue( "Asset info includes asset name", sResult.contains( getTestHome() ) );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }

}

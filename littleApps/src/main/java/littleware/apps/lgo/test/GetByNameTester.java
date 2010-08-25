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
import littleware.asset.Asset;
import littleware.base.feedback.LoggerFeedback;
import littleware.security.SecurityAssetType;
import littleware.test.LittleTest;

/**
 * Test GetByNameCommand
 */
public class GetByNameTester extends LittleTest {
    private static final Logger   log = Logger.getLogger( GetByNameTester.class.getName() );
    private final GetByNameCommand.Builder    commandBuilder;


    @Inject
    public GetByNameTester( GetByNameCommand.Builder commandBuilder ) {
        setName( "testByName" );
        this.commandBuilder = commandBuilder;
    }

    public void testByName() {
        try {
            final Asset aUser = commandBuilder.buildFromArgs(
                    Arrays.asList( "-name", "littleware.test_user",
                    "-type", "user"
                    )
                    ).runCommand( new LoggerFeedback() );
            assertTrue( "Got expected asset: " + aUser.getName(),
                    aUser.getAssetType().equals( SecurityAssetType.USER )
                    && aUser.getName().equals( "littleware.test_user" )
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Test failed", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }
}

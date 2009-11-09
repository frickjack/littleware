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
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.lgo.GetRootPathCommand;
import littleware.asset.AssetPath;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.test.LittleTest;

/**
 * Test the GetRootPathCommand
 */
public class RootPathCommandTest extends LittleTest {
    private static final Logger  log = Logger.getLogger( RootPathCommandTest.class.getName() );
    private final GetRootPathCommand command;
    private final AssetSearchManager search;
    private final AssetPathFactory pathFactory;

    @Inject
    public RootPathCommandTest( GetRootPathCommand command,
            AssetPathFactory pathFactory,
            AssetSearchManager search
            ) {
        this.command = command;
        setName( "testRootPath" );
        this.search = search;
        this.pathFactory = pathFactory;
    }

    public void testRootPath() {
        try {
            final AssetPath path = pathFactory.createPath( getTestHome( search ).getObjectId() );
            final AssetPath pathResult = command.runSafe( new LoggerUiFeedback( log ), path );
            log.log( Level.INFO, "Rooted path: " + path + " -- " + pathResult );
            assertTrue( "Path references littleware.test_home: " + pathResult,
                    pathResult.toString().indexOf( getTestHome() ) >= 0
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Caught " + ex );
        }
    }
}

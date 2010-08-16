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
import littleware.apps.lgo.CreateFolderCommand;
import littleware.lgo.LgoException;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.base.Maybe;
import littleware.base.feedback.LoggerFeedback;
import littleware.test.LittleTest;

/**
 * Test the CreateFolderCommand
 */
public class CreateFolderTester extends LittleTest {
    private static final Logger olog = Logger.getLogger( CreateFolderTester.class.getName() );

    private final AssetSearchManager   osearch;
    private final AssetManager         omgrAsset;
    private final CreateFolderCommand  ocomTest;

    /**
     * Delete the test folder if it already exists
     */
    @Override
    public void setUp() {
        try {
            final Asset aHome = getTestHome( osearch );
            final Maybe<Asset> maybeDelete = osearch.getAssetFrom( aHome.getId(),
                    "testCreateFolder"
                    );
            if ( maybeDelete.isSet() ) {
                olog.log( Level.INFO, "Cleaning up previous test" );
                omgrAsset.deleteAsset( maybeDelete.get().getId(), "test cleanup" );
            }
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Failed test setup", ex );
            assertTrue( "Failed setup: " + ex, false );
        }
    }

    @Override
    public void tearDown() {
        setUp();
    }

    @Inject
    public CreateFolderTester(
            AssetSearchManager search,
            AssetManager  mgrAsset,
            CreateFolderCommand comTest
            ) {
        setName( "testCreate" );
        osearch = search;
        omgrAsset = mgrAsset;
        ocomTest = comTest;
    }

    public void testCreate() {
        try {
            olog.log( Level.INFO, "Creating test folder ..." );
            final Asset aNew = ocomTest.runCommand( new LoggerFeedback()
                    //"/" + getTestHome() + "/testCreateFolder"
                    );
            assertTrue( "Created asset", null != aNew );
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }
}

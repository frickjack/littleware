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
import littleware.apps.lgo.CreateFolderCommand;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.test.AbstractAssetTest;
import littleware.base.Maybe;
import littleware.base.feedback.LoggerFeedback;

/**
 * Test the CreateFolderCommand
 */
public class CreateFolderTester extends AbstractAssetTest {
    private static final Logger log = Logger.getLogger( CreateFolderTester.class.getName() );

    private final AssetSearchManager   search;
    private final AssetManager         assetMgr;
    private final CreateFolderCommand.Builder  commandBuilder;

    /**
     * Delete the test folder if it already exists
     */
    @Override
    public void setUp() {
        try {
            final Asset aHome = getTestHome( search );
            final Maybe<Asset> maybeDelete = search.getAssetFrom( aHome.getId(),
                    "testCreateFolder"
                    );
            if ( maybeDelete.isSet() ) {
                log.log( Level.INFO, "Cleaning up previous test" );
                assetMgr.deleteAsset( maybeDelete.get().getId(), "test cleanup" );
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test setup", ex );
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
            CreateFolderCommand.Builder commandBuilder
            ) {
        setName( "testCreate" );
        this.search = search;
        this.assetMgr = mgrAsset;
        this.commandBuilder = commandBuilder;
    }

    public void testCreate() {
        try {
            log.log( Level.INFO, "Creating test folder ..." );
            final Asset aNew = commandBuilder.buildFromArgs(
                    Arrays.asList( "-path", "/" + getTestHome() + "/testCreateFolder" )
                    ).runCommand( new LoggerFeedback()
                    );
            assertTrue( "Created asset", null != aNew );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }
}

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
import littleware.apps.lgo.DeleteAssetCommand;
import littleware.apps.lgo.LgoException;
import littleware.asset.Asset;
import littleware.asset.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.base.AssertionFailedException;
import littleware.test.LittleTest;

/**
 * Test the DeleteAssetCommand
 */
public class DeleteAssetTester extends LittleTest {
    private static final Logger  olog = Logger.getLogger( DeleteAssetTester.class.getName() );
    private final static String               osHome = "littleware.test_home";
    private final static String               osName = "deleteCommandTest";
    private final static String               osTestPath = "/" + osHome + "/" + osName;

    private final AssetSearchManager   osearch;
    private final AssetManager         omgrAsset;
    private final DeleteAssetCommand   ocommand;
    private final AssetPathFactory     ofactory;

    private Asset   oaDelete = null;
    
    /**
     * Create the "deleteCommandTest" asset
     */
    @Override
    public void setUp() {
        try {
            Asset aHome = osearch.getByName( osHome, AssetType.HOME );
            oaDelete = osearch.getAssetFromOrNull( aHome.getObjectId(), osName );
            if ( null == oaDelete ) {
                Asset aNew = AssetType.createSubfolder( AssetType.GENERIC, osName, aHome);
                oaDelete = omgrAsset.saveAsset( aNew, "Setup test asset" );
            }
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Failed test setup", ex );
            throw new AssertionFailedException( "Failed to setup test asset", ex );
        }
    }

    @Inject
    public DeleteAssetTester(
            AssetSearchManager search,
            AssetManager       mgrAsset,
            DeleteAssetCommand command,
            AssetPathFactory   factory
            ) {
        setName( "testCommand" );
        osearch = search;
        omgrAsset = mgrAsset;
        ocommand = command;
        ofactory = factory;
    }

    public void testCommand() {
        try {
            assertTrue( "Delete asset ran ok",
                    null != ocommand.runSafe( new LoggerUiFeedback(), osTestPath )
                );
        } catch ( LgoException ex ) {
            olog.log ( Level.WARNING, "Command failed", ex );
            assertTrue( "Command threw exception: " + ex, false );
        }
    }
}

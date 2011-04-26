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
import com.google.inject.Provider;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.lgo.DeleteAssetCommand;
import littleware.asset.Asset;
import littleware.asset.client.AssetManager;
import littleware.asset.AssetPathFactory;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.GenericAsset;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.asset.test.AbstractAssetTest;
import littleware.base.AssertionFailedException;
import littleware.base.feedback.LoggerFeedback;

/**
 * Test the DeleteAssetCommand
 */
public class DeleteAssetTester extends AbstractAssetTest {
    private static final Logger  log = Logger.getLogger( DeleteAssetTester.class.getName() );
    private final String               osName = "deleteCommandTest";
    private final String               sTestPath = "/" + getTestHome() + "/" + osName;

    private final AssetSearchManager   search;
    private final AssetManager         assetMgr;
    private final DeleteAssetCommand.Builder   commandBuilder;
    private final AssetPathFactory     pathFactory;

    private Asset   deleteMeAsset = null;
    private final Provider<TreeNodeBuilder> nodeProvider;
    
    /**
     * Create the "deleteCommandTest" asset
     */
    @Override
    public void setUp() {
        try {
            final LittleHome home = search.getByName( getTestHome(), LittleHome.HOME_TYPE ).get().narrow();
            deleteMeAsset = search.getAssetFrom( home.getId(), osName ).getOr( null );
            if ( null == deleteMeAsset ) {
                final Asset aNew = nodeProvider.get().parent( home ).name( osName ).build();
                deleteMeAsset = assetMgr.saveAsset( aNew, "Setup test asset" );
            }
        } catch ( RuntimeException ex ) {
            throw ex;
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test setup", ex );
            throw new AssertionFailedException( "Failed to setup test asset", ex );
        }
    }

    @Inject
    public DeleteAssetTester(
            AssetSearchManager search,
            AssetManager       mgrAsset,
            DeleteAssetCommand.Builder commandBuilder,
            AssetPathFactory   factory,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider
            ) {
        setName( "testCommand" );
        this.search = search;
        this.assetMgr = mgrAsset;
        this.commandBuilder = commandBuilder;
        this.pathFactory = factory;
        this.nodeProvider = nodeProvider;
    }

    public void testCommand() {
        try {
            assertTrue( "Delete asset ran ok",
                    null != commandBuilder.buildFromArgs(
                    Arrays.asList( "-path", sTestPath )
                    ).runCommand( new LoggerFeedback() )
                );
        } catch ( Exception ex ) {
            log.log ( Level.WARNING, "Command failed", ex );
            assertTrue( "Command threw exception: " + ex, false );
        }
    }
}

/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.test;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.client.AssetModel;
import littleware.apps.client.AssetModelLibrary;
import littleware.apps.swingclient.JAssetFamilyView;
import littleware.asset.AssetSearchManager;
import littleware.security.AccountManager;
import littleware.security.SecurityAssetType;
import littleware.test.JLittleDialog;
import littleware.test.LittleTest;

/**
 * Test the swingclient.JAssetFamilyView
 */
public class JAssetFamilyTester extends LittleTest {
    private static final Logger olog = Logger.getLogger( JAssetFamilyTester.class.getName() );

    private final JAssetFamilyView ojViewTest;
    private final AssetSearchManager osearch;
    private final AssetModelLibrary olibAsset;

    @Inject
    public JAssetFamilyTester( JAssetFamilyView  jViewTest,
            AssetSearchManager search,
            AssetModelLibrary libAsset
            ) {
        setName( "testJAssetFamily" );
        ojViewTest = jViewTest;
        osearch = search;
        olibAsset = libAsset;
    }

    public void testJAssetFamily() {
        try {
            AssetModel modelEverybody = olibAsset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP, SecurityAssetType.GROUP, osearch );
            ojViewTest.setAssetModel( modelEverybody );
            assertTrue("User confirmed family-viewer UI functional",
                    JLittleDialog.showTestDialog(ojViewTest,
                    "play with the widget. \n" +
                    "Hit OK when test successfully done")
                    );

        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Failed test: " + ex, false );
        }
    }
}

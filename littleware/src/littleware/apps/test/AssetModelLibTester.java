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
import littleware.apps.client.AbstractAssetEditor;
import littleware.apps.client.AssetEditor;
import littleware.apps.client.AssetModel;
import littleware.apps.client.AssetModelLibrary;
import littleware.apps.client.LittleEvent;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.client.LittleService;
import littleware.security.AccountManager;
import littleware.security.SecurityAssetType;
import littleware.security.auth.LittleSession;
import littleware.test.LittleTest;

/**
 * Test the AssetModelLibrary, event propogation on
 * asset update, and hookup with
 * the ServiceProviderListener.
 */
public class AssetModelLibTester extends LittleTest {
    private static final Logger       olog = Logger.getLogger( AssetModelLibTester.class.getName() );

    private final AssetModelLibrary   olibAsset;
    private final LittleSession       osession;
    private final AssetSearchManager  osearch;

    @Inject
    public AssetModelLibTester( AssetModelLibrary libAsset,
            LittleSession session,
            AssetSearchManager search
            ) {
        olibAsset = libAsset;
        osearch = search;
        osession = session;
        setName( "testModelLibrary");
    }

    /**
     * Run the injected AssetModelLibrary through a few simple tests
     */
    public void testModelLibrary() {
        // couple bogus test assets - donot save to repository
        final Asset a_bogus1 = AssetType.GENERIC.create();
        final Asset a_bogus2 = AssetType.GENERIC.create();


        try {
            final Asset a_test = osession;

            olibAsset.remove(a_test.getObjectId());

            assertTrue("Simple sync is ok",
                    olibAsset.syncAsset(a_test).getAsset() == a_test);
            assertTrue("No retrieval if not necessary",
                    olibAsset.retrieveAssetModel(a_test.getObjectId(), osearch).getAsset() == a_test
                    );

            final AssetModel amodel_everybody =
                    olibAsset.syncAsset( osearch.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP )
                    );
            assertTrue( "ModelLibrary getByName inheritance aware 1",
                    olibAsset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.PRINCIPAL
                    ) != null
                    );
            assertTrue( "ModelLibrary getByName inheritance aware 2",
                    olibAsset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.USER
                    ) == null
                    );
            assertTrue( "ModelLibrary getByName inheritance aware 3",
                    olibAsset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP
                    ) != null
                    );
            olibAsset.remove( amodel_everybody.getAsset ().getObjectId () );
            assertTrue( "ModelLibrary getByName cleared after remove",
                    olibAsset.getByName( AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP
                    ) == null
                    );

            final AssetEditor edit_bogus = new AbstractAssetEditor(this) {

                @Override
                public void eventFromModel(LittleEvent event_property) {
                    // just do something - anything
                    olog.log( Level.INFO, "Test editor received event from model, setting value to 5" );
                    a_bogus2.setValue(5);
                }
            };
            edit_bogus.setAssetModel(olibAsset.syncAsset(a_bogus1)); //addPropertyChangeListener ( listen_assetprop );
            a_bogus2.setFromId(a_bogus1.getObjectId());
            // Adding a_bogus2 to the asset repository should trigger a Property.assetsLinkingFrom
            // property-change event on listeners to a_bogus1 AssetModel
            olibAsset.syncAsset(a_bogus2);
            Thread.sleep(4000); // let any asynchrony work itself out
            assertTrue("AssetModel cascading properties correctly", 5 == a_bogus2.getValue());

        } catch (Exception e) {
            olog.log(Level.WARNING, "Caught unexpected: " + e, e );
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            olibAsset.remove(a_bogus1.getObjectId());
            olibAsset.remove(a_bogus2.getObjectId());
        }
    }

    /**
     * Test whether a loaded asset is automatically added
     * to the model library.  Just a very simple test.
     */
    public void testSessionHookup() {
        try {
            assertTrue( "SearchManager is a service", osearch instanceof LittleService );
            olibAsset.remove( osession.getObjectId() );
            assertTrue( "Session removed from model library",
                    null == olibAsset.get( osession.getObjectId() )
                    );
            osearch.getAsset( osession.getObjectId() );
            assertTrue( "Asset automatically added to model library on load",
                    null != olibAsset.get( osession.getObjectId() )
                    );
        } catch ( Exception ex ) {
            olog.log( Level.WARNING, "Test failed" , ex );
            fail( "Caught exception: " + ex );
        }
    }
    
}

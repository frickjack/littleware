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
import littleware.base.feedback.LittleEvent;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.asset.client.LittleService;
import littleware.asset.client.SimpleLittleService;
import littleware.asset.test.AbstractAssetTest;
import littleware.security.AccountManager;
import littleware.security.SecurityAssetType;
import littleware.security.auth.LittleSession;

/**
 * Test the AssetModelLibrary, event propogation on
 * asset update, and hookup with
 * the ServiceProviderListener.
 */
public class AssetModelLibTester extends AbstractAssetTest {

    private static final Logger olog = Logger.getLogger(AssetModelLibTester.class.getName());
    private final AssetModelLibrary olibAsset;
    private final LittleSession osession;
    private final AssetSearchManager osearch;

    @Inject
    public AssetModelLibTester(AssetModelLibrary libAsset,
            LittleSession session,
            AssetSearchManager search) {
        olibAsset = libAsset;
        osearch = search;
        osession = session;
        setName("testModelLibrary");
    }

    /**
     * Run the injected AssetModelLibrary through a few simple tests
     */
    public void testModelLibrary() {
        Asset a_bogus1 = null;
        Asset a_bogus2 = null;
        try {
            // couple bogus test assets - donot save to repository
            final Asset home = getTestHome(osearch);
            a_bogus1 = GenericAsset.GENERIC.create().name("bogus1").parent(home).build();
            a_bogus2 = GenericAsset.GENERIC.create().name("bogus2").parent(home).build();
            final Asset a_test = osession;

            olibAsset.remove(a_test.getId());

            assertTrue("Simple sync is ok",
                    olibAsset.syncAsset(a_test).getAsset() == a_test);
            assertTrue("No retrieval if not necessary",
                    olibAsset.retrieveAssetModel(a_test.getId(), osearch).get().getAsset() == a_test);

            final AssetModel amodel_everybody =
                    olibAsset.syncAsset(osearch.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP).get());
            assertTrue("ModelLibrary getByName inheritance aware 1",
                    olibAsset.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.PRINCIPAL).isSet());
            assertTrue("ModelLibrary getByName inheritance aware 2",
                    !olibAsset.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.USER).isSet());
            assertTrue("ModelLibrary getByName inheritance aware 3",
                    olibAsset.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP).isSet());
            olibAsset.remove(amodel_everybody.getAsset().getId());
            assertTrue("ModelLibrary getByName cleared after remove",
                    !olibAsset.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP).isSet());

            final AssetEditor edit_bogus = new AbstractAssetEditor(this) {

                @Override
                public void eventFromModel(LittleEvent event_property) {
                    // just do something - anything
                    olog.log(Level.INFO, "Test editor received event from model, setting value to 5");
                    changeLocalAsset().setValue(5);
                }
            };
            edit_bogus.setAssetModel(olibAsset.syncAsset(a_bogus1)); //addPropertyChangeListener ( listen_assetprop );
            // Adding a_bogus2 to the asset repository should trigger a Property.assetsLinkingFrom
            // property-change event on listeners to a_bogus1 AssetModel
            olibAsset.syncAsset(a_bogus2.copy().fromId(a_bogus1.getId()).
                    timestamp(a_bogus2.getTimestamp() + 1).build());
            Thread.sleep(4000); // let any asynchrony work itself out
            assertTrue("AssetModel cascading properties correctly", 5 == edit_bogus.getLocalAsset().getValue());
        } catch (Exception e) {
            olog.log(Level.WARNING, "Caught unexpected: " + e, e);
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            olibAsset.remove(a_bogus1.getId());
            olibAsset.remove(a_bogus2.getId());
        }
    }

    /**
     * Test whether a loaded asset is automatically added
     * to the model library.  Just a very simple test.
     */
    public void testSessionHookup() {
        try {
            assertTrue("SearchManager is a service", osearch instanceof LittleService);
            olibAsset.remove(osession.getId());
            assertTrue("Session removed from model library",
                    null == olibAsset.get(osession.getId()));
            osearch.getAsset(osession.getId());
            assertTrue("Asset automatically added to model library on load",
                    null != olibAsset.get(osession.getId()));
            // Make sure that our client cache is getting wired up
            assertTrue("Client cache registration looks ok",
                    SimpleLittleService.getCacheCount() > 0
                    );
        } catch (Exception ex) {
            olog.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        }
    }
}

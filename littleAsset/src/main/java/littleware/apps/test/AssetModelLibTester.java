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
import com.google.inject.Provider;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.client.AbstractAssetEditor;
import littleware.apps.client.AssetEditor;
import littleware.apps.client.AssetModel;
import littleware.apps.client.AssetModelLibrary;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.base.feedback.LittleEvent;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.GenericAsset;
import littleware.asset.LittleHome;
import littleware.asset.client.LittleService;
import littleware.asset.client.SimpleLittleService;
import littleware.asset.test.AbstractAssetTest;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.LittlePrincipal;
import littleware.security.LittleUser;
import littleware.security.auth.LittleSession;

/**
 * Test the AssetModelLibrary, event propogation on
 * asset update, and hookup with
 * the ServiceProviderListener.
 */
public class AssetModelLibTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(AssetModelLibTester.class.getName());
    private final AssetModelLibrary assetLibrary;
    private final LittleSession session;
    private final AssetSearchManager search;
    private final Provider<GenericBuilder> genericProvider;

    @Inject
    public AssetModelLibTester(AssetModelLibrary libAsset,
            LittleSession session,
            AssetSearchManager search,
            Provider<GenericAsset.GenericBuilder> genericProvider
            ) {
        this.assetLibrary = libAsset;
        this.search = search;
        this.session = session;
        this.genericProvider = genericProvider;
        setName("testModelLibrary");
    }

    /**
     * Run the injected AssetModelLibrary through a few simple tests
     */
    public void testModelLibrary() {
        final List<Asset>  cleanupList = new ArrayList<Asset>();
        try {
            // couple bogus test assets - donot save to repository
            final LittleHome   home = getTestHome(search);
            final GenericAsset testAsset1 = genericProvider.get().name("bogus1").parent(home).build();
            final GenericAsset testAsset2 = genericProvider.get().name("bogus2").parent(home).build();

            cleanupList.add( testAsset1 );
            cleanupList.add( testAsset2 );
            assetLibrary.remove(session.getId());

            assertTrue("Simple sync is ok",
                    assetLibrary.syncAsset(session).getAsset() == session);
            assertTrue("No retrieval if not necessary",
                    assetLibrary.retrieveAssetModel(session.getId(), search).get().getAsset() == session);

            final AssetModel amodel_everybody =
                    assetLibrary.syncAsset(search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleGroup.GROUP_TYPE).get());
            assertTrue("ModelLibrary getByName inheritance aware 1",
                    assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittlePrincipal.PRINCIPAL_TYPE).isSet());
            assertTrue("ModelLibrary getByName inheritance aware 2",
                    !assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleUser.USER_TYPE).isSet());
            assertTrue("ModelLibrary getByName inheritance aware 3",
                    assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleGroup.GROUP_TYPE).isSet());
            assetLibrary.remove(amodel_everybody.getAsset().getId());
            assertTrue("ModelLibrary getByName cleared after remove",
                    !assetLibrary.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    LittleGroup.GROUP_TYPE).isSet());

            final AssetEditor edit_bogus = new AbstractAssetEditor(this) {

                @Override
                public void eventFromModel(LittleEvent event_property) {
                    // just do something - anything
                    log.log(Level.INFO, "Test editor received event from model, setting value to 5");
                    changeLocalAsset().narrow( GenericAsset.GenericBuilder.class ).setValue(5);
                }
            };
            edit_bogus.setAssetModel(assetLibrary.syncAsset(testAsset1)); //addPropertyChangeListener ( listen_assetprop );
            // Adding a_bogus2 to the asset repository should trigger a Property.assetsLinkingFrom
            // property-change event on listeners to a_bogus1 AssetModel
            assetLibrary.syncAsset(testAsset2.copy().parentId(testAsset1.getId()).
                    timestamp(testAsset2.getTimestamp() + 1).build());
            Thread.sleep(4000); // let any asynchrony work itself out
            assertTrue("AssetModel cascading properties correctly", 5 == edit_bogus.getLocalAsset().narrow( GenericAsset.class ).getValue());
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught unexpected: " + e, e);
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            for( Asset trash : cleanupList ) {
                assetLibrary.remove( trash.getId() );
            }
        }
    }

    /**
     * Test whether a loaded asset is automatically added
     * to the model library.  Just a very simple test.
     */
    public void testSessionHookup() {
        try {
            assertTrue("SearchManager is a service", search instanceof LittleService);
            assetLibrary.remove(session.getId());
            assertTrue("Session removed from model library",
                    null == assetLibrary.get(session.getId()));
            search.getAsset(session.getId());
            assertTrue("Asset automatically added to model library on load",
                    null != assetLibrary.get(session.getId()));
            // Make sure that our client cache is getting wired up
            assertTrue("Client cache registration looks ok",
                    SimpleLittleService.getCacheCount() > 0
                    );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        }
    }
}

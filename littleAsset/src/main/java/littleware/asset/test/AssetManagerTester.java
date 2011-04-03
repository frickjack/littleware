/*
 * Copyright 2007-2009,2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.*;
import littleware.base.*;
import littleware.security.*;
import littleware.test.LittleTest;

/**
 * Tester for implementations of the AssetManager interface 
 */
public class AssetManagerTester extends LittleTest {

    private static final Logger log = Logger.getLogger(AssetManagerTester.class.getName());
    private final AssetManager assetMgr;
    private final AssetSearchManager searchMgr;
    private final List<Asset> cleanupList = new ArrayList<Asset>();
    public final static String MS_TEST_HOME = "littleware.test_home";
    private final Provider<LittleUser> provideCaller;

    /**
     * Stash AssetManager instance to run tests against
     *
     * @param s_test_name of test to run - pass to super class
     * @param m_asset to test against
     * @param m_search to verify test results against
     */
    @Inject
    public AssetManagerTester(AssetManager m_asset, AssetSearchManager m_search,
            Provider<LittleUser> provideCaller ) {
        assetMgr = m_asset;
        searchMgr = m_search;
        this.provideCaller = provideCaller;
        setName("testAssetCreation");
    }

    /**
     * Try to remove the test-assets from the asset-tree
     */
    @Override
    public void tearDown() {
        try {
            for (Asset a_cleanup : cleanupList) {
                assetMgr.deleteAsset(a_cleanup.getId(), "Cleanup after test");
            }
        } catch (Exception e) {
            log.log(Level.INFO, "Failed to cleanup all test assets, caught: " + e);
        } finally {
            cleanupList.clear();
        }
    }

    /**
     * Just do some simple asset creationg/deletion/bla bla bla.
     */
    public void testAssetCreation() {
        try {
            final Asset home = searchMgr.getByName(MS_TEST_HOME, AssetType.HOME).get();
            final Asset acl = null;

            log.log(Level.INFO, "Running with test home: " + home);

            final LittleUser user = provideCaller.get();
            assertTrue("Have an authenticated user", null != user);
            final String s_name = "test_" + (new Date()).getTime();

            final Date t_now = new Date();
            log.log(Level.INFO, "Saving new asset");
            final Asset a_test = assetMgr.saveAsset(
                    AssetType.GENERIC.create().
                    name(s_name).
                    data("<data>no data </data>").
                    parent(user).
                    ownerId(user.getId()).
                    value(55).
                    state(3).
                    // Round end-date off to nearest second
                    endDate(new Date(t_now.getTime() - t_now.getTime() % 1000 + 1000 * 60 * 60L)).
                    putAttribute( "test", "test" ).
                    putLink( "test", UUID.randomUUID() ).
                    putDate( "test", new Date() ).
                    build(),
                    "new asset");

            log.log(Level.INFO, "Just created asset: " + s_name);
            assertTrue("Created an asset with some valid data",
                    (a_test.getId() != null) && a_test.getName().equals(s_name) && a_test.getAssetType().equals(AssetType.GENERIC)
                    && t_now.getTime() < a_test.getEndDate().getTime()
                    && (a_test.getTransaction() > 0)
                    && a_test.getDate("test").isSet()
                    && a_test.getAttribute( "test" ).getOr( "frick" ).equals( "test" )
                    && a_test.getLink("test" ).isSet()
                    );

            final Asset a_clone = a_test.copy().build();
            assertTrue("Able to clone new asset",
                    a_test.equals(a_clone) && a_clone.getName().equals(a_test.getName())
                    && a_clone.getAssetType().equals(a_test.getAssetType())
                    && a_clone.getTransaction() == a_test.getTransaction()
                    && a_clone.getAttributeMap().equals( a_test.getAttributeMap() )
                    && a_clone.getDateMap().equals( a_test.getDateMap() )
                    && a_clone.getLinkMap().equals( a_test.getLinkMap() )
                    );

            // Try to update the asset
            final Asset a_save = assetMgr.saveAsset(
                    a_test.copy().data("<data> some data </data>").
                    removeAttribute( "test" ).putAttribute( "test2", "test2" ).
                    removeLink( "test" ).putLink( "test2", UUID.randomUUID() ).
                    removeDate( "test" ).putDate( "test2", new Date() ).
                    build(),
                    "data update"
                    );
            assertTrue("Transaction count increases: " + a_save.getTransaction(),
                    a_save.getTransaction() > a_clone.getTransaction()
                    );
            assertTrue( "Property maps updated on save",
                    (a_save.getAttributeMap().size() == 1)
                    && (a_save.getLinkMap().size() == 1)
                    && (a_save.getDateMap().size() == 1)
                    && a_save.getAttribute( "test2" ).isSet()
                    && a_save.getLink( "test2" ).isSet()
                    && a_save.getDate( "test2" ).isSet()
                    );
            final Asset a_load = searchMgr.getAsset(a_test.getId()).get();
            assertTrue("Able to load new asset and data matches",
                    a_load.equals(a_clone) && a_clone.getName().equals(a_load.getName())
                    && a_clone.getAssetType().equals(a_load.getAssetType())
                    && a_clone.getEndDate().equals(a_load.getEndDate())
                    && (a_load.getValue() == 55.0) && (a_load.getState() == 3)
                    );

            // Delete the asset
            assetMgr.deleteAsset(a_clone.getId(), "Cleanup test case");

            assertTrue("No longer able to retrieve deleted asset: " + a_clone.getId(),
                    !searchMgr.getAsset(a_test.getId()).isSet()
                    );
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}


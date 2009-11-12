/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Inject;
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

    private static final Logger olog_generic = Logger.getLogger(AssetManagerTester.class.getName());
    private AssetManager om_asset = null;
    private AssetSearchManager om_search = null;
    private List<Asset> ov_cleanup_list = new ArrayList<Asset>();
    public final static String MS_TEST_HOME = "littleware.test_home";

    /**
     * Stash AssetManager instance to run tests against
     *
     * @param s_test_name of test to run - pass to super class
     * @param m_asset to test against
     * @param m_search to verify test results against
     */
    @Inject
    public AssetManagerTester(AssetManager m_asset, AssetSearchManager m_search) {
        om_asset = m_asset;
        om_search = m_search;
        setName("testAssetCreation");
    }

    /**
     * Try to remove the test-assets from the asset-tree
     */
    @Override
    public void tearDown() {
        try {
            for (Asset a_cleanup : ov_cleanup_list) {
                om_asset.deleteAsset(a_cleanup.getId(), "Cleanup after test");
            }
        } catch (Exception e) {
            olog_generic.log(Level.INFO, "Failed to cleanup all test assets, caught: " + e);
        } finally {
            ov_cleanup_list.clear();
        }
    }

    /**
     * Just do some simple asset creationg/deletion/bla bla bla.
     */
    public void testAssetCreation() {
        try {
            final Asset home = om_search.getByName(MS_TEST_HOME, AssetType.HOME).get();
            final Asset acl = null;

            olog_generic.log(Level.INFO, "Running with test home: " + home);

            final LittleUser user = SecurityAssetType.getAuthenticatedUserOrNull();
            assertTrue("Have an authenticated user", null != user);
            final String s_name = "test_" + (new Date()).getTime();

            final Date t_now = new Date();
            olog_generic.log(Level.INFO, "Saving new asset");
            final Asset a_test = om_asset.saveAsset(
                    AssetType.GENERIC.create().
                    name(s_name).
                    data("<data>no data </data>").
                    parent(user).
                    ownerId(user.getId()).
                    value(55).
                    state(3).
                    // Round end-date off to nearest second
                    endDate(new Date(t_now.getTime() - t_now.getTime() % 1000 + 1000 * 60 * 60L)).
                    build(),
                    "new asset");

            olog_generic.log(Level.INFO, "Just created asset: " + s_name);
            assertTrue("Created an asset with some valid data",
                    (a_test.getId() != null) && a_test.getName().equals(s_name) && a_test.getAssetType().equals(AssetType.GENERIC) && t_now.getTime() < a_test.getEndDate().getTime() && a_test.getTransaction() > 0);

            final Asset a_clone = a_test.copy().build();
            assertTrue("Able to clone new asset",
                    a_test.equals(a_clone) && a_clone.getName().equals(a_test.getName()) && a_clone.getAssetType().equals(a_test.getAssetType()) && a_clone.getTransaction() == a_test.getTransaction());

            // Try to update the asset
            
            final Asset a_save = om_asset.saveAsset(
                    a_test.copy().data("<data> some data </data>").build(),
                    "data update"
                    );
            assertTrue("Transaction count increases: " + a_save.getTransaction(),
                    a_save.getTransaction() > a_clone.getTransaction());
            final Asset a_load = om_search.getAsset(a_test.getId()).get();
            assertTrue("Able to load new asset and data matches",
                    a_load.equals(a_clone) && a_clone.getName().equals(a_load.getName())
                    && a_clone.getAssetType().equals(a_load.getAssetType())
                    && a_clone.getEndDate().equals(a_load.getEndDate())
                    && (a_load.getValue() == 55.0) && (a_load.getState() == 3)
                    );

            // Delete the asset
            om_asset.deleteAsset(a_clone.getId(), "Cleanup test case");

            assertTrue("No longer able to retrieve deleted asset: " + a_clone.getId(),
                    !om_search.getAsset(a_test.getId()).isSet()
                    );
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}


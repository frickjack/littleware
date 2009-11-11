/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Inject;
import littleware.asset.server.CacheManager;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.SecurityAssetType;

/**
 * Tester for implementations of the CacheManager interface, and TransactionManager
 */
public class CacheManagerTester extends TestCase {

    private static final Logger olog_generic = Logger.getLogger("littleware.asset.test.CacheManagerTester");
    private final AssetRetriever om_asset;
    private final CacheManager om_cache;

    /**
     * Stash CacheManager instance to run tests against,
     * and an AssetRetriever that uses the given CacheManager
     * to fascilitate the testing.
     *
     * @param s_test_name of test to run - pass to super class
     * @param m_cache to test against - gets cleared out as a side effect of tests
     * @param m_asset that does not access or add data to m_cache under the hood
     */
    @Inject
    public CacheManagerTester(CacheManager m_cache, AssetRetriever m_asset) {
        setName("testCache");
        om_cache = m_cache;
        om_asset = m_asset;
    }

    /**
     * Clear the cache
     */
    @Override
    public void setUp() {
        om_cache.clear();
    }

    /**
     * call setup
     */
    @Override
    public void tearDown() {
        setUp();
    }

    /**
     * Just load some test assets, put them into the cache.
     * Verify that the cache behaves as expected.
     */
    public void testCache() {
        try {
            Factory<UUID> factory_uuid = UUIDFactory.getFactory();
            UUID u_test = factory_uuid.create();

            assertTrue("Bogus UUID should not be in cache", om_cache.get(u_test) == null);

            Map<String, UUID> v_home_id = null;
            try {
                olog_generic.log(Level.INFO, "Calling getHomeAssetIds on empty cache");
                om_cache.getHomeAssetIds();
                assertTrue("Cache home-id lookup should have thrown cache-miss exception", false);
            } catch (CacheMissException e) {
                // expect to catch exception here ...
            } catch (Exception e) {
                olog_generic.log(Level.INFO, "Caught wrong type of exception: " + e +
                        ", " + BaseException.getStackTrace(e));
                assertTrue("Caught wrong type of exception: " + e, false);
            }

            v_home_id = om_asset.getHomeAssetIds();
            assertTrue("Home-asset set is not empty", !v_home_id.isEmpty());

            om_cache.setHomeAssetIds(v_home_id);

            olog_generic.log(Level.INFO, "Calling getHomeAssetIds on non-empty cache");
            Map<String, UUID> v_cache_data = om_cache.getHomeAssetIds();
            assertTrue("Cached homeid set has same size as original",
                    v_cache_data.size() == v_home_id.size());

            assertTrue("Test-home is in home set: " + AssetManagerTester.MS_TEST_HOME,
                    v_cache_data.containsKey(AssetManagerTester.MS_TEST_HOME));
            u_test = v_cache_data.get(AssetManagerTester.MS_TEST_HOME);
            Asset a_home = om_asset.getAsset(u_test).get();
            om_cache.put(u_test, a_home);
            a_home = om_cache.getAsset(u_test).get();
            assertTrue("Valid home data pulled out of cache",
                    a_home.getId().equals(u_test) && a_home.getName().equals(AssetManagerTester.MS_TEST_HOME) && a_home.getAssetType().equals(AssetType.HOME));
            // Change the asset-type, put it into the cache, and verify that our query updates
            om_cache.put(u_test, AssetType.GENERIC.create().id( u_test ).name( "bla" ).build() );
            a_home = om_cache.getAsset(u_test).get();
            assertTrue("Cache picked up our basic asset-type update",
                    a_home.getAssetType().equals(SecurityAssetType.USER));
            v_cache_data = om_cache.getHomeAssetIds();
            assertTrue("Cache removed changed asset from home-id result set",
                    !v_cache_data.containsKey(a_home.getName()));

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught: " + e, false);
        }
    }
}


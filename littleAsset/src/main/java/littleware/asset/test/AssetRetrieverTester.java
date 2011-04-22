/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.*;
import littleware.base.*;

/**
 * Tester for implementations of the AssetRetriever interface 
 */
public class AssetRetrieverTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(AssetRetrieverTester.class.getName());
    private final AssetRetriever search;

    /**
     * Stash AssetRetriever instance to run tests against
     *
     * @param search to test against
     */
    @Inject
    public AssetRetrieverTester(AssetRetriever search) {
        this.search = search;
        setName("testLoad");
    }

    /**
     * Just load some test assets.
     */
    public void testLoad() {
        try {
            final Map<String, UUID> v_home_id = search.getHomeAssetIds();
            assertTrue("Home-asset set is not empty", !v_home_id.isEmpty());
            for (String s_home : v_home_id.keySet()) {
                log.log(Level.INFO, "getHomeAssetIds found home: " + s_home);
            }
            assertTrue("Test-home is in home set: " + getTestHome(),
                    v_home_id.containsKey(getTestHome())
                    );
            /*...
            final Map<String,UUID> children = search.getAssetIdsFrom( v_home_id.get( getTestHome() ));
            final LittleGroup everybody = search.getAsset( children.get(AccountManager.LITTLEWARE_EVERYBODY_GROUP) ).get().narrow();
            final LittleUser  user = search.getAsset( children.get("littleware.test_user") ).get().narrow();
            assertTrue()
             */

            for( Map.Entry<String,UUID> entry : v_home_id.entrySet() ) {
                final Maybe<Asset> maybe = search.getAsset( entry.getValue() );
                assertTrue("Able to retrieve home: " + entry.getKey(),
                    maybe.isSet()
                    );
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught exception", e );
            assertTrue("Caught: " + e, false);
        }
    }

}



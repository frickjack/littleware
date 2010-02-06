/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo.test;

import com.google.gson.Gson;
import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.test.LittleTest;

/**
 * Test the Gson integration
 */
public class GsonTester extends LittleTest {
    private static final Logger log = Logger.getLogger( GsonTester.class.getName() );

    private final Gson gson;
    private final AssetSearchManager search;

    @Inject
    public GsonTester( Gson gson, AssetSearchManager search ) {
        this.gson = gson;
        this.search = search;
        setName( "testGson" );
    }

    public void testGson() {
        try {
            final Asset testAsset = getTestHome( search );
            final String result = gson.toJson( testAsset, Asset.class );
            log.log( Level.INFO, "Gson serialization returned: " + result );
            final Pattern pattern = Pattern.compile( "\"home\"\\s*:\\s*\"" + testAsset.getHomeId().toString() );
            assertTrue( "Gson result matches pattern", pattern.matcher(result).find() );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Test failed", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

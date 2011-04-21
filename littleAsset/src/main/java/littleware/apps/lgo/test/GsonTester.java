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
import com.google.inject.Provider;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import littleware.asset.Asset;
import littleware.asset.AssetSearchManager;
import littleware.asset.GenericAsset;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.asset.test.AbstractAssetTest;

/**
 * Test the Gson integration
 */
public class GsonTester extends AbstractAssetTest {
    private static final Logger log = Logger.getLogger( GsonTester.class.getName() );

    private final Gson gson;
    private final AssetSearchManager search;
    private final Provider<GenericBuilder> genericProvider;

    @Inject
    public GsonTester( Gson gson, AssetSearchManager search, Provider<GenericAsset.GenericBuilder> genericProvider ) {
        this.gson = gson;
        this.search = search;
        this.genericProvider = genericProvider;
        setName( "testGson" );
    }

    public void testGson() {
        try {
            log.log( Level.INFO, "Running testGson ..." );
            final Asset testAsset = genericProvider.get().parent( getTestHome( search )
                    ).name( "gsonTester" ).putAttribute( "test", "test" ).
                    putLink( "bla", UUID.randomUUID() ).
                    build();
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

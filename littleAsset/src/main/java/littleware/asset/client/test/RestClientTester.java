/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.test;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetInfo;
import littleware.asset.client.internal.RestSearchMgrProxy;
import littleware.security.auth.LittleSession;

public class RestClientTester extends littleware.test.LittleTest {
    private static final Logger log = Logger.getLogger( RestClientTester.class.getName() );
    private final RestSearchMgrProxy searchClient;
    private final Provider<LittleSession> sessionProvider;
    
    {
        setName( "testRestSearch" );
    }   
    
    @Inject
    public RestClientTester( 
            RestSearchMgrProxy searchClient,
            Provider<LittleSession> sessionProvider
            ) {
        this.searchClient = searchClient;
        this.sessionProvider = sessionProvider;
    }
    
    public void testRestSearch() {
        try {
            final LittleSession session = sessionProvider.get();
            final ImmutableMap<String,AssetInfo> homeMap = searchClient.getHomeAssetIds( session.getId(), -1L, 0 ).getData();
            assertTrue( "Found some home ids", ! homeMap.isEmpty() );
            assertTrue( "littleware.home in home id map", homeMap.containsKey("littleware.home" ) );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

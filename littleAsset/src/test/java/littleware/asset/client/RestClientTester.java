package littleware.asset.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetInfo;
import littleware.asset.client.internal.RestSearchMgrProxy;
import littleware.security.auth.LittleSession;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;



/**
 * Simple RestClient tester
 */
@RunWith(LittleTestRunner.class)
public class RestClientTester {
    private static final Logger log = Logger.getLogger( RestClientTester.class.getName() );
    private final RestSearchMgrProxy searchClient;
    private final Provider<LittleSession> sessionProvider;
     
    
    @Inject
    public RestClientTester( 
            RestSearchMgrProxy searchClient,
            Provider<LittleSession> sessionProvider
            ) {
        this.searchClient = searchClient;
        this.sessionProvider = sessionProvider;
    }
    
    @Test
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

package littleware.apps.fishRunner.test;

import com.google.inject.Inject;
import java.io.File;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.test.LittleTestRunner;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Test the glassfish setup, etc.
 */
@RunWith(LittleTestRunner.class)
public class FishCase 
{
    private static final Logger log = Logger.getLogger( FishCase.class.getName() );
    private final GlassFish      gf;
    private final TestConfig config;
    
    /**
     * Little helper class to simplify injection of some
     * environment-dependent configuration information.
     * PackageTest configures these properties at startup time ...
     */
    public static class TestConfig {
        public final File testWar;
        
        @Inject
        public TestConfig( File testWar ) {
            this.testWar = testWar;
        }
    }
    
    
    /**
     * Create the test case
     */
    @Inject
    public FishCase( GlassFish gf, TestConfig config )
    {
        this.gf = gf;
        this.config = config;
    }


    /**
     * Just see if we can startup glassfish
     */
    @Test
    public void testGlassFish()
    {
        try {
            assertTrue( "Test war exists: " + config.testWar,
                    config.testWar.exists()
                    );
            assertTrue( "Glassfish is running",
                    EnumSet.of( GlassFish.Status.STARTED, GlassFish.Status.STARTING ).contains( gf.getStatus() )
                    );
            log.log( Level.INFO, "Trying to deploy test war: {0}", config.testWar);
            final Deployer deployer = gf.getDeployer();
            deployer.deploy( config.testWar, "--force=true");
        } catch (Exception ex) {
            log.log( Level.WARNING, "test failed", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

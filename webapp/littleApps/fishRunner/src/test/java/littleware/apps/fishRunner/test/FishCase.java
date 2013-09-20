/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.fishRunner.test;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.File;
import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import org.glassfish.embeddable.Deployer;
import org.glassfish.embeddable.GlassFish;

/**
 * Test the glassfish setup, etc.
 */
public class FishCase 
    extends TestCase
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
        super( "testGlassFish" );
        this.gf = gf;
        this.config = config;
    }


    /**
     * Just see if we can startup glassfish
     */
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

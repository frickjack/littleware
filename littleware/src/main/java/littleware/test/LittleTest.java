package littleware.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;

/**
 * Some utilities for tests
 */
public final class LittleTest {
    /** Every test wants a logger */
    public static final Logger log = Logger.getLogger( LittleTest.class.getName() );
    
    /**
     * Typical exception handler
     */
     public static void handle( Throwable ex ) {
         log.log( Level.WARNING, "Failed test", ex );
         fail( "Caught: " + ex );
     } 
   
}

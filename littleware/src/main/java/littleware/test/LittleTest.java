package littleware.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import static org.junit.Assert.*;

/**
 * Slight specialization of junit.framework.TestCase
 * adds putName method to allow simultaneously setting
 * a TestCase test-method name and register the test-case
 * with a suite:  suite.addTest ( provider.get().putName( "testWhatever" ) )
 */
public abstract class LittleTest {
    /** Every test wants a logger */
    public final Logger log = Logger.getLogger( getClass().getName() );
    
    /**
     * Typical exception handler
     */
     public void handle( Throwable ex ) {
         log.log( Level.WARNING, "Failed test", ex );
         fail( "Caught: " + ex );
     } 
   
}

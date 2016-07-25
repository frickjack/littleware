package littleware.test;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.LittleBootstrap;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * JUnit4 test runner enabled with littleware.bootstrap Guice bootrap and
 * injection. Imitation of SpringJunit4TestRunner:
 *
 */
public class LittleTestRunner extends BlockJUnit4ClassRunner {
    private static final Logger log = Logger.getLogger(LittleTestRunner.class.getName());

    /**
     * Disable BlockJUnit4ClassRunner test-class constructor rules
     */
    @Override
    protected void validateConstructor( List<Throwable> errors ) {}
    
    /**
     * Construct a new {@code LittleTestRunner} and initialize a
     * {@link LittleBootstrap} to provide littleware testing functionality to
     * standard JUnit tests.
     *
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public LittleTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "constructor called with [{0}]", clazz);
        }
    }

    /**
     * This is where littleware hooks in
     * 
     * @return an instance of getClass constructed via the littleware managed Guice injector
     */
    @Override
    protected Object createTest() {
        try {
            return LittleBootstrap.factory.lookup(this.getTestClass().getJavaClass());
        } catch ( RuntimeException ex ) {
            log.log( Level.SEVERE, "Test class construction failed", ex );
            throw ex;
        }
    }
}

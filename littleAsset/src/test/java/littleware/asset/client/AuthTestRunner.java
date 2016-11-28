package littleware.asset.client;

import com.google.inject.Injector;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.base.login.LoginCallbackHandler;
import littleware.bootstrap.LittleBootstrap;
import org.junit.runners.BlockJUnit4ClassRunner;
import org.junit.runners.model.InitializationError;

/**
 * Utility to setup TestSuite that can bootstrap a littleware environment in
 * various configurations, and authenticates as a test user
 */
public class AuthTestRunner extends BlockJUnit4ClassRunner {

    private static final Logger log = Logger.getLogger(AuthTestRunner.class.getName());
    private final String testUserPassword = "test123";

    /**
     * Disable BlockJUnit4ClassRunner test-class constructor rules
     */
    @Override
    protected void validateConstructor(List<Throwable> errors) {
    }

    /**
     * Construct a new {@code LittleTestRunner} and initialize a
     * {@link LittleBootstrap} to provide littleware testing functionality to
     * standard JUnit tests.
     *
     * @param clazz the test class to be run
     * @see #createTestContextManager(Class)
     */
    public AuthTestRunner(Class<?> clazz) throws InitializationError {
        super(clazz);
        if (log.isLoggable(Level.FINE)) {
            log.log(Level.FINE, "constructor called with [{0}]", clazz);
        }
    }

    /**
     * This is where littleware hooks in
     *
     * @return an instance of getClass constructed via the littleware managed
     * Guice injector
     */
    @Override
    protected Object createTest() {
        try {
            //ServerBootstrap.provider.get().build() ... ?
            final Injector sessionInjector = LittleBootstrap.factory.lookup(Injector.class);

            // Login as test user - go through SessionManager
            try {
                final LoginContext context = new LoginContext("littleware.login", new Subject(),
                        new LoginCallbackHandler(AbstractAssetTest.getTestUserName(), testUserPassword),
                        sessionInjector.getInstance(javax.security.auth.login.Configuration.class));
                context.login();
            } catch (LoginException ex) {
                throw new IllegalStateException("Failed to setup test-user session", ex);
            }

            return sessionInjector.getInstance(this.getTestClass().getJavaClass());

        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test class construction failed", ex);
            throw ex;
        }
    }
}

/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client;

import com.google.inject.Injector;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import javax.security.auth.login.LoginException;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.base.login.LoginCallbackHandler;
import littleware.bootstrap.LittleBootstrap;

/**
 * Utility to setup TestSuite that can bootstrap
 * a littleware environment in various configurations.
 */
public class AssetTestFactory extends littleware.test.TestFactory {

    private static final Logger log = Logger.getLogger(AssetTestFactory.class.getName());
    private final String testUserPassword;
    
    /**
     * Inject a password to authenticate the littleware.test_user with 
     * @param testUserPassword 
     */
    public AssetTestFactory( String testUserPassword ) {
        this.testUserPassword = testUserPassword;
    }
    
    /**
     * Constructor sets test-user password to "test123"
     */
    public AssetTestFactory() {
        this( "test123" );
    }

    /**
     * Bootstraps a test session
     * within a parent server or app environment, and
     * return a test suite that runs the given tests
     * as a client in the client environment, then shuts
     * down both environments.
     */
    @Override
    public <T extends TestSuite> T build(final LittleBootstrap boot,
            final Class<T> clazz) {

        final Injector sessionInjector = boot.bootstrap(Injector.class);
        //final Injector      sessionInjector = new SimpleSessionBuilder( AppProfile.CliApp, shutdown.getInjector() ).build().startSession( Injector.class );

        // Login as test user - go through SessionManager
        try {
            final LoginContext context = new LoginContext("littleware.login", new Subject(),
                    new LoginCallbackHandler( AbstractAssetTest.getTestUserName(), testUserPassword ),
                    sessionInjector.getInstance(javax.security.auth.login.Configuration.class));
            context.login();
        } catch (LoginException ex) {
            System.out.println("Failed to login: " + ex);
            throw new IllegalStateException("Failed to setup test-user session", ex);
        }

        final T result = sessionInjector.getInstance(clazz);
        result.addTest(new TestCase("shutdownTest") {

            @Override
            public void runTest() {
                boot.shutdown();
            }
        });

        return result;
    }
}

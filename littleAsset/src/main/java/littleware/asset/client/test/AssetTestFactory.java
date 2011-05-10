/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.test;

import com.google.inject.Inject;
import java.util.logging.Logger;
import junit.framework.TestSuite;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.asset.client.bootstrap.ClientBootstrap.ClientBuilder;
import littleware.bootstrap.LittleBootstrap;

/**
 * Utility to setup TestSuite that can bootstrap
 * a littleware environment in various configurations.
 */
public class AssetTestFactory extends littleware.test.TestFactory {

    private static final Logger log = Logger.getLogger(AssetTestFactory.class.getName());

    public static class NullTestSuite extends TestSuite {
        private final ClientBootstrap.ClientBuilder clientBuilder;

        @Inject
        public NullTestSuite( ClientBootstrap.ClientBuilder clientBuilder ) {
            setName(getClass().getName());
            this.clientBuilder = clientBuilder;
        }

        public ClientBuilder getClientBuilder() {
            return clientBuilder;
        }
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
            final Class<T> clazz
            ) {
        final NullTestSuite shutdown = super.build( boot, NullTestSuite.class );
        final T             testSuite = shutdown.getClientBuilder().build().startTestSession(clazz);
        testSuite.addTest( shutdown );
        return testSuite;
    }
}

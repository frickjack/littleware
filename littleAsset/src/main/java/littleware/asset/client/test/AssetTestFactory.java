/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.client.test;

import java.util.logging.Logger;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap;

/**
 * Utility to setup TestSuite that can bootstrap
 * a littleware environment in various configurations.
 */
public class AssetTestFactory extends littleware.test.TestFactory {

    private static final Logger log = Logger.getLogger(AssetTestFactory.class.getName());

    public TestSuite buildClientTest( final ClientBootstrap clientBoot,
            Class<? extends TestSuite> clazz
            ) {
        clientBoot.bootstrap();
        final TestSuite suite = clientBoot.startTestSession(clazz);
        suite.addTest( new TestCase( "shutdownClient" ) {
            @Override
            public void runTest() {
                clientBoot.shutdown();
            }
        } );
        return suite;
    }

    /**
     * Bootstraps a client environment
     * within a server environment, and
     * return a test suite that runs the given tests
     * as a client in the client environment, then shuts
     * down both environments.
     *
     * @param clientBuilder to invoke test() login on once
     *          embedded server environment is up and running
     */
    public TestSuite build(final ServerBootstrap serverBoot,
            final ClientBootstrap clientBuilder,
            final Class<? extends TestSuite> clazz) {

        serverBoot.bootstrap();
        final TestSuite suite = buildClientTest( clientBuilder, clazz );
        suite.addTest( new TestCase( "shutdownServer" ) {
            @Override
            public void runTest() {
                serverBoot.shutdown();
            }
        } );
        return suite;
    }
}

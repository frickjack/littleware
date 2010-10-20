/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.base.EventBarrier;
import littleware.bootstrap.LittleBootstrap;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.bootstrap.server.ServerBootstrap;

/**
 * Utility to setup TestSuite that can bootstrap
 * a littleware environment in various configurations.
 */
public class TestFactory {

    private static final Logger log = Logger.getLogger(TestFactory.class.getName());

    public static class SetupBarrier extends EventBarrier<TestSuite> {
    }



    /**
     * Bootstrap a littleware environment, and
     * return a test suite ready to run against that
     * environment that shuts down
     * the environment in the last test.
     */
    public TestSuite build(final LittleBootstrap bootstrap,
            final Class<? extends TestSuite> testSuiteClass) {
        final SetupBarrier suiteBarrier = new SetupBarrier();
        try {
            final TestSuite suite = bootstrap.bootstrap( testSuiteClass );
            suite.addTest(new TestCase( "shutdownTest" ) {
                @Override
                public void runTest() {
                    bootstrap.shutdown();
                }
            });
            log.log(Level.INFO, "Returning TestSuite");
            return suite;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "Bootstrap failed", ex);
            throw new IllegalStateException("Test setup interrupted", ex);
        }
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
    public TestSuite build(final ServerBootstrap serverBootstrap,
            final ClientBootstrap.LoginSetup clientBuilder,
            final Class<? extends TestSuite> testSuiteClass) {

        serverBootstrap.bootstrap();
        final TestSuite suite = build(clientBuilder.test(), testSuiteClass);
        suite.addTest(
                new TestCase("shutdownLittlewareServer") {
                    @Override
                    public void runTest() {
                        serverBootstrap.shutdown();
                    }
                });
        log.log(Level.INFO, "Returning TestSuite");
        return suite;
    }
}

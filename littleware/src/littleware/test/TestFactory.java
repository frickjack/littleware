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

import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Module;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.base.EventBarrier;
import littleware.security.auth.GuiceOSGiBootstrap;
import littleware.security.auth.RunnerActivator;

/**
 * Utility to setup TestSuite that can bootstrap
 * a littleware environment in various configurations.
 */
public class TestFactory {

    private static final Logger log = Logger.getLogger(TestFactory.class.getName());

    public static class SetupBarrier extends EventBarrier<TestSuite> {
    }

    /**
     * Internal class - only visible to avoid Guice AOP
     */
    public static class SuiteActivator extends RunnerActivator {

        private final TestSuite suite;
        private final EventBarrier<TestSuite> barrier;

        @Inject
        public SuiteActivator(@Named("TestFactory.Suite") final TestSuite userTestSuite,
                SetupBarrier barrier) {
            this.suite = userTestSuite;
            this.barrier = barrier;
        }

        @Override
        public void run() {
            log.log(Level.FINE, "SuiteActivator started");
            // FrameworkEvent.STARTED never comes when running
            // multiple OSGi environments ... ugh!
            barrier.publishEventData(suite);
        }

    }


    /**
     * Bootstrap a littleware environment, and
     * return a test suite ready to run against that
     * environment that shuts down
     * the environment in the last test.
     */
    public TestSuite build(final GuiceOSGiBootstrap bootstrap,
            final Class<? extends TestSuite> testSuiteClass) {
        final SetupBarrier suiteBarrier = new SetupBarrier();
        bootstrap.getGuiceModule().add(
                new Module() {

                    @Override
                    public void configure(Binder binder) {
                        binder.bind(TestSuite.class).annotatedWith(Names.named("TestFactory.Suite")).to(testSuiteClass);
                        binder.bind(SetupBarrier.class).toInstance(suiteBarrier);
                    }
                });
        bootstrap.getOSGiActivator().add(SuiteActivator.class);

        try {
            bootstrap.bootstrap();
            log.log(Level.INFO, "Waiting for OSGi startup ...");
            final TestSuite suite = suiteBarrier.waitForEventData();
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
     */
    public TestSuite build(final GuiceOSGiBootstrap serverBootstrap,
            final GuiceOSGiBootstrap clientBootstrap,
            final Class<? extends TestSuite> testSuiteClass) {

        serverBootstrap.bootstrap();
        final TestSuite suite = build(clientBootstrap, testSuiteClass);
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

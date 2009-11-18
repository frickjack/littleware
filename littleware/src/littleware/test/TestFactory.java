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
import littleware.security.auth.LittleBootstrap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

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
    public static class SuiteActivator implements BundleActivator {

        private final TestSuite suite;
        private final EventBarrier<TestSuite> barrier;

        @Inject
        public SuiteActivator(@Named("TestFactory.Suite") final TestSuite userTestSuite,
                SetupBarrier barrier) {
            this.suite = userTestSuite;
            this.barrier = barrier;
        }

        @Override
        public void start(final BundleContext ctx) throws Exception {
            log.log( Level.FINE, "SuiteActivator started" );
            // FrameworkEvent.STARTED never comes when running
            // multiple OSGi environments ... ugh!
            barrier.publishEventData(suite);
            /*..
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ((evt.getType() == FrameworkEvent.STARTED)) {
                        ctx.removeFrameworkListener(this);
                        log.log( Level.FINE, "OSGi started, publishing TestSuite" );
                        barrier.publishEventData(suite);
                    } else {
                        log.log( Level.FINE, "Got event: " + evt );
                    }
                }
            });
             */
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
        }
    }

    public static class ShutdownTest extends TestCase {

        private final LittleBootstrap bootstrap;

        public ShutdownTest(LittleBootstrap bootstrap) {
            super("shutdownLittleware");
            this.bootstrap = bootstrap;
        }

        public void shutdownLittleware() {
            try {
                bootstrap.shutdown();
            } catch (Exception ex) {
                log.log(Level.WARNING, "Shutdown failed", ex);
                fail("Shutdown failed: " + ex);
            }
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
            log.log( Level.INFO, "Waiting for OSGi startup ..." );
            final TestSuite suite = suiteBarrier.waitForEventData();
            suite.addTest(new ShutdownTest( bootstrap ) );
            log.log( Level.INFO, "Returning TestSuite" );
            return suite;
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Bootstrap failed", ex );
            throw new IllegalStateException("Test setup interrupted", ex);
        }
    }

    /**
     * Internal class - only visible to avoid Guice AOP
     */
    public static class ClientData {

        private final Class<? extends TestSuite> suiteClass;
        private final TestFactory factory;

        public TestFactory getFactory() {
            return factory;
        }

        public GuiceOSGiBootstrap getBootstrap() {
            return bootstrap;
        }

        public Class<? extends TestSuite> getSuiteClass() {
            return suiteClass;
        }
        private final GuiceOSGiBootstrap bootstrap;
        private final EventBarrier<TestSuite> barrier = new EventBarrier<TestSuite>();

        public EventBarrier<TestSuite> getBarrier() {
            return barrier;
        }

        public ClientData(GuiceOSGiBootstrap bootstrap,
                Class<? extends TestSuite> suiteClass,
                TestFactory factory) {
            this.suiteClass = suiteClass;
            this.bootstrap = bootstrap;
            this.factory = factory;
        }
    }

    public static class ServerActivator implements BundleActivator {

        private final ClientData clientData;

        @Inject
        public ServerActivator(ClientData clientData) {
            this.clientData = clientData;
        }

        @Override
        public void start(final BundleContext ctx) throws Exception {
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ((evt.getType() == FrameworkEvent.STARTED)) {
                        ctx.removeFrameworkListener(this);
                        log.log( Level.INFO, "Launching client test ..." );
                        try {
                            clientData.getBarrier().publishEventData(
                                clientData.getFactory().build(clientData.getBootstrap(), clientData.getSuiteClass()));
                        } catch ( Exception ex ) {
                            log.log( Level.WARNING, "Failed client-test launch", ex);
                            clientData.getBarrier().publishEventData(null);
                        }
                    }
                }
            });

        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
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
        final ClientData data = new ClientData(clientBootstrap, testSuiteClass, this);
        serverBootstrap.getGuiceModule().add(new Module() {

            @Override
            public void configure(Binder binder) {
                binder.bind(ClientData.class).toInstance(data);
            }
        });
        serverBootstrap.getOSGiActivator().add(ServerActivator.class);
        try {
            serverBootstrap.bootstrap();
            final TestSuite suite = data.getBarrier().waitForEventData();
            suite.addTest(
                    new TestCase("shutdownLittlewareServer") {

                        public void shutdownLittlewareServer() {
                            serverBootstrap.shutdown();
                        }
                    });
            log.log( Level.INFO, "Returning TestSuite" );
            return suite;
        } catch (InterruptedException ex) {
            throw new IllegalStateException("Bootstrap failed", ex);
        }
    }
}

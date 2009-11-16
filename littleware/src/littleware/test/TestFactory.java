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
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.base.EventBarrier;
import littleware.security.auth.GuiceOSGiBootstrap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * Utility to setup TestSuite that can bootstrap
 * a littleware environment in various configurations.
 */
public class TestFactory {

    /**
     * Internal class - only visible to avoid Guice AOP
     */
    public static class SuiteActivator implements BundleActivator {
        private final TestSuite suite;
        private final EventBarrier<TestSuite> barrier;
        @Inject
        public SuiteActivator( @Named("TestFactory.Suite" ) final TestSuite userTestSuite,
                @Named("TestFactory.SuiteBarrier") EventBarrier<TestSuite> barrier ) {
            this.suite = userTestSuite;
            this.barrier = barrier;
        }

        @Override
        public void start(final BundleContext ctx) throws Exception {
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ((evt.getType() == FrameworkEvent.STARTED) ) {
                        ctx.removeFrameworkListener(this);
                        barrier.publishEventData(suite);
                    }
                }
            });
        }

        @Override
        public void stop(BundleContext ctx) throws Exception {
            
        }

    }
    

    /**
     * Bootstrap a littleware environment, and
     * return a test suite ready to run against that
     * environment that shuts down
     * the environment in the last test.
     */
    public TestSuite build( final GuiceOSGiBootstrap bootstrap,
            final Class<? extends TestSuite> testSuiteClass
            ) {
        final EventBarrier<TestSuite>  suiteBarrier = new EventBarrier<TestSuite>();
        bootstrap.getGuiceModule().add(
                new Module() {
            @Override
            public void configure(Binder binder) {
                binder.bind( TestSuite.class ).annotatedWith( Names.named( "TestFactory.Suite" )).to( testSuiteClass );
                binder.bind( EventBarrier.class ).annotatedWith( Names.named( "TestFactory.SuiteBarrier" )).toInstance(suiteBarrier);
            }
        }
                );
        bootstrap.getOSGiActivator().add(SuiteActivator.class);
        bootstrap.bootstrap();
        try {
            final TestSuite suite = suiteBarrier.waitForEventData();
            suite.addTest( new TestCase( "shutdownLittleware" ) {
                public void shutdownLittleware() {
                    bootstrap.shutdown();
                }
            }
            );
            return suite;
        } catch (InterruptedException ex) {
            throw new IllegalStateException( "Test setup interrupted", ex );
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
        
        public ClientData( GuiceOSGiBootstrap bootstrap,
                Class<? extends TestSuite> suiteClass,
                TestFactory factory ) {
            this.suiteClass = suiteClass;
            this.bootstrap = bootstrap;
            this.factory = factory;
        }
    }

    public static class ServerActivator implements BundleActivator {
        private final ClientData clientData;

        @Inject
        public ServerActivator( ClientData clientData
               ) {
            this.clientData = clientData;
        }

        @Override
        public void start( final BundleContext ctx) throws Exception {
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ((evt.getType() == FrameworkEvent.STARTED) ) {
                        ctx.removeFrameworkListener(this);
                        clientData.getBarrier().publishEventData(
                            clientData.getFactory().build( clientData.getBootstrap(), clientData.getSuiteClass() )
                                );
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
    public TestSuite build( final GuiceOSGiBootstrap serverBootstrap,
            final GuiceOSGiBootstrap clientBootstrap,
            final Class<? extends TestSuite> testSuiteClass
            ) {
        final ClientData     data = new ClientData( clientBootstrap, testSuiteClass, this );
        serverBootstrap.getGuiceModule().add( new Module() {

            @Override
            public void configure(Binder binder) {
                binder.bind( ClientData.class ).toInstance(data);
            }
        } );
        serverBootstrap.getOSGiActivator().add(ServerActivator.class );
        try {
            serverBootstrap.bootstrap();
            final TestSuite suite = data.getBarrier().waitForEventData();
            suite.addTest(
                    new TestCase( "shutdownLittlewareServer" ) {
                public void shutdownLittlewareServer() {
                    serverBootstrap.shutdown();
                }
            }
                    );
            return suite;
        } catch (InterruptedException ex) {
            throw new IllegalStateException( "Bootstrap failed", ex );
        }
    }
}

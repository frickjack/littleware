/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.demo.simpleCL;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import littleware.apps.client.ClientBootstrap;
import littleware.base.AssertionFailedException;
import littleware.base.EventBarrier;
import littleware.security.auth.LittleBootstrap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;
import org.osgi.framework.FrameworkEvent;
import org.osgi.framework.FrameworkListener;

/**
 * Test suite for simpleCL package
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    @Inject
    public PackageTestSuite( Provider<SimpleCLTester> provideSimpleCL,
            final LittleBootstrap bootstrap
            ) {
        super( PackageTestSuite.class.getName() );
        this.addTest( provideSimpleCL.get() );
        this.addTest( new TestCase( "testShutdown" ) {
            public void testShutdown () {
                bootstrap.shutdown();
            }
        });
    }

    public static class TestLauncher implements BundleActivator {
        public static EventBarrier<TestSuite> barrier = new EventBarrier<TestSuite>();
        private final PackageTestSuite suite;

        @Inject
        public TestLauncher( PackageTestSuite suite ) {
            this.suite = suite;
        }

        @Override
        public void start( final BundleContext ctx) throws Exception {
            ctx.addFrameworkListener(new FrameworkListener() {

                @Override
                public synchronized void frameworkEvent(FrameworkEvent evt) {
                    if ( evt.getType() == FrameworkEvent.STARTED ) {
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
    
    public static TestSuite suite() {
        final ClientBootstrap bootstrap = new ClientBootstrap( "localhost" );
        bootstrap.getOSGiActivator().add( TestLauncher.class );
        try {
            return TestLauncher.barrier.waitForEventData();
        } catch (InterruptedException ex) {
            log.log( Level.WARNING, "Failed bootstrap", ex );
            throw new AssertionFailedException( "Failed bootstrap", ex );
        }
    }

    public static void main( String[] v_args ) {
	String[] v_test_args = {"-noloading", PackageTestSuite.class.getName() };
        //Test suite = suite ();
        log.log ( Level.INFO, "Trying to setup test gui for: " + PackageTestSuite.class.getName () );
	junit.swingui.TestRunner.main(v_test_args);
    }

}

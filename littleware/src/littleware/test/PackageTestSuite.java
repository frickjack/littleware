/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.test;

import com.google.inject.Binder;
import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.TypeLiteral;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.*;
import javax.swing.SwingUtilities;
import junit.framework.*;
import littleware.apps.client.AssetModelServiceListener;
import littleware.apps.client.ClientBootstrap;
import littleware.asset.AssetSearchManager;
import littleware.base.BaseException;
import littleware.base.EventBarrier;
import littleware.base.PropertiesGuice;
import littleware.security.auth.client.ClientCache;
import littleware.security.auth.server.ServerBootstrap;
import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

/**
 * Test suite constructor that pulls together tests from
 * every littleware.*.test package.
 */
public class PackageTestSuite extends ServerTestLauncher {

    private static final Logger olog = Logger.getLogger(PackageTestSuite.class.getName());
    private ClientBootstrap clientTestBootstrap;

    @Inject
    public PackageTestSuite(
            littleware.base.test.PackageTestSuite suite_base,
            //littleware.db.test.PackageTestSuite suite_db,
            littleware.asset.test.PackageTestSuite suite_asset,
            littleware.security.test.PackageTestSuite suite_security,
            littleware.security.auth.server.SimpleDbLoginConfiguration config,
            AssetSearchManager search) {
        super(PackageTestSuite.class.getName(), search);
        // disable server tests
        final boolean bRun = true;

        olog.log(Level.INFO, "Trying to setup littleware.test test suite");
        try {
            if (bRun) {
                olog.log(Level.INFO, "Trying to setup littleware.base test suite");
                this.addTest(suite_base);
            }

            if (bRun) {
                olog.log(Level.INFO, "Trying to setup littleware.db test suite");
                olog.log(Level.INFO, "Test disabled ... does not apply when running with JPA");
                //this.addTest( suite_db );
            }

            if (bRun) {
                olog.log(Level.INFO, "Trying to setup littleware.asset test suite");
                this.addTest(suite_asset);
            }

            if (bRun) {
                olog.log(Level.INFO, "Trying to setup littleware.security test suite");
                this.addTest(suite_security);
            }

        } catch (RuntimeException e) {
            olog.log(Level.SEVERE, "Failed to setup test suite, caught: " + e + ", " + BaseException.getStackTrace(e));
            throw e;
        }
        olog.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    /**
     * Just call through to ServerTestLauncher.suite() - should only
     * invoke when this is the master SeverTestLauncher TestSuite.
     */
    public static Test suite() {
        return ServerTestLauncher.suite();
    }

    /**
     * Boot the littleware server OSGi environment,
     * and register this master test suite as a BundleActivator.
     */
    public static void main(String[] v_args) {
        ServerBootstrap boot = new ServerBootstrap(true);
        boot.getOSGiActivator().add(PackageTestSuite.class);
        boot.bootstrap();
    }

    /**
     * Internal utility runs after OSGi server side bootstrap -
     * registers client-side test cases via a separate
     * Guice injection process.
     */
    private ClientBootstrap addClientTests() throws IOException {
        final ClientBootstrap bootstrap = new ClientBootstrap();
        final EventBarrier<Test> barrier = new EventBarrier<Test>();
        /*...
        Injector injector = Guice.createInjector(new Module[]{
        new littleware.apps.swingclient.StandardSwingGuice(),
        new littleware.apps.client.StandardClientGuice(),
        new littleware.apps.misc.StandardMiscGuice(),
        new littleware.security.auth.ClientServiceGuice(),
        new PropertiesGuice(littleware.base.PropertiesLoader.get().loadProperties())
        });
        // Hack to setup client-side service listeners -
        //         normally done by client-side OSGi
        injector.getInstance(AssetModelServiceListener.class);
        injector.getInstance(ClientCache.class);
         */
        bootstrap.getGuiceModule().add(new Module() {

            @Override
            public void configure(Binder binder) {
                binder.bind(new TypeLiteral<EventBarrier<Test>>() {
                }).toInstance(barrier);
            }
        });

        bootstrap.getOSGiActivator().add(ClientTestSuite.class);
        bootstrap.bootstrap();
        try {
            if ( true ) {
                this.addTest(barrier.waitForEventData());
            }
        } catch (InterruptedException ex) {
            throw new IllegalStateException("Failed to setup client tests", ex);
        }
        return bootstrap;
    }

    /**
     * Internal activator configures ClientTestSuite on Client OSGi bundle
     */
    public static class ClientTestSuite extends TestSuite implements BundleActivator {

        private final EventBarrier<Test> barrier;

        @Inject
        public ClientTestSuite(littleware.apps.test.PackageTestSuite suite, EventBarrier<Test> barrier) {
            this.addTest(suite);
            this.barrier = barrier;
        }

        @Override
        public void start(BundleContext arg0) throws Exception {
            barrier.publishEventData(this);
        }

        @Override
        public void stop(BundleContext arg0) throws Exception {
        }

        /*
        final boolean bRun = true;

        if (false) {
        // TODO - guice enable JSF beans
        // TODO - move web and apps test cases over to ClientTestSuite
        olog.log(Level.INFO, "Trying to setup littleware.web test suite");
        this.addTest(injector.getInstance(littleware.web.test.PackageTestSuite.class));
        }

        if (bRun) {
        // TODO - workout OSGi bootstrap with server framework
        olog.log(Level.INFO, "Trying to setup littleware.apps test suite");
        this.addTest(injector.getInstance(littleware.apps.test.PackageTestSuite.class));
        }
         */
    }

    /**
     * Override default implementation to add client test suite
     *
     * @param ctx
     * @throws java.lang.Exception
     */
    @Override
    public void start(BundleContext ctx) throws Exception {
        clientTestBootstrap = addClientTests();
        super.start(ctx);
    }

    @Override
    public void stop(BundleContext arg0) throws Exception {
        clientTestBootstrap.shutdown();
    }
}

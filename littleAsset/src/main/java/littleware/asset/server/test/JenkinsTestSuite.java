/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.test;


import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.test.AssetManagerTester;
import littleware.asset.client.test.AssetSearchManagerTester;
import littleware.asset.server.LittleContext;
import littleware.asset.server.LittleContext.ContextFactory;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.server.db.test.DbAssetManagerTester;
import littleware.security.auth.LittleSession;
import littleware.test.LittleTest;
import littleware.test.TestFactory;

/**
 * Test suite for littleware.asset package
 */
public class JenkinsTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger(JenkinsTestSuite.class.getName());

    /**
     * Session-level module for setting up test cases around
     * a single LittleContext
     */
    public static class SessionModule implements Module {
        private final LittleContext ctx;
        private final Class<? extends LittleTest> clazz;
        /**
         * Construct a Module to
         * bind TestCase.class to clazz in and LittleContext.class to ctx
         */
        public SessionModule( LittleContext ctx, Class<? extends LittleTest> clazz ) {
            this.ctx = ctx;
            this.clazz = clazz;
        }

        @Override
        public void configure(Binder binder) {
            binder.bind( LittleContext.class ).toInstance(ctx);
            binder.bind( AssetSearchManager.class ).to( MockSearchManager.class ).in( Scopes.SINGLETON );
            binder.bind( AssetManager.class ).to( MockAssetManager.class ).in( Scopes.SINGLETON );
            binder.bind( LittleTest.class ).to( clazz );
            binder.bind( DelegateTester.class ).in( Scopes.SINGLETON );
        }
    }

    public static class SessionBuilder {
        private final Injector injector;
        private final LittleSession session;
        private final ContextFactory factory;
        @Inject
        public SessionBuilder( Injector injector, 
                LittleSession session,
                ContextFactory factory ) {
            this.injector = injector;
            this.session = session;
            this.factory = factory;
        }

        public DelegateTester buildInContext( Class<? extends LittleTest> clazz ) {
            return injector.createChildInjector( new SessionModule( factory.build( session.getId() ), clazz ) ).getInstance( DelegateTester.class );
        }
    }

    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public JenkinsTestSuite(
            Provider<TransactionTester> provideTransTester,
            Provider<DbAssetManagerTester> provideDbTester,
            Provider<QuotaUtilTester> provideQuotaTester,
            littleware.asset.test.JenkinsTestSuite assetTestSuite,
            SessionBuilder sessionBuilder
            ) {
        super(JenkinsTestSuite.class.getName());
        boolean runTest = true;

        if (runTest) {
            this.addTest( provideDbTester.get() );
            this.addTest( provideDbTester.get().putName( "testCreateUpdateDelete" ) );
        }
        this.addTest( assetTestSuite );
        if ( runTest ) {
            this.addTest( 
                    sessionBuilder.buildInContext( AssetManagerTester.class )
                    );
        }
        if (runTest) {
            this.addTest( sessionBuilder.buildInContext( AssetSearchManagerTester.class ).putName( "testLoad"));
            this.addTest( sessionBuilder.buildInContext( AssetSearchManagerTester.class ) );
            this.addTest( sessionBuilder.buildInContext( AssetSearchManagerTester.class ).putName( "testTransactionLog"));
        }

        if ( false ) {
            // this test polutes the asset-type table, so only run it when necessary
            this.addTest( provideDbTester.get().putName( "testAssetTypeCheck" ) );
        }

        if (runTest) {
            this.addTest( provideTransTester.get() );
        }
        if ( false ) {
            // this test only applies for JdbcLittleTransaction db implementation
            this.addTest(provideTransTester.get().putName("testSavepoint") );
        }
        if ( runTest ) {
            this.addTest( provideQuotaTester.get() );
        }
        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    public static Test suite() {
        try {
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get(
                    ).addModuleFactory( new MockModuleFactory() ).build();
            return (new TestFactory()).build( serverBoot, JenkinsTestSuite.class );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }

}


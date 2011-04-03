/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.server.test;


import littleware.asset.server.CacheManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import java.util.logging.Logger;
import java.util.logging.Level;
import javax.security.auth.Subject;
import junit.framework.*;
import littleware.asset.AssetManager;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.server.db.test.DbAssetManagerTester;
import littleware.asset.server.internal.RmiAssetManager;
import littleware.asset.server.internal.SimpleAssetManager;
import littleware.asset.server.internal.SimpleAssetSearchManager;
import littleware.asset.test.AssetManagerTester;
import littleware.asset.test.AssetRetrieverTester;
import littleware.asset.test.AssetSearchManagerTester;
import littleware.asset.test.AssetTestFactory;
import littleware.base.stat.SimpleSampler;
import littleware.security.LittleUser;

/**
 * Test suite for littleware.asset package
 */
public class JenkinsTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger(JenkinsTestSuite.class.getName());

    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public JenkinsTestSuite(
            CacheManager m_cache,
            SimpleAssetManager m_asset,
            SimpleAssetSearchManager m_search,
            Provider<CacheManagerTester> provideCacheTester,
            Provider<TransactionTester> provideTransTester,
            Provider<DbAssetManagerTester> provideDbTester,
            Provider<AssetRetrieverTester> provideRetrieverTest,
            Provider<AssetSearchManagerTester> provideSearchTest,
            Provider<LittleUser> provideCaller
            ) {
        super(JenkinsTestSuite.class.getName());
        boolean runTest = true;

        if (runTest) {
            this.addTest( provideDbTester.get() );
            this.addTest( provideDbTester.get().putName( "testCreateUpdateDelete" ) );
        }
        if ( false ) {
            // this test polutes the asset-type table, so only run it when necessar
            this.addTest( provideDbTester.get().putName( "testAssetTypeCheck" ) );
        }

        if (false) { // Disable these test - running with NullCacheManager now ...
            this.addTest( provideCacheTester.get() );
        }
        if (runTest) {
            this.addTest( provideTransTester.get() );
        }
        if ( false ) {
            // this test only applies for JdbcLittleTransaction db implementation
            this.addTest(provideTransTester.get().putName("testSavepoint") );
        }

        //AssetRetriever     m_retriever = new LocalAssetRetriever ( om_dbasset, om_cache, oregistry_special );
        //AssetSearchManager m_search = new SimpleAssetSearchManager ( om_dbasset, om_cache, oregistry_special );
        if (runTest) {
            this.addTest(provideRetrieverTest.get());
            this.addTest(provideRetrieverTest.get().putName("testAssetType") );
        }
        if (runTest) {
            this.addTest( provideSearchTest.get() );
            this.addTest( provideSearchTest.get().putName( "testTransactionLog"));
        }

        if (runTest) {
            try {
                final Subject subject = new Subject();
                subject.getPrincipals().add( provideCaller.get() );
                InvocationHandler handler_asset = new littleware.security.auth.SubjectInvocationHandler<AssetManager>(
                        subject, m_asset, new SimpleSampler()
                        );

                AssetManager m_proxy = (AssetManager) Proxy.newProxyInstance(AssetManager.class.getClassLoader(),
                        new Class[]{AssetManager.class},
                        handler_asset);
                this.addTest(new AssetManagerTester(
                        new RmiAssetManager(m_proxy),
                        m_search, provideCaller ));
            } catch (Exception e) {
                throw new littleware.base.AssertionFailedException("Failed to setup RmiAssetManager, caught: " + e, e);
            }
        }

        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }

    public static Test suite() {
        try {
            final ServerBootstrap serverBoot = ServerBootstrap.provider.get().build();
            return (new AssetTestFactory()).build(serverBoot,
                    JenkinsTestSuite.class
                    );
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }

}


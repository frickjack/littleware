/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.asset.test;

import littleware.asset.server.CacheManager;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.lang.reflect.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.pickle.HumanPicklerProvider;
import littleware.asset.pickle.PickleMaker;
import littleware.asset.pickle.PickleType;
import littleware.asset.server.db.test.*;
import littleware.asset.server.RmiAssetManager;
import littleware.asset.server.SimpleAssetManager;
import littleware.asset.server.SimpleAssetSearchManager;
import littleware.base.stat.*;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger olog_generic = Logger.getLogger(PackageTestSuite.class.getName());

    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public PackageTestSuite(
            CacheManager m_cache,
            SimpleAssetManager m_asset,
            SimpleAssetSearchManager m_search,
            Provider<CacheManagerTester> provideCacheTester,
            Provider<TransactionTester> provideTransTester,
            Provider<DbAssetManagerTester> provideDbTester,
            Provider<AssetRetrieverTester> provideRetrieverTest,
            Provider<AssetPathTester> providePathTester,
            Provider<AssetTreeToolTester> provideTreeTester
            ) {
        super(PackageTestSuite.class.getName());
        boolean b_run = true;

        if ( b_run ) {
            this.addTest( provideTreeTester.get() );
        }
        if (b_run) {
            this.addTest(new PickleTester(new HumanPicklerProvider()));
            this.addTest(new PickleTester(
                    new Provider<PickleMaker<Asset>>() {

                        @Override
                        public PickleMaker<Asset> get() {
                            return PickleType.XML.createPickleMaker();
                        }
                    }));
        }

        if (b_run) {
            this.addTest( provideDbTester.get() );
            this.addTest( provideDbTester.get().putName( "testCreateUpdateDelete" ) );
        }

        if (false) { // Disable these test - running with NullCacheManager now ...
            this.addTest( provideCacheTester.get() );
        }
        if (b_run) {
            this.addTest( provideTransTester.get() );
        }
        if ( false ) {
            // this test only applies for JdbcLittleTransaction db implementation
            this.addTest(provideTransTester.get().putName("testSavepoint") );
        }

        //AssetRetriever     m_retriever = new LocalAssetRetriever ( om_dbasset, om_cache, oregistry_special );
        //AssetSearchManager m_search = new SimpleAssetSearchManager ( om_dbasset, om_cache, oregistry_special );

        if (b_run) {
            this.addTest(new AssetBuilderTester("testBuild"));
        }
        if (b_run) {
            this.addTest(provideRetrieverTest.get());
            this.addTest(provideRetrieverTest.get().putName("testAssetType") );
        }
        if (b_run) {
            this.addTest(new AssetSearchManagerTester("testSearch", m_search));
        }
        if (b_run) {
            this.addTest(providePathTester.get() );
        }

        if (b_run) {
            try {
                InvocationHandler handler_asset = new littleware.security.auth.SubjectInvocationHandler<AssetManager>(
                        null, m_asset, new SimpleSampler());

                AssetManager m_proxy = (AssetManager) Proxy.newProxyInstance(AssetManager.class.getClassLoader(),
                        new Class[]{AssetManager.class},
                        handler_asset);
                this.addTest(new AssetManagerTester(
                        new RmiAssetManager(m_proxy),
                        m_search));
            } catch (Exception e) {
                throw new littleware.base.AssertionFailedException("Failed to setup RmiAssetManager, caught: " + e, e);
            }
        }

        if (b_run) {
            this.addTest(new XmlAssetTester("testXmlAsset"));
        }

        olog_generic.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
}


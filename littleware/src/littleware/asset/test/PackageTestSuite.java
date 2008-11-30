package littleware.asset.test;

import com.google.inject.Inject;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;
import java.lang.reflect.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.server.AssetSpecializerRegistry;
import littleware.asset.server.db.*;
import littleware.asset.server.db.test.*;
import littleware.asset.server.LocalAssetRetriever;
import littleware.asset.server.QuotaUtil;
import littleware.asset.server.SimpleAssetManager;
import littleware.asset.server.RmiAssetManager;
import littleware.asset.server.SimpleAssetSearchManager;
import littleware.base.stat.*;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite implements com.google.inject.Provider<TestSuite> {
	private static final Logger olog_generic = Logger.getLogger ( PackageTestSuite.class.getName() );
	
    private final DbAssetManager            om_dbasset;
    private final CacheManager              om_cache;
    private final AssetSpecializerRegistry  oregistry_special;
    private final QuotaUtil                 oquota_util;
    
    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public PackageTestSuite( DbAssetManager m_dbasset, CacheManager m_cache, AssetSpecializerRegistry registry_special, QuotaUtil quota_util ) {
        om_dbasset = m_dbasset;
        om_cache = m_cache;
        oregistry_special = registry_special;
        oquota_util = quota_util;
    }


	/**
	 * Setup a test suite to exercise this package -
	 */
    public TestSuite get () {
        TestSuite test_suite = new TestSuite ( PackageTestSuite.class.getName() );
				
		boolean         b_run = true;
		
		if ( b_run ) {
			test_suite.addTest ( new DbAssetManagerTester ( "testLoad", om_dbasset ) );
		}
		
		if ( b_run ) {
			AssetRetriever m_test_retriever = new LocalAssetRetriever ( om_dbasset,
																		new NullCacheManager (),
                                                                        oregistry_special
																		);
			test_suite.addTest ( new CacheManagerTester ( "testCache",
														  om_cache,
														  m_test_retriever
														  )
								 );
            
			// Test the non-cacheing AssetRetriever while we're at it
			test_suite.addTest ( new AssetRetrieverTester ( "testLoad", m_test_retriever ) );
		}	
        if ( b_run ) {
            test_suite.addTest ( new TransactionTester ( "testTransactionManager" )	);   
            test_suite.addTest ( new TransactionTester ( "testSavepoint" )	);    
        }
		
		AssetRetriever     m_retriever = new LocalAssetRetriever ( om_dbasset, om_cache, oregistry_special );
		AssetSearchManager m_search = new SimpleAssetSearchManager ( om_dbasset, om_cache, oregistry_special );
        AssetManager       m_asset = new SimpleAssetManager ( om_cache,
                                                           m_search, om_dbasset, oquota_util,
                                                           oregistry_special
                                                           );
        
        if ( b_run ) {
            test_suite.addTest ( new AssetBuilderTester ( "testBuild" ) );
        }
		if ( b_run ) {
			test_suite.addTest ( new AssetRetrieverTester ( "testLoad", m_retriever ) );
			test_suite.addTest ( new AssetRetrieverTester ( "testAssetType", m_retriever ) );            
		}
		if ( b_run ) {
			test_suite.addTest ( new AssetSearchManagerTester ( "testSearch", m_search ) );
		}
		if ( b_run ) {
			test_suite.addTest ( new AssetPathTester ( "testPathTraverse", m_search, m_asset ) );
		}
        
		if ( b_run ) {
			try {
				InvocationHandler handler_asset = new littleware.security.auth.SubjectInvocationHandler<AssetManager> 
					(
					 null, m_asset, olog_generic, new SimpleSampler ()
					 );
																							  
				AssetManager    m_proxy = (AssetManager) Proxy.newProxyInstance ( AssetManager.class.getClassLoader (),
																		  new Class[] { AssetManager.class },
																		  handler_asset
																		  );
				m_asset = new RmiAssetManager ( m_proxy );
				test_suite.addTest ( new AssetManagerTester ( "testAssetCreation", m_asset, m_search ) );
			} catch ( RemoteException e ) {
				throw new littleware.base.AssertionFailedException ( "Failed to setup RmiAssetManager, caught: " + e, e );
			}
		}
		
		if ( b_run ) {
			test_suite.addTest ( new XmlAssetTester ( "testXmlAsset" ) );
		}

		olog_generic.log ( Level.INFO, "PackageTestSuite.suite () returning ok ..." );
        return test_suite;
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


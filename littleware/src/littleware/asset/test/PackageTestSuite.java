package littleware.asset.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;
import javax.sql.DataSource;
import javax.security.auth.Subject;
import java.rmi.RemoteException;
import java.lang.reflect.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.asset.server.db.test.*;
import littleware.asset.server.db.postgres.DbAssetPostgresManager;
import littleware.asset.server.AssetResourceBundle;
import littleware.asset.server.LocalAssetRetriever;
import littleware.asset.server.SimpleCacheManager;
import littleware.asset.server.SimpleAssetManager;
import littleware.asset.server.RmiAssetManager;
import littleware.asset.server.SimpleAssetSearchManager;
import littleware.base.*;
import littleware.base.stat.*;
import littleware.db.SqlResourceBundle;
import littleware.security.AccountManager;
import littleware.security.server.SimpleAccountManager;
import littleware.security.auth.server.db.postgres.PostgresDbAuthManager;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite {
	private static Logger olog_generic = Logger.getLogger ( "littleware.asset.test.PackageTestSuite" );
	
	/**
	 * Setup a test suite to exercise this package -
	 * junit.swingui.TestRunner looks for this.
	 */
    public static Test suite () {
        TestSuite test_suite = new TestSuite ( "littleware.asset.test.PackageTestSuite" );
        final DbAssetManager  m_dbasset;
		
		try {
            AssetResourceBundle bundle_asset = AssetResourceBundle.getBundle ();
            m_dbasset = (DbAssetManager) ((GuardedObject) bundle_asset.getObject ( AssetResourceBundle.Content.AssetDbManager )).getObject ();
		} catch ( MissingResourceException e ) {
			olog_generic.log ( Level.SEVERE, "DataSource resource not yet available, caught: " + e );
			throw e;
		}
		
		boolean         b_run = true;
		
		if ( b_run ) {
			test_suite.addTest ( new DbAssetManagerTester ( "testLoad", m_dbasset ) );
		}
		
		
		CacheManager    m_cache = SimpleCacheManager.getTheManager ();  
		
		if ( b_run ) {
			AssetRetriever m_test_retriever = new LocalAssetRetriever ( m_dbasset,
																		new NullCacheManager ()
																		);
			test_suite.addTest ( new CacheManagerTester ( "testCache",
														  m_cache,
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
		
		AssetRetriever     m_retriever = new LocalAssetRetriever ( m_dbasset, m_cache );
		AssetSearchManager m_search = new SimpleAssetSearchManager ( m_dbasset, m_cache );
        AssetManager       m_asset = new SimpleAssetManager ( m_cache, 
                                                           m_retriever, m_dbasset, null
                                                           );
        AccountManager     m_account = new SimpleAccountManager ( m_asset, m_search,
                                                                  new PostgresDbAuthManager ()
                                                                  );
		((SimpleAssetManager) m_asset).setAccountManager ( m_account );
        
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


/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */



package littleware.asset.test;

import com.google.inject.Inject;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.rmi.RemoteException;
import java.lang.reflect.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.asset.server.db.*;
import littleware.asset.server.db.test.*;
import littleware.asset.server.RmiAssetManager;
import littleware.asset.server.SimpleAssetManager;
import littleware.asset.server.SimpleAssetSearchManager;
import littleware.asset.server.TransactionManager;
import littleware.base.stat.*;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite extends TestSuite {
	private static final Logger olog_generic = Logger.getLogger ( PackageTestSuite.class.getName() );
	
    
    /**
     * Inject dependencies necessary to setup the TestSuite
     */
    @Inject
    public PackageTestSuite( DbAssetManager m_dbasset, CacheManager m_cache,
            SimpleAssetManager m_asset,
            SimpleAssetSearchManager m_search,
            TransactionManager    mgr_trans
            ) {
        super( PackageTestSuite.class.getName() );
		boolean         b_run = true;
		
		if ( b_run ) {
			this.addTest ( new DbAssetManagerTester ( "testLoad", m_dbasset ) );
		}
		
		if ( b_run ) {
			this.addTest ( new CacheManagerTester ( "testCache",
														  m_cache,
														  m_search
														  )
								 );
            
			// Test the non-cacheing AssetRetriever while we're at it
			this.addTest ( new AssetRetrieverTester ( "testLoad", m_search ) );
		}	
        if ( b_run ) {
            this.addTest ( new TransactionTester ( "testTransactionManager", mgr_trans )	);
            this.addTest ( new TransactionTester ( "testSavepoint", mgr_trans )	);
        }
		
		//AssetRetriever     m_retriever = new LocalAssetRetriever ( om_dbasset, om_cache, oregistry_special );
		//AssetSearchManager m_search = new SimpleAssetSearchManager ( om_dbasset, om_cache, oregistry_special );
        
        if ( b_run ) {
            this.addTest ( new AssetBuilderTester ( "testBuild" ) );
        }
		if ( b_run ) {
			this.addTest ( new AssetRetrieverTester ( "testLoad", m_search ) );
			this.addTest ( new AssetRetrieverTester ( "testAssetType", m_search ) );
		}
		if ( b_run ) {
			this.addTest ( new AssetSearchManagerTester ( "testSearch", m_search ) );
		}
		if ( b_run ) {
			this.addTest ( new AssetPathTester ( "testPathTraverse", m_search, m_asset ) );
		}
        
		if ( b_run ) {
			try {
				InvocationHandler handler_asset = new littleware.security.auth.SubjectInvocationHandler<AssetManager> 
					(
					 null, m_asset, new SimpleSampler ()
					 );
																							  
				AssetManager    m_proxy = (AssetManager) Proxy.newProxyInstance ( AssetManager.class.getClassLoader (),
																		  new Class[] { AssetManager.class },
																		  handler_asset
																		  );
				this.addTest ( new AssetManagerTester ( "testAssetCreation", 
                        new RmiAssetManager( m_proxy ), m_search )
                        );
			} catch ( RemoteException e ) {
				throw new littleware.base.AssertionFailedException ( "Failed to setup RmiAssetManager, caught: " + e, e );
			}
		}
		
		if ( b_run ) {
			this.addTest ( new XmlAssetTester ( "testXmlAsset" ) );
		}

		olog_generic.log ( Level.INFO, "PackageTestSuite.suite () returning ok ..." );
    }
}


package littleware.apps.test;

import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import junit.framework.*;

import littleware.apps.filebucket.BucketManager;
import littleware.apps.filebucket.BucketServiceType;
import littleware.apps.swingclient.*;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.base.AssertionFailedException;
import littleware.base.BaseException;
import littleware.security.auth.*;
import littleware.security.AccountManager;
import littleware.security.LittleUser;
import littleware.security.SecurityAssetType;

/**
 * Test suite for littleware.asset package
 */
public class PackageTestSuite extends TestSuite {
	private static Logger            olog_generic = Logger.getLogger ( "littleware.apps.test.PackageTestSuite" );
    private static SessionHelper     om_helper = null;

    /**
     * Stash a test login session, so each test does not
     * have to setup a separate session.
     * Tests can access this in setUp routine.
     */
    static SessionHelper getTestSessionHelper () {
        if ( null == om_helper ) {
            try {
                SessionManager m_session = SessionUtil.getSessionManager ( SessionUtil.getRegistryHost (),
                                                                           SessionUtil.getRegistryPort ()
                                                                           );			
                String s_test_user = "littleware.test_user";
                String s_test_password = "test123";
                
                om_helper = m_session.login ( s_test_user, s_test_password, 
                                               "setup test session for SwingClientTester" 
                                               );
            } catch ( RuntimeException e ) {
                throw e;
            } catch ( Exception e ) {
                throw new AssertionFailedException ( "Failure setting up test session, caught: " + e, e );
            }
        }
        return om_helper;
    }
    
    /**
     * Setup a test suite to exercise this package -
	 * junit.swingui.TestRunner looks for this.
	 */
    public static Test suite () {
        return new PackageTestSuite ();
    }
    
    /**
     * Self-register all the JUnit tests to do a littleware.apps
     * regression test.
     */
    public PackageTestSuite () {
        super ( "littleware.apps.test.PackageTestSuite" );
    
        final AssetModelLibrary  lib_asset = new SimpleAssetModelLibrary ();
        final IconLibrary        lib_icon;
        try {
            lib_icon = new WebIconLibrary ( "localhost/littleware/lib/icons" );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new AssertionFailedException ( "Failed to initialize IconLibrary", e );
        }
        final SessionHelper      m_helper = PackageTestSuite.getTestSessionHelper ();
        final AssetManager       m_asset;
        final AssetSearchManager m_search;
        final LittleUser         user_test;
        try {
            m_asset = m_helper.getService ( ServiceType.ASSET_MANAGER );
            m_search = m_helper.getService ( ServiceType.ASSET_SEARCH );            
            
            user_test = (LittleUser) m_search.getByName ( "littleware.test_user",
                                                                  SecurityAssetType.USER
                                                                 );
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new AssertionFailedException ( "Failed to intialize services", e );
        }
        final AssetViewFactory         factory_view = new SimpleAssetViewFactory ( m_search, lib_icon );
        final AssetEditorFactory       factory_edit = new EditorAssetViewFactory ( m_asset, m_search, lib_icon, factory_view );
		boolean         b_run = true;

		if ( b_run ) {
			this.addTest ( new AddressBookTester ( "testAddressBook", m_asset, m_search, user_test ) );
		}
		if ( b_run ) {
			this.addTest ( new SwingClientTester ( "testJSessionManager", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit
                                                         ) );
			this.addTest ( new SwingClientTester ( "testJSessionHelper", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit                                                         
                                                         ) );
        }
        if ( b_run ) {
            this.addTest ( new SwingClientTester ( "testJAssetViews", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit                                                         
                                                         ) );
        }
        if ( b_run ) {
            this.addTest ( new SwingClientTester ( "testJAssetBrowser", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit                                                         
                                                         ) 
                                 );
        } 
        if ( b_run ) {
            this.addTest ( new SwingClientTester ( "testGroupFolderTool", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit                                                         
                                                         ) 
                                 );
        }
        if ( b_run ) {
            this.addTest ( new SwingClientTester ( "testAssetModelLibrary", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit                                                         
                                                         ) 
                                 );
        }         
        if ( b_run ) {
            this.addTest ( new SwingClientTester ( "testJEditor", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit                                                         
                                                         ) );
        }
        if ( b_run ) {
            this.addTest ( new SwingClientTester ( "testWizardCreate", m_helper, lib_asset, lib_icon,
                                                         factory_view, factory_edit                                                         
                                                         ) );
        }
        
        try {
            BucketManager m_bucket = m_helper.getService( BucketServiceType.BUCKET_MANAGER );
            // new littleware.apps.filebucket.server.SimpleBucketManager( m_search, m_asset );

            if ( b_run ) {
                this.addTest ( new BucketTester ( "testBucket", 
                                                        m_asset, m_search, m_bucket
                                                        )
                                     );
            }
            if ( b_run ) {
                this.addTest ( new TrackerTester ( "testTracker",
                        m_helper, lib_icon
                                                         )
                                     );
            }
            if ( b_run ) {
                this.addTest ( new TrackerTester ( "testTrackerSwing",
                        m_helper, lib_icon
                                                         )
                                     );
            }
        } catch ( RuntimeException e ) {
            throw e;
        } catch ( Exception e ) {
            throw new AssertionFailedException ( "Failed to get started" );
        }
        
        olog_generic.log ( Level.INFO, "PackageTestSuite() returning ok ..." );
    }
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


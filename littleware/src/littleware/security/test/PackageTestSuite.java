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

package littleware.security.test;

import littleware.security.server.AclManager;
import com.google.inject.Inject;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.*;

import junit.framework.*;

import littleware.asset.*;
import littleware.base.AssertionFailedException;
import littleware.security.*;
import littleware.security.auth.*;



/**
 * Just little utility class that packages up a test suite
 * for the littleware.security package.
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger   olog = Logger.getLogger ( PackageTestSuite.class.getName() );

    private final AssetManager       om_asset;
	private final AssetSearchManager om_search;
    private final SessionManager     om_session;
    private final AccountManager     om_account;
    private final AclManager         om_acl;

    /**
     * Inject the managers to test against
     *
     * @param m_session
     * @param m_search
     * @param m_account
     * @param m_acl
     */
    @Inject
    public PackageTestSuite( SessionManager m_session,
            AssetSearchManager m_search,
            AssetManager m_asset,
            AccountManager m_account,
            AclManager m_acl
            )
    {
        super( PackageTestSuite.class.getName() );

        om_asset = m_asset;
        om_session = m_session;
        om_search = m_search;
        om_account = m_account;
        om_acl = m_acl;
        
		olog.log ( Level.INFO, "Trying to setup littleware.security test suite" );
		
		olog.log ( Level.INFO, "Registering littleware SimpleDbLoginConfiguration" );
		boolean        b_run = false;

		if ( b_run ) {
			try {
				Principal p_administrator = om_account.getPrincipal ( AccountManager.LITTLEWARE_ADMIN );

				this.addTest ( new AclTester ( "testAcl", new SimpleAccessList (), p_administrator, om_search ) );
				this.addTest ( new AclTester ( "testAclOwner", new SimpleAccessList (), p_administrator, om_search ) );
			} catch ( Exception e ) {
				throw new AssertionFailedException ( "Caught unexpected during test initialization: " + e, e );
			}
		}			
		
		if ( b_run ) {
			this.addTest ( new AccountManagerTester ( "testGetPrincipals", om_account, om_asset ) );
			this.addTest ( new AccountManagerTester ( "testQuota", om_account, om_asset ) );
			this.addTest ( new AccountManagerTester ( "testPasswordUpdate", om_account, om_asset ) );
			this.addTest ( new AccountManagerTester ( "testGroupUpdate", om_account, om_asset ) );
		}
		if ( b_run ) {
			this.addTest ( new AclManagerTester ( "testAclLoad", om_acl, om_account, om_asset ) );
			this.addTest ( new AclManagerTester ( "testAclUpdate", om_acl, om_account, om_asset ) );
		}
		
		olog.log ( Level.INFO, "PackageTestSuite.suite () returning ok ..." );
    }
}



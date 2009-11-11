/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;


import junit.framework.*;

import littleware.asset.*;
import littleware.security.*;
import littleware.security.auth.*;

/**
 * Just little utility class that packages up a test suite
 * for the littleware.security package.
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger olog = Logger.getLogger(PackageTestSuite.class.getName());

    /**
     * Inject the managers to test against
     *
     * @param m_session
     * @param m_search
     * @param m_account
     * @param m_acl
     */
    @Inject
    public PackageTestSuite(SessionManager m_session,
            AssetSearchManager search,
            AssetManager m_asset,
            AccountManager m_account,
            Provider<AclManagerTester> provideAclTester,
            Provider<AccountManagerTester> provideAccountTester) {
        super(PackageTestSuite.class.getName());

        olog.log(Level.INFO, "Trying to setup littleware.security test suite");
        olog.log(Level.INFO, "Registering littleware SimpleDbLoginConfiguration");
        boolean b_run = true;

        if (b_run) {
            this.addTest(provideAclTester.get().putName("testAcl"));
        }

        if (b_run) {
            this.addTest(provideAccountTester.get().putName("testGetPrincipals"));
            this.addTest(provideAccountTester.get().putName("testQuota"));
            //this.addTest(provideAccountTester.get().putName("testPasswordUpdate"));
            this.addTest(provideAccountTester.get().putName("testGroupUpdate"));
        }
        if (b_run) {
            this.addTest(provideAclTester.get().putName("testAclLoad"));
            //this.addTest(provideAclTester.get().putName("testAclUpdate"));
        }

        olog.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
}



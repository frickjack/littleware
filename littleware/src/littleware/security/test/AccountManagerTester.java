/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.test;

import com.google.inject.Inject;
import java.util.*;
import java.security.*;
import javax.security.auth.login.*;

import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.asset.AssetType;
import littleware.security.*;
import littleware.security.auth.*;
import littleware.base.BaseException;
import littleware.test.LittleTest;
import littleware.test.ServerTestLauncher;

/**
 * TestFixture runs SecurityMnaager implementations
 * through their paces.
 */
public class AccountManagerTester extends LittleTest {

    private static final Logger olog_generic = Logger.getLogger(AccountManagerTester.class.getName());
    private final AssetSearchManager osearch;
    private final AccountManager om_account;
    private final AssetManager om_asset;

    /**
     * Little utility that invokes a PriviledgedAction to
     * access the specified GuardedObject in the specified
     * resource bundle
     *
     * @param s_resource_bundle to lookup
     * @param s_resource that corresponds to a GuardedObject to access
     * @return whatever the GuardedObject is guarding - invoke Guard
     *                within a local PriviledgedAction
     */
    private static Object getGuardedResource(String s_resource_bundle, String s_resource) {
        return AccessController.doPrivileged(new GetGuardedResourceAction(s_resource_bundle, s_resource));
    }

    /**
     * Constructor registers the AccountManager to test against.
     * Use littleware.security.test.LoginTester as LoginContext application name.
     *
     * @param s_name of test case to run
     * @param m_account to run test against
     * @param m_asset to do saveAsset/deleteAsset/... calls against
     */
    @Inject
    public AccountManagerTester(String s_name,
            AccountManager m_account,
            AssetManager m_asset,
            AssetSearchManager search) {
        om_account = m_account;
        om_asset = m_asset;
        osearch = search;
        setName("testGetPrincipals");
    }

    /**
     * Just retrieve some Principals - USER and GROUP
     */
    public void testGetPrincipals() {
        try {
            final LittleUser userAdmin = osearch.getByName(AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER).get();
            final LittleGroup groupAdmin = osearch.getByName(
                    AccountManager.LITTLEWARE_ADMIN_GROUP,
                    SecurityAssetType.GROUP
                    ).get();

            for (Enumeration<? extends Principal> enum_x = groupAdmin.members();
                    enum_x.hasMoreElements();) {
                LittlePrincipal p_member = (LittlePrincipal) enum_x.nextElement();
                olog_generic.log(Level.INFO, "Got admin group member: " + p_member.getName() +
                        " (" + p_member.getObjectId() + ")");
            }
            assertTrue("administrator should be member of admin group",
                    groupAdmin.isMember(userAdmin));
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught unexpected: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught unexpected exception: " + e + ", " +
                    BaseException.getStackTrace(e), false);
        }
    }

    /**
     * Test interaction between AccountManager.updateUser
     * and LoginManager login.
     */
    public void testPasswordUpdate() {
        try {
            LittleUser p_user = om_account.getAuthenticatedUser();
            assertTrue("Test running as " + ServerTestLauncher.OS_TEST_USER,
                    ServerTestLauncher.OS_TEST_USER.equals(ServerTestLauncher.OS_TEST_USER));
            olog_generic.log(Level.INFO, "Changing password for " + ServerTestLauncher.OS_TEST_USER);

            try {
                String s_password = "whatever";
                om_account.updateUser(p_user, s_password, "change password");
                Principal p_login = LoginTester.runLoginTest(ServerTestLauncher.OS_TEST_USER, s_password, this);
                assertTrue("Login ok", p_login.getName().equals(ServerTestLauncher.OS_TEST_USER));
                try {
                    LoginContext x_login = new LoginContext("littleware.security.simplelogin",
                            new SimpleNamePasswordCallbackHandler(p_user.getName(), "bogus"));
                    x_login.login();
                    assertTrue("Should have failed login with bogus password", false);
                } catch (LoginException e) {
                    olog_generic.log(Level.INFO, "Password check ok");
                }
            } finally {
                om_account.updateUser(p_user, ServerTestLauncher.OS_TEST_USER_PASSWORD, "restore password");
            }
            final Principal x_user = osearch.getByName(ServerTestLauncher.OS_TEST_USER, SecurityAssetType.PRINCIPAL ).get();
            final Principal x_tmp = osearch.getByName("group." + ServerTestLauncher.OS_TEST_USER, SecurityAssetType.PRINCIPAL ).get();

        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught unexpected: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught unexpected exception: " + e + ", " +
                    BaseException.getStackTrace(e), false);
        }
    }

    /**
     * Just verify that incrementQuota increments the quota asset.
     * Must be running the test as a user with an active Quota set.
     */
    public void testQuota() {
        try {
            LittleUser p_me = om_account.getAuthenticatedUser();
            Quota a_quota_before = om_account.getQuota(p_me);
            assertTrue("Got a quota we can test against",
                    (null != a_quota_before) && (a_quota_before.getQuotaLimit() > 0) && (a_quota_before.getQuotaCount() >= 0));
            om_account.incrementQuotaCount();
            Quota a_quota_after = om_account.getQuota(p_me);
            assertTrue("Quota incremented by 1: " + a_quota_before.getQuotaCount() +
                    " -> " + a_quota_after.getQuotaCount(),
                    a_quota_before.getQuotaCount() + 1 == a_quota_after.getQuotaCount());
            // Verify get/setData parsing
            String s_data = a_quota_after.getData();
            a_quota_after.setData(a_quota_after.getData());
            assertTrue("get/setData consistency", s_data.equals(a_quota_after.getData()));
        } catch (Exception e) {
            olog_generic.log(Level.WARNING, "Caught unexpected: " + e +
                    ", " + BaseException.getStackTrace(e));
            assertTrue("Caught unexpected exception: " + e + ", " +
                    BaseException.getStackTrace(e), false);
        }
    }

    /**
     * Test group update
     */
    public void testGroupUpdate() {
        try {
            final String name = "group.littleware.test_user";
            final LittleUser caller = om_account.getAuthenticatedUser();
            LittleGroup groupTest = osearch.getByName(name, SecurityAssetType.GROUP ).getOr(null);
            if ( null == groupTest ) {
                groupTest = AssetType.createSubfolder( SecurityAssetType.GROUP, name, getTestHome(osearch) );
                groupTest.addMember(caller);
                groupTest = om_asset.saveAsset(groupTest, "setup test group" );
            }
            

            if (groupTest.removeMember(caller)) {
                groupTest = om_asset.saveAsset(groupTest, "Removed tester " + caller.getName());
            }
            groupTest = osearch.getByName(name, SecurityAssetType.GROUP ).get();
            assertTrue("Already removed caller as primary member of group",
                    !groupTest.removeMember(caller));
            assertTrue("Added caller to test group: " + caller.getName(),
                    groupTest.addMember(caller));
            groupTest = om_asset.saveAsset(groupTest, "Added tester " + caller.getName());

            groupTest = osearch.getByName(name, SecurityAssetType.GROUP ).get();
            assertTrue("Able to remove caller " + caller.getName() + " from test group",
                    groupTest.removeMember(caller));
            groupTest = om_asset.saveAsset(groupTest, "Removed tester " + caller.getName());

            groupTest = osearch.getByName(name, SecurityAssetType.GROUP ).get();
            assertTrue("Already removed caller as primary member of group 2nd time",
                    !groupTest.removeMember(caller));
        } catch (Exception e) {
            olog_generic.log(Level.INFO, "Caught: " + e + ", " + BaseException.getStackTrace(e));
            assertTrue("Should not have caught: " + e, false);
        }
    }
}



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


import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.security.*;
import littleware.base.BaseException;
import littleware.test.LittleTest;

/**
 * TestFixture runs SecurityMnaager implementations
 * through their paces.
 */
public class AccountManagerTester extends LittleTest {

    private static final Logger log = Logger.getLogger(AccountManagerTester.class.getName());
    private final AssetSearchManager search;
    private final AccountManager accountMgr;
    private final AssetManager assetMgr;
    private final LittleUser caller;

    /**
     * Constructor registers the AccountManager to test against.
     * Use littleware.security.test.LoginTester as LoginContext application name.
     *
     * @param s_name of test case to run
     * @param m_account to run test against
     * @param m_asset to do saveAsset/deleteAsset/... calls against
     */
    @Inject
    public AccountManagerTester(
            AccountManager m_account,
            AssetManager m_asset,
            AssetSearchManager search,
            LittleUser caller ) {
        this.accountMgr = m_account;
        this.assetMgr = m_asset;
        this.search = search;
        this.caller = caller;
        setName("testGetPrincipals");
    }

    /**
     * Just retrieve some Principals - USER and GROUP
     */
    public void testGetPrincipals() {
        try {
            final LittleUser userAdmin = search.getByName(AccountManager.LITTLEWARE_ADMIN, SecurityAssetType.USER).get().narrow();
            final LittleGroup groupAdmin = search.getByName(
                    AccountManager.LITTLEWARE_ADMIN_GROUP,
                    SecurityAssetType.GROUP).get().narrow();

            for (LittlePrincipal member : groupAdmin.getMembers() ) {
                log.log(Level.INFO, "Got admin group member: " + member.getName() +
                        " (" + member.getId() + ")");
            }
            assertTrue("administrator should be member of admin group",
                    groupAdmin.isMember(userAdmin));
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught unexpected: " + e +
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
            Quota a_quota_before = accountMgr.getQuota(caller);
            assertTrue("Got a quota we can test against",
                    (null != a_quota_before) && (a_quota_before.getQuotaLimit() > 0) && (a_quota_before.getQuotaCount() >= 0));
            accountMgr.incrementQuotaCount();
            Quota a_quota_after = accountMgr.getQuota(caller);
            assertTrue("Quota incremented by 1: " + a_quota_before.getQuotaCount() +
                    " -> " + a_quota_after.getQuotaCount(),
                    a_quota_before.getQuotaCount() + 1 == a_quota_after.getQuotaCount());
            // Verify get/setData parsing
            assertTrue("get/setData consistency",
                    a_quota_after.getData().equals(a_quota_after.copy().data(a_quota_after.getData()).build().getData()));
        } catch (Exception e) {
            log.log(Level.WARNING, "Caught unexpected: " + e +
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
            LittleGroup groupTest = (LittleGroup) search.getByName(name, SecurityAssetType.GROUP).getOr(null);
            if (null == groupTest) {
                groupTest = assetMgr.saveAsset(
                        SecurityAssetType.GROUP.create().add(caller).name(name).parent(getTestHome(search)).
                        build(), "setup test group").narrow();
                assertTrue( caller.getName() + " is member of new group " + groupTest.getName(),
                        groupTest.isMember(caller)
                        );
            }
            if ( ! groupTest.isMember( caller ) ) {
                LittleGroup copy = groupTest.copy().add(caller).build();
                copy = copy.getAssetType().create().copy( copy ).build().narrow();
                assertTrue( caller.getName() + " added to " + copy.getName(),
                    copy.isMember( caller )
                    );
                final Set<Principal> memberSet = new HashSet<Principal>( copy.getMembers() );
                assertTrue( caller.getName() + " in members copy of " + copy.getName(),
                        memberSet.contains(caller)
                        );
                groupTest = assetMgr.saveAsset(copy, "Add tester " + caller.getName());
            }
            assertTrue( caller.getName() + " is member of " + groupTest.getName(),
                    groupTest.isMember( caller )
                    );
            groupTest = assetMgr.saveAsset(groupTest.copy().remove(caller).build(), "Removed tester " + caller.getName());
            groupTest = search.getByName(name, SecurityAssetType.GROUP).get().narrow();
            assertTrue( caller.getName() + " is not a member of " + groupTest.getName(),
                    !groupTest.isMember(caller)
                    );
        } catch (Exception e) {
            log.log(Level.INFO, "Caught: " + e + ", " + BaseException.getStackTrace(e));
            assertTrue("Should not have caught: " + e, false);
        }
    }
}



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
import java.util.logging.Logger;
import java.security.acl.*;


import java.util.logging.Level;
import littleware.asset.AssetManager;
import littleware.asset.AssetSearchManager;
import littleware.security.*;
import littleware.base.*;
import littleware.test.LittleTest;

/**
 * Run ACL implementations through basic test.
 */
public class AclManagerTester extends LittleTest {

    private static final Logger log = Logger.getLogger(AclManagerTester.class.getName());
    private final AssetSearchManager search;
    private final AssetManager assetMgr;

    /**
     * Constructor sets testcase name, and stashes manager to test against.
     *
     * @param m_acl to test
     * @param m_account to pull test_user test-user from
     * @param m_asset to do save/delete tests with
     */
    @Inject
    public AclManagerTester(
            AssetSearchManager search,
            AssetManager m_asset) {
        this.search = search;
        assetMgr = m_asset;
        setName("testAclLoad");
    }

    /**
     * Just try to load an ACL
     */
    public void testAclLoad() {
        try {
            final LittleAcl aclEverybody = search.getByName(LittleAcl.ACL_EVERYBODY_READ, SecurityAssetType.ACL).get().narrow();
        } catch (Exception e) {
            log.severe("Caught unexpected: " +
                    e + ", " + BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        }
    }

    /**
     * Just run the ACL through some simple scenarios
     */
    public void testAcl() {
        log.log(Level.INFO, "testAcl starting with empty test ACL ok");

        try {
            { // Make sure our test permission is working
                final BogusAclPermission x_perm = new BogusAclPermission(5);
                assertTrue("Test permission type recognizes equality between instances",
                        x_perm.equals(new BogusAclPermission(5)));

                final LittleUser p_test =  SecurityAssetType.USER.create().name("10").build().narrow();
                assertTrue("SimplePrincipal recognizes equality between instances",
                        p_test.equals(SecurityAssetType.USER.create().id(p_test.getId()).name("10").build()));
            }

            // Generate AclEntries for a bunch of principal's
            final LittleAcl.Builder aclBuilder = (LittleAcl.Builder) SecurityAssetType.ACL.create().name( "acl.bogus" ).parent( getTestHome( search ) );
            for (int i = 10; i < 20; ++i) {
                final LittlePrincipal testUser = (LittlePrincipal) SecurityAssetType.USER.create().name(Integer.toString(i)).build();

                log.log(Level.INFO, "Registering and verifying AclEntry for principal: " + testUser);
                final LittleAclEntry.Builder entryBuilder = (LittleAclEntry.Builder) SecurityAssetType.ACL_ENTRY.create().principal( testUser ).
                        fromId( aclBuilder.getId() ).homeId( aclBuilder.getId() );

                // Assign a couple permissions to the entry
                for (int j = 0; j < 10; ++j) {
                    entryBuilder.addPermission(new BogusAclPermission(j));
                }
                final LittleAclEntry entry = entryBuilder.build();
                assertTrue("AclEntry is not negative", false == entry.isNegative());
                assertTrue("Registered AclEntry principal equals retrieved principal",
                        entry.getPrincipal().equals(testUser)
                        );

                aclBuilder.addEntry(entry);
            }

            final LittleAcl acl = aclBuilder.build();
            // Ok, let's test a few ops
            for (int i = 10; i < 20; ++i) {
                final LittleUser p_test = SecurityAssetType.USER.create().name(Integer.toString(i)).build().narrow();
                // Assign a couple permissions to the entry
                for (int j = 0; j < 5; ++j) {
                    java.security.acl.Permission x_permission = new BogusAclPermission(j);
                    assertTrue("Principal " + p_test + " does not have permission " + x_permission,
                            !acl.checkPermission(p_test, x_permission));
                }
                // Remove few permissions
                for (int j = 5; j < 8; ++j) {
                    java.security.acl.Permission x_permission = new BogusAclPermission(j);
                    assertTrue("Principal " + p_test + " does not have permission " + x_permission,
                            !acl.checkPermission(p_test, x_permission));
                }
            }
            {
                // Make sure setData/getData are valid
                final LittleAclEntry entry = SecurityAssetType.ACL_ENTRY.create().addPermission(LittlePermission.READ).parent( acl ).build().narrow();
                assertTrue("get/setData consistent", entry.getData().equals( entry.copy().data( entry.getData()).build().getData() ));
            }
        } catch (Exception e) {
            log.log(Level.INFO, "Failed test", e );
            fail("Should not have caught: " + e);
        }
    }

    /** Stupid little Acl permission for use in test cases */
    public static class BogusAclPermission extends LittlePermission {

        private int oi_id;

        /** Constructor just gives integer id to this guy */
        public BogusAclPermission(int i_id) {
            oi_id = i_id;
        }

        /** Equal if same class and same id */
        @Override
        public boolean equals(Object x_other) {
            return ((null != x_other) && (x_other instanceof AclManagerTester.BogusAclPermission) && (oi_id == ((BogusAclPermission) x_other).oi_id));
        }

        /** Just return id number */
        @Override
        public String toString() {
            return "BogusAclPermission id: " + oi_id;
        }

        /** Return id as hash-code */
        @Override
        public int hashCode() {
            return oi_id;
        }
    }

}


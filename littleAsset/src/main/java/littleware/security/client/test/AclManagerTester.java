/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.client.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;


import java.util.logging.Level;
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.security.*;
import littleware.security.LittleUser.Builder;

/**
 * Run ACL implementations through basic test.
 */
public class AclManagerTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(AclManagerTester.class.getName());
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final Provider<Builder> userProvider;
    private final Provider<LittleAclEntry.Builder> aclEntryProvider;
    private final Provider<LittleAcl.Builder> aclProvider;

    /**
     * Constructor sets testcase name, and stashes manager to test against.
     */
    @Inject
    public AclManagerTester(
            AssetSearchManager search,
            AssetManager assetMgr,
            Provider<LittleUser.Builder> userProvider,
            Provider<LittleAcl.Builder> aclProvider,
            Provider<LittleAclEntry.Builder> aclEntryProvider
            ) {
        this.search = search;
        this.assetMgr = assetMgr;
        this.userProvider = userProvider;
        this.aclProvider = aclProvider;
        this.aclEntryProvider = aclEntryProvider;
        setName("testAclLoad");
    }

    /**
     * Just try to load an ACL
     */
    public void testAclLoad() {
        try {
            final LittleAcl aclEverybody = search.getAsset(LittleAcl.UUID_EVERYBODY_READ ).get().narrow();
        } catch (Exception ex) {
            log.log( Level.SEVERE, "Failed test", ex );
            assertTrue("Caught unexpected: " + ex, false);
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

                final LittleUser testUser =  userProvider.get().name("10").build().narrow();
                assertTrue("SimplePrincipal recognizes equality between instances",
                        testUser.equals(userProvider.get().id(testUser.getId()).name("10").build()));
            }

            // Generate AclEntries for a bunch of principal's
            final LittleAcl.Builder aclBuilder = aclProvider.get().name( "acl.bogus" ).parent( getTestHome( search ) );
            for (int i = 10; i < 20; ++i) {
                final LittlePrincipal testUser = userProvider.get().name(Integer.toString(i)).build();

                log.log(Level.INFO, "Registering and verifying AclEntry for principal: " + testUser);
                final LittleAclEntry.Builder entryBuilder = aclEntryProvider.get().principal( testUser ).
                        aclId( aclBuilder.getId() ).homeId( aclBuilder.getId() );

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
                final LittleUser p_test = userProvider.get().name(Integer.toString(i)).build().narrow();
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
                final LittleAclEntry entry = aclEntryProvider.get().addPermission(LittlePermission.READ).acl( acl ).build().narrow();
                assertTrue("get/setData consistent", 
                        entry.getPermissions().equals( entry.copy().narrow( LittleAclEntry.Builder.class ).build().getPermissions() )
                        );
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


/*
 * Copyright 2007-2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.inject.Inject;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.security.Principal;


import junit.framework.*;

import littleware.asset.*;
import littleware.base.*;
import littleware.security.LittlePrincipal;
import littleware.security.AccountManager;
import littleware.security.LittleGroup;
import littleware.security.SecurityAssetType;
import littleware.test.LittleTest;

/**
 * Tester for implementations of the AssetSearchManager interface.
 * AssetPathTester also tests AssetSearchManager.
 */
public class AssetSearchManagerTester extends LittleTest {

    private static final Logger log = Logger.getLogger(AssetSearchManagerTester.class.getName());
    private final AssetSearchManager search;
    private final AssetManager assetMan;

    /**
     * Inject dependencies
     */
    @Inject
    public AssetSearchManagerTester( AssetSearchManager m_search,
            AssetManager assetMan ) {
        search = m_search;
        this.assetMan = assetMan;
        setName( "testSearch" );
    }

    /**
     * Just load some test assets.
     */
    public void testSearch() {
        try {
            Asset a_lookup = search.getByName(AssetManagerTester.MS_TEST_HOME,
                    AssetType.HOME).getOr(null);
            assertTrue("Got some home-by-name data", null != a_lookup);

            assertTrue("Searcher did not freak out on empty search",
                    !search.getByName("frickityFrickjackFroo", SecurityAssetType.USER).isSet());
            assertTrue("Child search did not freak on empty search",
                    (!search.getAssetFrom(a_lookup.getObjectId(), "UgidyUgaUga").isSet()) && (!search.getAssetFrom(UUID.randomUUID(), "whatever").isSet()));

            final LittleGroup group_everybody = search.getByName(AccountManager.LITTLEWARE_EVERYBODY_GROUP,
                    SecurityAssetType.GROUP).get();
            for (Enumeration<? extends Principal> enum_members = group_everybody.members();
                    enum_members.hasMoreElements();) {
                LittlePrincipal p_member = (LittlePrincipal) enum_members.nextElement();
                Set<UUID> v_links = search.getAssetIdsTo(p_member.getObjectId(),
                        SecurityAssetType.GROUP_MEMBER);
                assertTrue("Group member as links TO it: " + p_member,
                        !v_links.isEmpty());
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Test failed", e);
            fail("Caught: " + e);
        }
    }

    /**
     * Update a well known asset, then make sure
     * it shows up in the transaction log
     */
    public void testTransactionLog() {
        try {
          final Asset home = getTestHome( search );
          final Asset aTest;
          { // update the test asset
              final String name = "testTransactionLog";
              final Maybe<Asset> maybeTest = search.getAssetFrom( home.getObjectId(), name);
              if ( maybeTest.isSet() ) {
                  aTest = assetMan.saveAsset( maybeTest.get(), "force an update for testing" );
              } else {
                  aTest = assetMan.saveAsset( AssetType.createSubfolder(AssetType.GENERIC, name, home ),
                               "setup test asset");
              }
          }
          final List<IdWithClock> data = search.checkTransactionLog( home.getObjectId(), 0 );
          assertTrue( "Some transaction in the log", ! data.isEmpty() );
          assertTrue( "Data not too old", data.get(0).getTransaction() > aTest.getTransactionCount() - 1000 );
          long lastTransaction = 0;
          boolean bFoundTest = false;
          final Set<UUID>  idSet = new HashSet<UUID>();
          for( IdWithClock scan : data ) {
              assertTrue( "no duplicate ids in data", ! idSet.contains( scan.getId() ) );
              assertTrue( "data is in transaction order",
                      scan.getTransaction() >= lastTransaction );
              lastTransaction = scan.getTransaction ();
              if ( scan.getId().equals( aTest.getObjectId() ) ) {
                  bFoundTest = true;
                  assertTrue( "Test transaction count matches expected value " + scan.getTransaction () +
                          " == " + aTest.getTransactionCount(),
                          scan.getTransaction() == aTest.getTransactionCount()
                          );
              }
          }
          assertTrue( "Log scan includes test asset", bFoundTest );
        } catch (Exception e) {
            log.log(Level.WARNING, "Test failed", e);
            fail("Caught: " + e);
        }
    }
}


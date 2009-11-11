/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.db.*;
import littleware.db.*;
import littleware.base.*;
import littleware.test.LittleTest;

/**
 * Tests for DbAssetManager implementations.
 * Gets registered with the littleware.asset.test.PackageTestSuite
 */
public class DbAssetManagerTester extends LittleTest {

    private static final Logger olog = Logger.getLogger(DbAssetManagerTester.class.getName());
    private static final UUID ouTestHome = UUIDFactory.parseUUID("D589EABED8EA43C1890DBF3CF1F9689A");
    private static final UUID ouTestCreate = UUIDFactory.parseUUID("0443fe54-53c6-4d80-ba88-d09c7d96d809");
    private final DbAssetManager omgrDb;
    private final Provider<LittleTransaction> oprovideTrans;

    /**
     * Constructor stashes data to run tests against
     *
     * @param m_db manager to test against
     */
    @Inject
    public DbAssetManagerTester(DbAssetManager mgrDb,
            Provider<LittleTransaction> provideTrans) {
        setName("testLoad");
        omgrDb = mgrDb;
        oprovideTrans = provideTrans;
    }

    /**
     * Test the load of a well-known test asset
     */
    public void testLoad() {
        final LittleTransaction trans = oprovideTrans.get();
        trans.startDbAccess();
        try {
            DbReader<Asset, UUID> db_reader = omgrDb.makeDbAssetLoader();
            Asset a_result = db_reader.loadObject(ouTestHome);
            assertTrue("Asset is of proper type",
                    a_result.getAssetType().equals(AssetType.HOME));
            assertTrue("Asset has right id", a_result.getId().equals(ouTestHome));
            // Verify that looking up a non-existent thing does not throw an exceptioon
            final UUID uTest = UUIDFactory.getFactory().create();
            a_result = db_reader.loadObject(uTest);
            assertTrue("Got null result for nonexistent UUID", null == a_result);
        } catch (Exception e) {
            olog.log(Level.INFO, "Caught unexpected", e);
            fail("Caught unexpected: " + e);
        } finally {
            trans.endDbAccess();
        }
    }

    /**
     * Test the load of a well-known test asset
     */
    public void testCreateUpdateDelete() {
        final LittleTransaction trans = oprovideTrans.get();
        try {
            trans.startDbUpdate();
        } catch (Exception ex) {
            olog.log(Level.WARNING, "Failed transaction setup", ex);
            fail("Failed to setup transaction: " + ex);
        }
        boolean bRollback = false;
        try {
            final DbReader<Asset, UUID> dbReader = omgrDb.makeDbAssetLoader();
            final Asset aHome = dbReader.loadObject(ouTestHome);
            Asset aTest = dbReader.loadObject(ouTestCreate);
            if (null != aTest) {
                final DbWriter<Asset> dbDelete = omgrDb.makeDbAssetDeleter();
                dbDelete.saveObject(aTest);
            }
            final AssetBuilder assetBuilder = AssetType.GENERIC.create().name( "DbAssetTester" ).parent( aHome).
                    id(ouTestCreate).
                    parent(aHome).
                    comment("Just a test").
                    lastUpdate("Testing asset setup").
                    creatorId( aHome.getCreatorId() ).
                    lastUpdaterId( aHome.getCreatorId() ).
                    ownerId( aHome.getOwnerId() ).transaction(1);
            final DbWriter<Asset> dbSaver = omgrDb.makeDbAssetSaver();
            final Asset testSave = assetBuilder.build();
            final long lNewTransaction = aTest.getTransaction();
            dbSaver.saveObject(aTest);
            aTest = omgrDb.makeDbAssetLoader().loadObject(testSave.getId());
            assertTrue("From preserved on load", testSave.getFromId().equals(aHome.getId()));
            // Test from-loader
            final Map<String, UUID> mapChildren = omgrDb.makeDbAssetIdsFromLoader(ouTestHome, Maybe.something((AssetType) AssetType.GENERIC), Maybe.empty(Integer.class)).loadObject("");
            assertTrue("Able to load children: " + mapChildren.size(),
                    !mapChildren.isEmpty());
            omgrDb.makeDbAssetDeleter().saveObject(aTest);
        } catch (Exception e) {
            olog.log(Level.INFO, "Caught unexpected", e);
            fail("Caught unexpected: " + e);
            bRollback = true;
        } finally {
            try {
                trans.endDbUpdate(bRollback);
            } catch (Exception ex) {
                olog.log(Level.WARNING, "Transaction commit failed", ex);
                fail("Failed to close transaction: " + ex);
            }
        }
    }

    /**
     * Test cache-sync stuff - load well known asset,
     * spoof other client modifying that asset, check for update/sync.
     */
    public void testCacheSync() {
        int i_client_id = omgrDb.getClientId();
        try {
            int i_client1 = i_client_id + 1;
            int i_client2 = i_client1 + 1;

            omgrDb.setClientId(i_client1);
            DbWriter<String> db_clear = omgrDb.makeDbCacheSyncClearer();
            db_clear.saveObject(null);

            DbReader<Map<UUID, Asset>, String> db_sync = omgrDb.makeDbCacheSyncLoader();
            Map<UUID, Asset> v_sync = db_sync.loadObject(null);
            assertTrue("Cache clear seems ok", v_sync.isEmpty());

            DbReader<Asset, UUID> db_reader = omgrDb.makeDbAssetLoader();
            Asset a_result = db_reader.loadObject(ouTestHome);
            assertTrue("Asset is of proper type", a_result instanceof littleware.security.LittlePrincipal);

            omgrDb.setClientId(i_client2);
            DbWriter<Asset> db_writer = omgrDb.makeDbAssetSaver();
            db_writer.saveObject(a_result);

            v_sync = db_sync.loadObject(null);
            Asset a_sync = v_sync.get(ouTestHome);
            assertTrue("Sync detected cross-client update", a_sync != null);
        } catch (Exception e) {
            olog.log(Level.INFO, "Caught unexepcted: " + e + ", " +
                    littleware.base.BaseException.getStackTrace(e));
            assertTrue("Caught unexpected: " + e, false);
        } finally {
            omgrDb.setClientId(i_client_id);
        }
    }
}


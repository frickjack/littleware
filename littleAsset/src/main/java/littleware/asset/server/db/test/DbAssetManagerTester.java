/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;

import littleware.asset.*;
import littleware.asset.TreeNode.TreeNodeBuilder;
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

    private static final Logger log = Logger.getLogger(DbAssetManagerTester.class.getName());
    private static final UUID testHomeId = UUIDFactory.parseUUID("D589EABED8EA43C1890DBF3CF1F9689A");
    private static final UUID testCreateId = UUIDFactory.parseUUID("0443fe54-53c6-4d80-ba88-d09c7d96d809");
    private static final AssetType  testSuperType;
    private static final AssetType  testSubType;
    static {
        final UUID superId = UUID.randomUUID();
        final UUID subId = UUID.randomUUID();
        final Date now = new Date();
        testSuperType = new AssetType( superId, "testSuper" + now.getTime() ) {};
        testSubType = new AssetType( subId, "testSub" + now.getTime() ) {
            @Override
            public Option<AssetType> getSuperType() {
                return Maybe.something( testSuperType );
        };
        };
    }
    private final DbAssetManager dbMgr;
    private final Provider<TreeNodeBuilder> nodeProvider;
    private final LittleTransaction trans;

    /**
     * Constructor stashes data to run tests against
     */
    @Inject
    public DbAssetManagerTester(DbAssetManager mgrDb,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            LittleTransaction trans
            ) {
        setName("testLoad");
        dbMgr = mgrDb;
        this.nodeProvider = nodeProvider;
        this.trans = trans;
    }

    /**
     * Test the load of a well-known test asset
     */
    public void testLoad() {
        trans.startDbAccess();
        try {
            final DbReader<Asset, UUID> db_reader = dbMgr.makeDbAssetLoader( trans );
            Asset a_result = db_reader.loadObject(testHomeId);
            assertTrue("Asset is of proper type",
                    a_result.getAssetType().equals(LittleHome.HOME_TYPE));
            assertTrue("Asset has right id", a_result.getId().equals(testHomeId));
            // Verify that looking up a non-existent thing does not throw an exceptioon
            final UUID uTest = UUIDFactory.getFactory().create();
            a_result = db_reader.loadObject(uTest);
            assertTrue("Got null result for nonexistent UUID", null == a_result);
        } catch (Exception e) {
            log.log(Level.INFO, "Caught unexpected", e);
            fail("Caught unexpected: " + e);
        } finally {
            trans.endDbAccess();
        }
    }

    /**
     * Test the load of a well-known test asset
     */
    public void testCreateUpdateDelete() {
        try {
            trans.startDbUpdate();
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed transaction setup", ex);
            fail("Failed to setup transaction: " + ex);
        }
        boolean bRollback = false;
        try {
            final DbReader<Asset, UUID> dbReader = dbMgr.makeDbAssetLoader( trans );
            final LittleHome aHome = dbReader.loadObject(testHomeId).narrow();
            {
                Asset aTest = dbReader.loadObject(testCreateId);
                if (null != aTest) {
                    final DbWriter<Asset> dbDelete = dbMgr.makeDbAssetDeleter( trans );
                    dbDelete.saveObject(aTest);
                }
            }
            final TreeNode testSave = nodeProvider.get().name("DbAssetTester").parent(aHome).
                    id(testCreateId).
                    parent(aHome).
                    comment("Just a test").
                    lastUpdate("Testing asset setup").
                    creatorId(aHome.getCreatorId()).
                    lastUpdaterId(aHome.getCreatorId()).
                    ownerId(aHome.getOwnerId()).
                    timestamp( trans.getTimestamp() ).build().narrow();
            final DbWriter<Asset> dbSaver = dbMgr.makeDbAssetSaver( trans );
            
            dbSaver.saveObject(testSave);
            final TreeNode aTest = dbMgr.makeDbAssetLoader( trans ).loadObject( testSave.getId()).narrow();
            assertTrue("From preserved on load", testSave.getParentId().equals(aHome.getId()));
            // Test from-loader
            final Map<String, UUID> mapChildren = dbMgr.makeDbAssetIdsFromLoader( trans, testHomeId, Maybe.something((AssetType) GenericAsset.GENERIC), Maybe.empty(Integer.class)).loadObject("");
            assertTrue("Able to load children: " + mapChildren.size(),
                    !mapChildren.isEmpty());
            dbMgr.makeDbAssetDeleter( trans ).saveObject(aTest);
        } catch (Exception e) {
            log.log(Level.INFO, "Caught unexpected", e);
            fail("Caught unexpected: " + e);
            bRollback = true;
        } finally {
            try {
                trans.endDbUpdate(bRollback);
            } catch (Exception ex) {
                log.log(Level.WARNING, "Transaction commit failed", ex);
                fail("Failed to close transaction: " + ex);
            }
        }
    }


    public void testAssetTypeCheck() {
        trans.startDbAccess();
        try {
            dbMgr.makeTypeChecker(trans).saveObject(testSubType);
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Caught exception", ex );
            fail( "Caught exception: "  + ex );
        } finally {
            trans.endDbAccess();
        }
    }
}


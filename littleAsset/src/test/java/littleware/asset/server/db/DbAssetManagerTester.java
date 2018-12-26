package littleware.asset.server.db;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

import littleware.asset.*;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.asset.server.LittleTransaction;
import littleware.db.*;
import littleware.base.*;
import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tests for DbAssetManager implementations.
 * Gets registered with the littleware.asset.test.PackageTestSuite
 */
@RunWith(littleware.test.LittleTestRunner.class)
public class DbAssetManagerTester {
    private static final Logger log = Logger.getLogger( DbAssetManagerTester.class.getName() );
    private static final UUID testHomeId = AbstractAssetTest.getTestHomeId();
    private static final UUID testCreateId = UUIDFactory.parseUUID("0443fe54-53c6-4d80-ba88-d09c7d96d809");
    private static final AssetType  testSuperType;
    private static final AssetType  testSubType;
    static {
        final UUID superId = UUID.randomUUID();
        final UUID subId = UUID.randomUUID();
        final Date now = new Date();
        testSuperType = new AssetType( superId, "testSuper" + now.getTime() ) {};
        testSubType = new AssetType( subId, "testSub" + now.getTime(), testSuperType ){};
    }
    private final DbCommandManager dbMgr;
    private final Provider<GenericAsset.GenericBuilder> nodeProvider;
    private final LittleTransaction trans;
    private final Provider<UUID> uuidFactory;

    /**
     * Constructor stashes data to run tests against
     */
    @Inject
    public DbAssetManagerTester(DbCommandManager mgrDb,
            Provider<GenericAsset.GenericBuilder> nodeProvider,
            LittleTransaction trans,
            Provider<UUID> uuidFactory
            ) {
        dbMgr = mgrDb;
        this.nodeProvider = nodeProvider;
        this.trans = trans;
        this.uuidFactory = uuidFactory;
    }

    /**
     * Test the load of a well-known test asset
     */
    @Test
    public void testLoad() {
        trans.startDbAccess();
        try {
            final DbReader<Asset, UUID> reader = dbMgr.makeDbAssetLoader( trans );
            final LittleHome testHome = reader.loadObject(testHomeId).narrow();
            assertTrue("Asset is of proper type",
                    testHome.getAssetType().equals(LittleHome.HOME_TYPE));
            assertTrue("Asset has right id", testHome.getId().equals(testHomeId));
            // Verify that looking up a non-existent thing does not throw an exceptioon
            final UUID bogusId = uuidFactory.get();
            assertTrue("Got null result for nonexistent UUID", null == reader.loadObject(bogusId) );
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
    @Test
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
                final Asset aTest = dbReader.loadObject(testCreateId);
                if (null != aTest) {
                    final DbWriter<Asset> dbDelete = dbMgr.makeDbAssetDeleter( trans );
                    dbDelete.saveObject(aTest);
                }
            }
            final GenericAsset testSave = nodeProvider.get().name("DbAssetTester").parent(aHome).
                    id(testCreateId).
                    parent(aHome).
                    data( "some data" ).
                    comment("Just a test").
                    lastUpdate("Testing asset setup").
                    creatorId(aHome.getCreatorId()).
                    lastUpdaterId(aHome.getCreatorId()).
                    ownerId(aHome.getOwnerId()).
                    timestamp( trans.getTimestamp() ).
                    putAttribute( "attr1", "bla").build();
            // Note - in normal operation - saving the same asset multiple times in a single transaction is disallowed
            // at higher API levels
            dbMgr.makeDbAssetSaver( trans ).saveObject(testSave);
            final GenericAsset testLoad = dbMgr.makeDbAssetLoader( trans ).loadObject( testSave.getId()).narrow();
            assertTrue("From preserved on load", testLoad.getParentId().equals(aHome.getId()));
            assertTrue( "attr1 preserved on load", "bla".equals( testLoad.getAttribute( "attr1" ).orElse( "Ugh!") ) );
            assertTrue( "comment preserved on load", testLoad.getComment().equals( testSave.getComment() ) );
            assertTrue( "data preserved on load", testLoad.getData().equals( testSave.getData() ) );
            
            final GenericAsset resave = testLoad.copy().narrow( GenericAsset.GenericBuilder.class 
                    ).removeAttribute("attr1").build();
            assertTrue( "Timestamp preserved on copy: " + resave.getTimestamp(), resave.getTimestamp() >= trans.getTimestamp() );
            assertTrue( "Ids are consistent", resave.getId().equals( testSave.getId() ) );
            assertTrue( "resave has no attributes", resave.getAttributeMap().isEmpty() );
            dbMgr.makeDbAssetSaver( trans ).saveObject( resave );
            assertTrue( "attr1 removed on reload", ! dbMgr.makeDbAssetLoader( trans ).loadObject( testSave.getId()).narrow( GenericAsset.class ).getAttribute("attr1" ).isPresent() );
            
            // Test from-loader
            final Map<String, UUID> mapChildren = (Map<String, UUID>) dbMgr.makeDbAssetIdsFromLoader( trans, testHomeId, Optional.of((AssetType) TreeNode.TREE_NODE_TYPE ), Optional.empty()).loadObject("");
            assertTrue("Able to load children using a parent asset type", !mapChildren.isEmpty());
            
            // Test transaction nonsense
            assertTrue( "Detected this transaction", 
                    ! dbMgr.makeLogLoader(trans, testHomeId).loadObject( trans.getTimestamp() - 10 ).isEmpty() 
                    );

            // Delete the test asset
            dbMgr.makeDbAssetDeleter( trans ).saveObject(testLoad);
            
        } catch (Exception ex ) {
            LittleTest.handle(ex);
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


    @Test
    public void testAssetTypeCheck() {
        trans.startDbAccess();
        try {
            dbMgr.makeTypeChecker(trans).saveObject(testSubType);
        } catch ( Exception ex ) { LittleTest.handle(ex);
        } finally {
            trans.endDbAccess();
        }
    }

    /**
     * Just query for home ids
     */
    @Test
    public void testHomeIdsQuery() {
        trans.startDbAccess();
        try {
            assertTrue( "Found some home ids",
                    ! dbMgr.makeDbHomeIdLoader(trans).loadObject("").isEmpty()
                    );
        } catch ( Exception ex ) { LittleTest.handle(ex);
        } finally {
            trans.endDbAccess();
        }
    }
    
    /**
     * Query by name
     */
    @Test
    public void testByNameQuery() {
        trans.startDbAccess();
        try {
            assertTrue( "Found the test home",
                    dbMgr.makeDbAssetsByNameLoader(trans, AbstractAssetTest.getTestHome(), LittleHome.HOME_TYPE ).loadObject("").isPresent()
                    );
        } catch ( Exception ex ) { LittleTest.handle(ex);
        } finally {
            trans.endDbAccess();
        }        
    }
}


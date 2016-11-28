package littleware.asset.client;


import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.asset.*;
import littleware.asset.GenericAsset.GenericBuilder;
import littleware.security.*;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Tester for implementations of the AssetManager interface 
 */
@RunWith(LittleTestRunner.class)
public class AssetManagerTester {

    private static final Logger log = Logger.getLogger(AssetManagerTester.class.getName());
    private final AssetManager assetMgr;
    private final AssetSearchManager searchMgr;
    public final static String MS_TEST_HOME = "littleware.test_home";
    private final Provider<LittleUser> provideCaller;
    private final Provider<GenericBuilder> assetProvider;

    /**
     * Stash AssetManager instance to run tests against
     *
     * @param assetMgr to test against
     * @param search to verify test results against
     */
    @Inject
    public AssetManagerTester(AssetManager assetMgr, AssetSearchManager search,
            Provider<LittleUser> provideCaller,
            Provider<GenericAsset.GenericBuilder> assetProvider
            ) {
        this.assetMgr = assetMgr;
        this.searchMgr = search;
        this.provideCaller = provideCaller;
        this.assetProvider = assetProvider;
    }


    /**
     * Just do some simple asset creationg/deletion/bla bla bla.
     */
    @Test
    public void testAssetCreation() {
        try {
            final Asset home = searchMgr.getByName(MS_TEST_HOME, LittleHome.HOME_TYPE).get();
            final Asset acl = null;

            log.log(Level.INFO, "Running with test home: " + home);

            final LittleUser user = provideCaller.get();
            assertTrue("Have an authenticated user", null != user);
            final String name = "test_" + (new Date()).getTime();

            final Date t_now = new Date();
            log.log(Level.INFO, "Saving new asset");
            final GenericAsset testAsset = assetMgr.saveAsset(
                    assetProvider.get().
                    name(name).
                    data("<data>no data </data>").
                    parent(user).
                    ownerId(user.getId()).
                    value(55).
                    state(3).
                    // Round end-date off to nearest second
                    endDate(new Date(t_now.getTime() - t_now.getTime() % 1000 + 1000 * 60 * 60L)).
                    putAttribute( "test", "test" ).
                    putLink( "test", UUID.randomUUID() ).
                    putDate( "test", new Date() ).
                    build(),
                    "new asset");

            log.log(Level.INFO, "Just created asset: " + name);
            assertTrue("Created an asset with some valid data",
                    (testAsset.getId() != null) && testAsset.getName().equals(name) && testAsset.getAssetType().equals(GenericAsset.GENERIC)
                    && t_now.getTime() < testAsset.getEndDate().getTime()
                    && (testAsset.getTimestamp() > 0)
                    && testAsset.getDate("test").isPresent()
                    && testAsset.getAttribute( "test" ).orElse( "frick" ).equals( "test" )
                    && testAsset.getLink("test" ).isPresent()
                    );

            final GenericAsset assetClone = testAsset.copy().build().narrow();
            assertTrue("Able to clone new asset",
                    testAsset.equals(assetClone) 
                    && assetClone.getId().equals( testAsset.getId() )
                    && assetClone.getName().equals(testAsset.getName())
                    && assetClone.getAssetType().equals(testAsset.getAssetType())
                    && assetClone.getTimestamp() == testAsset.getTimestamp()
                    && assetClone.getAttributeMap().equals( testAsset.getAttributeMap() )
                    && assetClone.getDateMap().equals( testAsset.getDateMap() )
                    && assetClone.getLinkMap().equals( testAsset.getLinkMap() )
                    );
            
            final GenericAsset assetLoad = searchMgr.getAsset( testAsset.getId() ).get().narrow();
            assertTrue("Able to load new asset",
                    testAsset.equals(assetLoad) 
                    && assetLoad.getId().equals( testAsset.getId() )
                    && assetLoad.getName().equals(testAsset.getName())
                    && assetLoad.getAssetType().equals(testAsset.getAssetType())
                    && assetLoad.getTimestamp() == testAsset.getTimestamp()
                    && assetLoad.getAttributeMap().equals( testAsset.getAttributeMap() )
                    && assetLoad.getDateMap().equals( testAsset.getDateMap() )
                    && assetLoad.getLinkMap().equals( testAsset.getLinkMap() )
                    );

            // Try to update the asset
            final GenericAsset savedAsset = assetMgr.saveAsset(
                    testAsset.copy().narrow( GenericAsset.GenericBuilder.class ).data("<data> some data </data>").
                    removeAttribute( "test" ).putAttribute( "test2", "test2" ).
                    removeLink( "test" ).putLink( "test2", UUID.randomUUID() ).
                    removeDate( "test" ).putDate( "test2", new Date() ).
                    build(),
                    "data update"
                    );
            assertTrue("Transaction count increases: " + savedAsset.getTimestamp(),
                    savedAsset.getTimestamp() > assetClone.getTimestamp()
                    );
            assertTrue( "Property maps updated on save",
                    (savedAsset.getAttributeMap().size() == 1)
                    && (savedAsset.getLinkMap().size() == 1)
                    && (savedAsset.getDateMap().size() == 1)
                    && savedAsset.getAttribute( "test2" ).isPresent()
                    && savedAsset.getLink( "test2" ).isPresent()
                    && savedAsset.getDate( "test2" ).isPresent()
                    );
            final GenericAsset loadedAsset = searchMgr.getAsset(testAsset.getId()).get().narrow();
            assertTrue("Able to load new asset and data matches",
                    loadedAsset.equals(assetClone)
                    && assetClone.getId().equals(loadedAsset.getId())
                    && assetClone.getName().equals(loadedAsset.getName())
                    && assetClone.getAssetType().equals(loadedAsset.getAssetType())
                    && assetClone.getEndDate().equals(loadedAsset.getEndDate())
                    && (loadedAsset.getValue() == 55.0) && (loadedAsset.getState() == 3)
                    );

            // Delete the asset
            log.log( Level.INFO, "Deleting test asset: " + assetClone );
            assetMgr.deleteAsset(assetClone.getId(), "Cleanup test case");

            assertTrue("No longer able to retrieve deleted asset: " + assetClone.getId(),
                    !searchMgr.getAsset(testAsset.getId()).isPresent()
                    );
        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex );
            fail("Caught: " + ex);
        }
    }
}


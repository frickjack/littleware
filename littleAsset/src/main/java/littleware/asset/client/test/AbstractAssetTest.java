package littleware.asset.client.test;

import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.List;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.LittleHome;
import littleware.base.BaseException;

/**
 * Slight specialization of junit.framework.TestCase
 * adds putName method to allow simultaneously setting
 * a TestCase test-method name and register the test-case
 * with a suite:  suite.addTest ( provider.get().putName( "testWhatever" ) )
 */
public abstract class AbstractAssetTest {
    public static UUID  getTestUserId() {
        return TestConfig.getTestUserId();
    }
    
    public static String getTestUserName() {
        return TestConfig.getTestUserName();
    }
    
    public static UUID   getTestHomeId() {
        return TestConfig.getTestHomeId();
    }
    
    /** Lots of tests want to create test assets under littleware.test_home */
    public static String getTestHome() {
        return TestConfig.getTestHome();
    }

    /**
     * Lots of test need to get the test-home in order to create
     * assets under it.
     */
    public static LittleHome getTestHome(AssetSearchManager search) throws BaseException, RemoteException, GeneralSecurityException {
        return search.getByName(getTestHome(), LittleHome.HOME_TYPE).get().narrow( LittleHome.class );
    }

    /**
     * Lots of tests want to delete a bunch of assets at tearDown time.
     * This method loops over the list of ids, tries to delete each one,
     * and just logs if the delete fails.
     *
     * @param vDelete assets to try to delete
     * @param mgrAsset to delete with
     * @param log to log delete failures to - do not throw exceptions
     */
    public static void deleteTestAssets(List<UUID> vDelete, AssetManager mgrAsset, Logger log) {
        for (UUID uDelete : vDelete) {
            try {
                mgrAsset.deleteAsset(uDelete, "cleanup test");
            } catch (Exception ex) {
                log.log(Level.WARNING, "Failed to delete test asset: " + uDelete, ex);
            }
        }
    }

}

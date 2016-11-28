package littleware.asset.client.test;

import java.util.UUID;
import littleware.base.UUIDFactory;

/**
 * Just a container for some constants.
 * Moved out from AbstractAssetTest to avoid runtime dependency on junit -
 * db-init code accesses these constants.
 */
public final class TestConfig {
    private TestConfig(){}
    
    private static final UUID testUserId = UUIDFactory.parseUUID( "7AC5D21049254265B224B7512EFCF0D1");
    public static UUID  getTestUserId() {
        return testUserId;
    }
    
    private static final String testUserName = "littleware.test_user";
    public static String getTestUserName() {
        return testUserName;
    }
    
    
    private static final UUID testHomeId = UUIDFactory.parseUUID("D589EABED8EA43C1890DBF3CF1F9689A");
    public static UUID   getTestHomeId() {
        return testHomeId;
    }
    
    /** Lots of tests want to create test assets under littleware.test_home */
    public static String getTestHome() {
        return "littleware.test_home";
    }
    
}

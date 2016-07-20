package littleware.base;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.UUID;
import littleware.base.UUIDFactory;


import littleware.test.LittleTest;
import static org.junit.Assert.assertTrue;
import org.junit.Test;

/**
 * Just run UUIDFactory implementations through a simple test
 */
public class UUIDFactoryTester {

    private final Provider<UUID> uuidFactory;

    /**
     * Constructor stashes name of test to run, and UUIDFactory to run test
     * against
     */
    @Inject()
    public UUIDFactoryTester(Provider<UUID> uuidFactory) {
        this.uuidFactory = uuidFactory;
    }

    /**
     * Just get a couple UUID's, then go back and forth to the string
     * representation
     */
    @Test
    public void testUUIDFactory() {
        try {
            UUID u_test1 = uuidFactory.get();
            UUID u_test2 = uuidFactory.get();

            assertTrue("Got 2 different id's", !u_test1.equals(u_test2));
            UUID u_from_string = UUIDFactory.parseUUID(u_test1.toString());
            assertTrue("Able to parse " + u_test1, u_from_string.equals(u_test1));
            u_from_string = UUIDFactory.parseUUID(u_test1.toString().replaceAll("-", ""));
            assertTrue("Able to parse with no dashes: " + u_test1,
                    u_from_string.equals(u_test1));
        } catch (Exception ex) {
            LittleTest.handle(ex);
        }
    }
}

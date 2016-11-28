package littleware.security.client;

import com.google.inject.Inject;
import com.google.inject.Provider;


import java.security.Principal;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.LittleHome;


import littleware.asset.client.AssetManager;
import littleware.asset.client.AssetSearchManager;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.security.*;
import littleware.base.BaseException;
import littleware.security.LittleGroup.Builder;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * TestFixture runs SecurityMnaager implementations
 * through their paces.
 */
@RunWith(littleware.test.LittleTestRunner.class)
public class AccountManagerTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(AccountManagerTester.class.getName());
    private final AssetSearchManager search;
    private final AssetManager assetMgr;
    private final LittleUser caller;
    private final Provider<Builder> groupProvider;

    /**
     * Constructor registers the AccountManager to test against.
     * Use littleware.security.test.LoginTester as LoginContext application name.
     */
    @Inject
    public AccountManagerTester(
            AssetManager assetMgr,
            AssetSearchManager search,
            LittleUser caller,
            Provider<LittleGroup.Builder> groupProvider
            ) {
        this.assetMgr = assetMgr;
        this.search = search;
        this.caller = caller;
        this.groupProvider = groupProvider;
    }

    /**
     * Just retrieve some Principals - USER and GROUP
     */
    @Test
    public void testGetPrincipals() {
        try {
            final LittleUser userAdmin = search.getByName(AccountManager.LITTLEWARE_ADMIN, LittleUser.USER_TYPE).get().narrow();
            final LittleGroup groupAdmin = search.getAsset(
                    AccountManager.UUID_ADMIN_GROUP).get().narrow();

            for (LittlePrincipal member : groupAdmin.getMembers() ) {
                log.log(Level.INFO, "Got admin group member: " + member.getName() +
                        " (" + member.getId() + ")");
            }
            assertTrue("administrator should be member of admin group",
                    groupAdmin.isMember(userAdmin));
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex );
            assertTrue("Caught unexpected exception: " + ex + ", " +
                    BaseException.getStackTrace(ex), false);
        }
    }


    /**
     * Test group update
     */
    @Test
    public void testGroupUpdate() {
        try {
            final String name = "group.littleware.test_user";
            final LittleHome home = getTestHome( search );
            LittleGroup groupTest = (LittleGroup) search.getAssetFrom( home.getId(), name ).orElse(null);
            if (null == groupTest) {
                groupTest = assetMgr.saveAsset(
                        groupProvider.get().add(caller).name(name).parent( home ).
                        build(), "setup test group").narrow();
                assertTrue( caller.getName() + " is member of new group " + groupTest.getName(),
                        groupTest.isMember(caller)
                        );
            }
            if ( ! groupTest.isMember( caller ) ) {
                LittleGroup copy = groupTest.copy().narrow( LittleGroup.Builder.class ).add(caller).build();
                copy = copy.copy().build().narrow();
                assertTrue( caller.getName() + " added to " + copy.getName(),
                    copy.isMember( caller )
                    );
                final Set<Principal> memberSet = new HashSet<Principal>( copy.getMembers() );
                assertTrue( caller.getName() + " in members copy of " + copy.getName(),
                        memberSet.contains(caller)
                        );
                groupTest = assetMgr.saveAsset(copy, "Add tester " + caller.getName());
            }
            assertTrue( caller.getName() + " is member of " + groupTest.getName(),
                    groupTest.isMember( caller )
                    );
            /* test-user may not have permission to edit group!  doh! ... fix this in db initialization ...
            groupTest = assetMgr.saveAsset(groupTest.copy().narrow( LittleGroup.Builder.class ).remove(caller).timestamp( -1L ).build(), "Removed tester " + caller.getName());
            groupTest = search.getAsset( groupTest.getId() ).get().narrow();
            assertTrue( caller.getName() + " is not a member of " + groupTest.getName(),
                    !groupTest.isMember(caller)
                    );
             * 
             */
        } catch (Exception ex) {
            log.log(Level.INFO, "Failed test", ex );
            fail("Should not have caught: " + ex);
        }
    }
}



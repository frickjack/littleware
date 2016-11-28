package littleware.asset.client;

import com.google.common.collect.ImmutableMap;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import littleware.asset.*;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.security.LittleGroup;
import littleware.security.LittleUser;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Tester for implementations of the AssetSearchManager interface.
 * AssetPathTester also tests AssetSearchManager.
 */
@RunWith(LittleTestRunner.class)
public class AssetSearchManagerTester extends AbstractAssetTest {

    private static final Logger log = Logger.getLogger(AssetSearchManagerTester.class.getName());
    private final AssetSearchManager search;
    private final AssetManager assetMan;
    private final Provider<TreeNodeBuilder> nodeProvider;
    private final AssetPathFactory pathFactory;

    /**
     * Inject dependencies
     */
    @Inject
    public AssetSearchManagerTester( AssetSearchManager search,
            AssetManager assetMan,
            Provider<TreeNode.TreeNodeBuilder> nodeProvider,
            AssetPathFactory pathFactory
            ) {
        this.search = search;
        this.assetMan = assetMan;
        this.nodeProvider = nodeProvider;
        this.pathFactory = pathFactory;
    }

    /**
     * Just load some test assets.
     */
    @Test
    public void testSearch() {
        try {
            final Asset lookup = search.getByName(AssetManagerTester.MS_TEST_HOME,
                    LittleHome.HOME_TYPE).orElse(null);
            assertTrue("Got some home-by-name data", null != lookup);

            assertTrue("Searcher did not freak out on empty search",
                    !search.getByName("frickityFrickjackFroo", LittleUser.USER_TYPE).isPresent());
            assertTrue("Child search did not freak on empty search",
                    (!search.getAssetFrom(lookup.getId(), "UgidyUgaUga").isPresent()) && (!search.getAssetFrom(UUID.randomUUID(), "whatever").isPresent()));

            // Test everybody group - which is a crazy special case
            final LittleGroup everybody = search.getAssetAtPath( pathFactory.createPath( "/littleware.home/group.littleware.everybody" ) ).get().narrow();
                    //search.getAsset(AccountManager.UUID_EVERYBODY_GROUP).get().narrow();
            /*
             * New Everybody group does not conform to normal group behavior
             * 
            for (LittlePrincipal member : group_everybody.getMembers() ) {
                Set<UUID> v_links = search.getAssetIdsTo(member.getId(),
                        LittleGroupMember.GROUP_MEMBER_TYPE
                        );
                assertTrue("Group member as links TO it: " + member,
                        !v_links.isEmpty());
            }
             * 
             */
        } catch (Exception e) {
            log.log(Level.WARNING, "Test failed", e);
            fail("Caught: " + e);
        }
    }

    /**
     * Update a well known asset, then make sure
     * it shows up in the timestamp log
     */
    @Test
    public void testTransactionLog() {
        try {
          final LittleHome home = getTestHome( search );
          final TreeNode testNode;
          { // update the test asset
              final String name = "testTransactionLog";
              final AssetRef maybeTest = search.getAssetFrom( home.getId(), name);
              if ( maybeTest.isPresent() ) {
                  testNode = assetMan.saveAsset( maybeTest.get(), "force an update for testing" ).narrow();
              } else {
                  testNode = assetMan.saveAsset( nodeProvider.get().name( name ).parent( home ).build(),
                               "setup test asset");
              }
          }

        } catch (Exception e) {
            log.log(Level.WARNING, "Test failed", e);
            fail("Caught: " + e);
        }
    }

    /**
     * Just load some test assets.
     */
    @Test
    public void testLoad() {
        try {
            final ImmutableMap<String, AssetInfo> homeIds = search.getHomeAssetIds();
            assertTrue("Home-asset set is not empty", !homeIds.isEmpty());
            for (String homeName : homeIds.keySet()) {
                log.log(Level.INFO, "getHomeAssetIds found home: " + homeName);
            }
            assertTrue("Test-home is in home set: " + getTestHome(),
                    homeIds.containsKey(getTestHome())
                    );
            /*...
            final Map<String,UUID> children = search.getAssetIdsFrom( v_home_id.get( getTestHome() ));
            final LittleGroup everybody = search.getAsset( children.get(AccountManager.LITTLEWARE_EVERYBODY_GROUP) ).get().narrow();
            final LittleUser  user = search.getAsset( children.get("littleware.test_user") ).get().narrow();
            assertTrue()
             */

            for( Map.Entry<String,AssetInfo> entry : homeIds.entrySet() ) {
                final AssetRef maybe = search.getAsset( entry.getValue().getId() );
                assertTrue("Able to retrieve home: " + entry.getKey(),
                    maybe.isPresent()
                    );
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Caught exception", ex );
            fail("Caught: " + ex);
        }
    }

}


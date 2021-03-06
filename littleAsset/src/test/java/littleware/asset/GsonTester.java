package littleware.asset;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.google.inject.Inject;
import com.google.inject.Provider;
import littleware.asset.client.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.gson.LittleGsonResolver;
import littleware.base.BaseException;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.test.LittleTestRunner;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import org.junit.Test;
import org.junit.runner.RunWith;


/**
 * Test json serialization
 */
@RunWith(LittleTestRunner.class)
public class GsonTester {

    private static final Logger log = Logger.getLogger(GsonTester.class.getName());
    private final Map<UUID, Asset> testAssets;
    private final LittleGsonResolver mockResolver = new LittleGsonResolver() {

        @Override
        public Optional<Asset> getAsset(UUID id) throws BaseException, GeneralSecurityException, RemoteException {
            return Optional.ofNullable(testAssets.get(id));
        }

        @Override
        public void markInProcess(UUID id) {
        }
    };

    private final LittleGsonFactory gsonFactory;

    @Inject
    public GsonTester(LittleGsonFactory gsonFactory,
            Provider<GenericAsset.GenericBuilder> genericBuilder,
            Provider<TreeNode.TreeNodeBuilder> tnodeBuilder,
            Provider<LittleUser.Builder> userFactory,
            Provider<LittleGroup.Builder> groupFactory,
            Provider<LittleAcl.Builder> aclFactory,
            Provider<LittleAclEntry.Builder> aclEFactory) {
        this.gsonFactory = gsonFactory;
        final UUID homeId = UUID.randomUUID();
        final ImmutableMap.Builder<UUID, Asset> testBuilder = ImmutableMap.builder();
        final LittleGroup.Builder groupBuilder = groupFactory.get().homeId(homeId).parentId(homeId).name("testAsset");
        for (String name : new String[]{"fred", "barney", "murray"}) {
            final LittleUser user = userFactory.get().homeId(homeId).parentId(homeId).name(name).build();
            groupBuilder.add(user);
            testBuilder.put(user.getId(), user);
        }
        final LittleGroup group = groupBuilder.build();
        testBuilder.put(group.getId(), group);
        final UUID aclId = UUID.randomUUID();
        final LittleAcl acl = aclFactory.get().homeId(homeId).parentId(homeId).id(aclId).name("testAcl").addEntry(
                aclEFactory.get().owningAclId(aclId).addPermission(LittlePermission.READ).principal(group).build()).build();

        testBuilder.put(acl.getId(), acl);
        testAssets = testBuilder.build();
    }

    @Test
    public void testGson() {
        try {
            final Gson gson = gsonFactory.get(mockResolver);
            for (Asset assetIn : testAssets.values()) {
                final Asset assetOut = gson.fromJson(gson.toJson(assetIn, Asset.class), Asset.class);
                assertTrue("Gson conversion preserves asset type: " + assetOut.getAssetType() + ", " + assetIn.getAssetType(),
                        assetOut.getAssetType().equals(assetIn.getAssetType()));
                assertTrue("Gson conversion preserves id and name",
                        assetOut.getName().equals(assetIn.getName())
                        && assetOut.getId().equals(assetIn.getId()));
            }

            // Test GSON handling of name-id maps
            final TypeToken<Map<String,UUID>> nameIdType = new TypeToken<Map<String,UUID>>() {};
            final Map<String,UUID> testIn;
            final Map<String,UUID> testOut;
            {
                final ImmutableMap.Builder<String,UUID> builder = ImmutableMap.builder();
                for( int i=0; i < 10; ++i ) {
                    builder.put( "a" + i, UUID.randomUUID() );
                }
                testIn = builder.build();
            }
            testOut = gson.fromJson( gson.toJson( testIn, nameIdType.getType() ), nameIdType.getType() );
            assertTrue( "Gson map in/out got expected size: " + testOut.size(),
                   testOut.size() == testIn.size()
                    );
            for( Map.Entry<String,UUID> scan : testIn.entrySet() ) {
                assertTrue( scan.getKey() + " got expected gson out value: " + testOut.get( scan.getKey() ),
                        scan.getValue().equals( testOut.get( scan.getKey() ))
                        );
            }
        } catch (Exception ex) {
            log.log(Level.WARNING, "Failed test", ex);
            fail("Caught ex: " + ex);
        }
    }
}

/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.test;

import com.google.common.collect.ImmutableMap;
import com.google.gson.Gson;
import com.google.inject.Inject;
import com.google.inject.Provider;
import java.rmi.RemoteException;
import java.security.GeneralSecurityException;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.Asset;
import littleware.asset.GenericAsset;
import littleware.asset.TreeNode;
import littleware.asset.gson.LittleGsonFactory;
import littleware.asset.gson.LittleGsonResolver;
import littleware.base.BaseException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.test.LittleTest;

/**
 *
 * @author pasquini
 */
public class GsonTester extends LittleTest {
    private static final Logger log = Logger.getLogger( GsonTester.class.getName() );
    
    private final Map<UUID, Asset> testAssets;
    private final LittleGsonResolver mockResolver = new LittleGsonResolver() {

        @Override
        public Option<Asset> getAsset(UUID id) throws BaseException, GeneralSecurityException, RemoteException {
            return Maybe.emptyIfNull( testAssets.get( id) );
        }

        @Override
        public void markInProcess(UUID id) {
        }
    };

    {
        setName("testGson");
    }
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
        final LittleGroup.Builder groupBuilder = groupFactory.get().homeId( homeId ).parentId( homeId ).name("testAsset");
        for (String name : new String[]{"fred", "barney", "murray"}) {
            final LittleUser user = userFactory.get().homeId( homeId ).parentId( homeId ).name(name).build();
            groupBuilder.add(user);
            testBuilder.put(user.getId(), user);
        }
        final LittleGroup group = groupBuilder.build();
        testBuilder.put(group.getId(), group);
        final UUID aclId = UUID.randomUUID();
        final LittleAcl acl = aclFactory.get().homeId( homeId ).parentId( homeId ).id(aclId).name("testAcl").addEntry(
                aclEFactory.get().owningAclId(aclId).addPermission(LittlePermission.READ).principal(group).build()).build();

        testBuilder.put(acl.getId(), acl);
        testAssets = testBuilder.build();
    }

    public void testGson() {
        try {
        final Gson gson = gsonFactory.get(mockResolver);
        for (Asset assetIn : testAssets.values()) {
            final Asset assetOut = gson.fromJson( gson.toJson(assetIn, Asset.class ), Asset.class);
            assertTrue("Gson conversion preserves asset type: " + assetOut.getAssetType() + ", " + assetIn.getAssetType(),
                    assetOut.getAssetType().equals(assetIn.getAssetType()));
            assertTrue("Gson conversion preserves id and name",
                    assetOut.getName().equals(assetIn.getName())
                    && assetOut.getId().equals(assetIn.getId()));
        }
        } catch( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Caught ex: " + ex );
        }
    }
}

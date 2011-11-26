/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.LittleHome;
import littleware.asset.LittleHome.HomeBuilder;
import littleware.asset.TreeNode;
import littleware.asset.TreeNode.TreeNodeBuilder;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.asset.server.LittleTransaction;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.AssertionFailedException;
import littleware.security.AccountManager;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittleGroupMember;
import littleware.security.LittleGroupMember.MemberBuilder;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;
import littleware.security.LittleUser.Builder;

/**
 * Shared helper class populates an empty littleware repository database
 * with a catalog of initial assets like /littleware.home, etc.
 */
public class DbInitializer {
    private static final Logger log = Logger.getLogger( DbInitializer.class.getName() );
    
    private final Provider<LittleTransaction> transFactory;
    private final Provider<HomeBuilder> homeFactory;
    private final Provider<TreeNodeBuilder> treeNodeFactory;
    private final Provider<Builder> userFactory;
    private final Provider<LittleGroup.Builder> groupFactory;
    private final Provider<MemberBuilder> groupMemberFactory;
    private final Provider<LittleAcl.Builder> aclFactory;
    private final Provider<LittleAclEntry.Builder> aclEntryFactory;
    private final AssetProviderRegistry assetRegistry;

    @Inject
    public DbInitializer(
            Provider<LittleTransaction> transFactory,
            Provider<LittleHome.HomeBuilder> homeFactory,
            Provider<TreeNode.TreeNodeBuilder> treeNodeFactory,
            Provider<LittleUser.Builder> userFactory,
            Provider<LittleGroup.Builder> groupFactory,
            Provider<LittleGroupMember.MemberBuilder> groupMemberFactory,
            Provider<LittleAcl.Builder> aclFactory,
            Provider<LittleAclEntry.Builder> aclEntryFactory,
            AssetProviderRegistry assetRegistry) {
        this.transFactory = transFactory;
        this.homeFactory = homeFactory;
        this.treeNodeFactory = treeNodeFactory;
        this.userFactory = userFactory;
        this.groupFactory = groupFactory;
        this.groupMemberFactory = groupMemberFactory;
        this.aclFactory = aclFactory;
        this.aclEntryFactory = aclEntryFactory;
        this.assetRegistry = assetRegistry;
    }


    public void initDB( DbAssetManager mgr ) {
        {
            // Hack here - register the asset-types being used here, because this
            // code may run before the OSGi activators that register the providers with
            // the the provider registry - and the asset-loader depends on the registry
            assetRegistry.registerService(LittleHome.HOME_TYPE, homeFactory);
            assetRegistry.registerService(LittleUser.USER_TYPE, userFactory);
            assetRegistry.registerService(LittleGroup.GROUP_TYPE, groupFactory);
            assetRegistry.registerService(LittleGroupMember.GROUP_MEMBER_TYPE, groupMemberFactory);
            assetRegistry.registerService(LittleAcl.ACL_TYPE, aclFactory);
            assetRegistry.registerService(LittleAclEntry.ACL_ENTRY, aclEntryFactory);
        }
        final LittleTransaction trans = transFactory.get();
        trans.startDbUpdate();
        try { // Go on to setup initial freakin' repository nodes ...
            if (null == mgr.makeDbAssetLoader(trans).loadObject(AbstractAssetTest.getTestHomeId())) {
                // Note: need to initialize everything as we're bypassing a couple layers of API
                final LittleHome testHome = homeFactory.get().homeId(AbstractAssetTest.getTestHomeId()).id(AbstractAssetTest.getTestHomeId()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).ownerId(AbstractAssetTest.getTestUserId()).aclId(LittleAcl.UUID_EVERYBODY_READ).name(AbstractAssetTest.getTestHome()).comment("Tree for test-case nodes").lastUpdate("auto-created by AwsModule domain setup").timestamp(trans.getTimestamp()).build().narrow();
                mgr.makeDbAssetSaver(trans).saveObject(testHome);
            }
            LittleHome littleHome = (LittleHome) mgr.makeDbAssetLoader(trans).loadObject(LittleHome.LITTLE_HOME_ID);
            if (null == littleHome) {
                // Note: need to initialize everything as we're bypassing a couple layers of API
                littleHome = homeFactory.get().homeId(LittleHome.LITTLE_HOME_ID).id(LittleHome.LITTLE_HOME_ID).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).ownerId(AccountManager.UUID_ADMIN).aclId(LittleAcl.UUID_EVERYBODY_READ).name(LittleHome.LITTLE_HOME).comment("littleware home tree").lastUpdate("auto-created by AwsModule domain setup").timestamp(trans.getTimestamp()).build().narrow();
                mgr.makeDbAssetSaver(trans).saveObject(littleHome);
            }
            LittleUser admin = (LittleUser) mgr.makeDbAssetLoader(trans).loadObject(AccountManager.UUID_ADMIN);
            if (null == admin) {
                admin = this.userFactory.get().parent(littleHome).id(AccountManager.UUID_ADMIN).name(AccountManager.LITTLEWARE_ADMIN).aclId(null).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).ownerId(AccountManager.UUID_ADMIN).comment("littleware admin user").lastUpdate("auto-created by AwsModule domain setup").build().narrow();
                mgr.makeDbAssetSaver(trans).saveObject(admin);
            }
            if (null == mgr.makeDbAssetLoader(trans).loadObject(AbstractAssetTest.getTestUserId())) {
                final LittleUser testUser = this.userFactory.get().parent(littleHome).id(AbstractAssetTest.getTestUserId()).name(AbstractAssetTest.getTestUserName()).aclId(null).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).ownerId(AbstractAssetTest.getTestUserId()).comment("littleware test user").lastUpdate("auto-created by AwsModule domain setup").build().narrow();
                mgr.makeDbAssetSaver(trans).saveObject(testUser);
            }

            if (null == mgr.makeDbAssetLoader(trans).loadObject(AccountManager.UUID_ADMIN_GROUP)) {
                final LittleGroup adminGroup = this.groupFactory.get().parent(littleHome).id(AccountManager.UUID_ADMIN_GROUP).name("group.littleware.administrator").aclId(LittleAcl.UUID_EVERYBODY_READ).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).ownerId(AccountManager.UUID_ADMIN).comment("littleware admin group").lastUpdate("auto-created by AwsModule domain setup").build().narrow();
                mgr.makeDbAssetSaver(trans).saveObject(adminGroup);

                // add member to group
                final LittleGroupMember link = groupMemberFactory.get().group(adminGroup).member(admin).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("littleware admin group").lastUpdate("auto-created by AwsModule domain setup").build();
                mgr.makeDbAssetSaver(trans).saveObject(link);
            }
            LittleGroup everybody = (LittleGroup) mgr.makeDbAssetLoader(trans).loadObject(AccountManager.UUID_EVERYBODY_GROUP);
            if (null == everybody) {
                // Create a stub group for everybody group - everybody group is actualy
                // implemented as a special class where isMember(x) == true in all cases
                everybody = this.groupFactory.get().parent(littleHome).id(AccountManager.UUID_EVERYBODY_GROUP).name("group.littleware.everybody").aclId(LittleAcl.UUID_EVERYBODY_READ).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).ownerId(AccountManager.UUID_ADMIN).comment("littleware everybody group").lastUpdate("auto-created by AwsModule domain setup").build().narrow();
                mgr.makeDbAssetSaver(trans).saveObject(everybody);
            }

            if (null == mgr.makeDbAssetLoader(trans).loadObject(LittleAcl.UUID_EVERYBODY_READ)) {
                final LittleAcl aclRead = aclFactory.get().id(LittleAcl.UUID_EVERYBODY_READ).aclId(LittleAcl.UUID_EVERYBODY_READ).name("acl.littleware.everybody.read").parent(littleHome).ownerId(AccountManager.UUID_ADMIN).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("littleware everybody read acl").lastUpdate("auto-created by AwsModule domain setup").build();
                mgr.makeDbAssetSaver(trans).saveObject(aclRead);

                final LittleAclEntry entry = aclEntryFactory.get().owningAcl(aclRead).principal(everybody).name("group.littleware.everybody.positive").addPermission(LittlePermission.READ).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("everybody group entry").lastUpdate("auto-created by AwsModule domain setup").build();
                mgr.makeDbAssetSaver(trans).saveObject(entry);
            }
            if (null == mgr.makeDbAssetLoader(trans).loadObject(LittleAcl.UUID_EVERYBODY_WRITE)) {
                final LittleAcl aclWrite = aclFactory.get().id(LittleAcl.UUID_EVERYBODY_WRITE).aclId(LittleAcl.UUID_EVERYBODY_READ).name("acl.littleware.everybody.write").parent(littleHome).ownerId(AccountManager.UUID_ADMIN).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("littleware everybody read and write acl").lastUpdate("auto-created by AwsModule domain setup").build();
                mgr.makeDbAssetSaver(trans).saveObject(aclWrite);

                final LittleAclEntry entry = aclEntryFactory.get().owningAcl(aclWrite).principal(everybody).name("group.littleware.everybody.positive").addPermission(LittlePermission.READ).addPermission(LittlePermission.WRITE).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("everybody group entry").lastUpdate("auto-created by AwsModule domain setup").build();
                mgr.makeDbAssetSaver(trans).saveObject(entry);
            }

        } catch (SQLException ex) {
            log.log(Level.WARNING, "Failed to initialize AWS domain", ex);
            throw new AssertionFailedException("Failed to initialize AWS domain", ex);
        } finally {
            trans.endDbUpdate(false);
        }
    }
}

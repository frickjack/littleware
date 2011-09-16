/*
 * Copyright 2011 http://code.google.com/p/littleware/
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.PropertiesCredentials;
import com.amazonaws.services.simpledb.AmazonSimpleDB;
import com.amazonaws.services.simpledb.AmazonSimpleDBClient;
import com.amazonaws.services.simpledb.model.CreateDomainRequest;
import com.amazonaws.services.simpledb.model.DeleteDomainRequest;
import com.google.inject.Binder;
import com.google.inject.Inject;
import com.google.inject.Provider;
import com.google.inject.Scopes;
import java.io.IOException;
import java.io.InputStream;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.AssetType;
import littleware.asset.LittleHome;
import littleware.asset.TreeNode;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.db.AbstractLittleTransaction;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.spi.AssetProviderRegistry;
import littleware.base.AssertionFailedException;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.PropertiesLoader;
import littleware.bootstrap.AppBootstrap;
import littleware.security.AccountManager;
import littleware.security.LittleAcl;
import littleware.security.LittleAclEntry;
import littleware.security.LittleGroup;
import littleware.security.LittleGroupMember;
import littleware.security.LittlePermission;
import littleware.security.LittleUser;

/**
 * Configuration module for AWS database backend.
 * The bootstrap code will create and initialize the SimpleDB domain
 * at guice-configure time if the domain doesn't already exist.
 */
public class AwsModule extends AbstractServerModule {

    /**
     * The AWS SimpleDB domain in AwsConfig is normally loaded from
     * an AwsConfig.properties file, but this property overrides
     * that value if set.  Mostly useful for testing.  The value is
     * only checked at boot time - changing dbDomainOverride after
     * the application has started has no effect.
     */
    public static Option<String> dbDomainOverride = Maybe.empty();
    private static final Logger log = Logger.getLogger(AwsModule.class.getName());

    public AwsModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

    /**
     * AWS implementation of LittleTransaction
     */
    public static class AwsTransaction extends AbstractLittleTransaction {

        private final long timestamp = (new java.util.Date()).getTime();

        @Override
        protected void endDbAccess(int updateLevel) {
            // NOOP
        }

        @Override
        protected void endDbUpdate(boolean rollback, int updateLevel) {
            if (rollback) {
                log.log(Level.WARNING, "SimpleDB backend does not support rollback");
            }
        }

        @Override
        public long getTimestamp() {
            return timestamp;
        }
    }

    /**
     * Handler for initializing a new AWS SimpleDB domain for littleware use.
     * Domain is initialized lazily at the first attempt to instantiate an
     * AwsDbAssetManager.
     */
    public static class SetupHandler implements Runnable, Provider<DbAssetManager> {

        private final AmazonSimpleDB db;
        private final AwsConfig config;
        private final DbAssetManager mgr;
        private boolean isDomainInitialized = false;
        private final Provider<LittleTransaction> transFactory;
        private final Provider<LittleHome.HomeBuilder> homeFactory;
        private final Provider<TreeNode.TreeNodeBuilder> treeNodeFactory;
        private final Provider<LittleUser.Builder> userFactory;
        private final Provider<LittleGroup.Builder> groupFactory;
        private final Provider<LittleGroupMember.MemberBuilder> groupMemberFactory;
        private final Provider<LittleAcl.Builder> aclFactory;
        private final Provider<LittleAclEntry.Builder> aclEntryFactory;
        private final AssetProviderRegistry assetRegistry;

        @Inject
        public SetupHandler(AmazonSimpleDB db, AwsConfig config, AwsDbAssetManager mgr,
                Provider<LittleTransaction> transFactory,
                Provider<LittleHome.HomeBuilder> homeFactory,
                Provider<TreeNode.TreeNodeBuilder> treeNodeFactory,
                Provider<LittleUser.Builder> userFactory,
                Provider<LittleGroup.Builder> groupFactory,
                Provider<LittleGroupMember.MemberBuilder> groupMemberFactory,
                Provider<LittleAcl.Builder> aclFactory,
                Provider<LittleAclEntry.Builder> aclEntryFactory,
                AssetProviderRegistry assetRegistry) {
            this.db = db;
            this.config = config;
            this.mgr = mgr;
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

        @Override
        public void run() {
            if ( false ) {
                // !!!! DANGER !!!!
                // Debug hook for zapping domains ... should move this out to a command line or whatever.
                db.deleteDomain( new DeleteDomainRequest( "littleware" ) );
                db.deleteDomain(new DeleteDomainRequest("littleTestDomain"));
                throw new RuntimeException("Domains zeroed out!");
            }
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
            if (!db.listDomains().getDomainNames().contains(config.getDbDomain())) {
                db.createDomain(new CreateDomainRequest(config.getDbDomain()));
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

                    final LittleAclEntry entry = aclEntryFactory.get().acl(aclRead).principal(everybody).name("group.littleware.everybody.positive").addPermission(LittlePermission.READ).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("everybody group entry").lastUpdate("auto-created by AwsModule domain setup").build();
                    mgr.makeDbAssetSaver(trans).saveObject(entry);
                }
                if (null == mgr.makeDbAssetLoader(trans).loadObject(LittleAcl.UUID_EVERYBODY_WRITE)) {
                    final LittleAcl aclWrite = aclFactory.get().id(LittleAcl.UUID_EVERYBODY_WRITE).aclId( LittleAcl.UUID_EVERYBODY_READ ).name("acl.littleware.everybody.write").parent(littleHome).ownerId(AccountManager.UUID_ADMIN).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("littleware everybody read and write acl").lastUpdate("auto-created by AwsModule domain setup").build();
                    mgr.makeDbAssetSaver(trans).saveObject(aclWrite);

                    final LittleAclEntry entry = aclEntryFactory.get().acl(aclWrite).principal(everybody).name("group.littleware.everybody.positive").addPermission(LittlePermission.READ).addPermission(LittlePermission.WRITE).timestamp(trans.getTimestamp()).creatorId(AccountManager.UUID_ADMIN).lastUpdaterId(AccountManager.UUID_ADMIN).comment("everybody group entry").lastUpdate("auto-created by AwsModule domain setup").build();
                    mgr.makeDbAssetSaver(trans).saveObject(entry);
                }

            } catch (SQLException ex) {
                log.log(Level.WARNING, "Failed to initialize AWS domain", ex);
                throw new AssertionFailedException("Failed to initialize AWS domain", ex);
            } finally {
                trans.endDbUpdate(false);
            }
        }

        @Override
        public DbAssetManager get() {
            if (!isDomainInitialized) {
                this.run();
                isDomainInitialized = true;
            }
            return mgr;
        }
    }

    @Override
    public void configure(Binder binder) {
        binder.bind(LittleTransaction.class).to(AwsTransaction.class);
        binder.bind(AwsDbAssetManager.class).in(Scopes.SINGLETON);
        binder.bind(DbAssetManager.class).toProvider(SetupHandler.class).in(Scopes.SINGLETON);
        final String credsPath = "littleware/asset/server/db/aws/AwsCreds.properties";
        try {
            final InputStream is = getClass().getClassLoader().getResourceAsStream(credsPath);
            if (null == is) {
                throw new IllegalArgumentException("Failed to load creds: " + credsPath);
            }
            final AWSCredentials creds;
            try {
                creds = new PropertiesCredentials(is);
            } finally {
                is.close();
            }
            final AmazonSimpleDB db = new AmazonSimpleDBClient(creds);
            binder.bind(AmazonSimpleDB.class).toInstance(db);

            final String domain;
            if (AwsModule.dbDomainOverride.isSet()) {
                domain = AwsModule.dbDomainOverride.get();
            } else {
                domain = PropertiesLoader.get().loadProperties(AwsConfig.class).getProperty("domain", "littleware");
            }

            final AwsConfig config = new AwsConfig() {

                @Override
                public String getDbDomain() {
                    return domain;
                }
            };
            binder.bind(AwsConfig.class).toInstance(config);
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load AWS configuration", ex);
        }
    }
}

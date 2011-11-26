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
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.db.AbstractLittleTransaction;
import littleware.asset.server.db.DbAssetManager;
import littleware.asset.server.db.DbInitializer;
import littleware.base.Maybe;
import littleware.base.Option;
import littleware.base.PropertiesLoader;
import littleware.bootstrap.AppBootstrap;

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
        private boolean isDomainInitialized = false;
        private final DbAssetManager mgr;
        private final DbInitializer dbInit;

        @Inject
        public SetupHandler(AmazonSimpleDB db, AwsConfig config, AwsDbAssetManager mgr,
                DbInitializer dbInit
                ) {
            this.db = db;
            this.config = config;
            this.mgr = mgr;
            this.dbInit = dbInit;
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
            if (!db.listDomains().getDomainNames().contains(config.getDbDomain())) {
                db.createDomain(new CreateDomainRequest(config.getDbDomain()));
            }
            dbInit.initDB( mgr );
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

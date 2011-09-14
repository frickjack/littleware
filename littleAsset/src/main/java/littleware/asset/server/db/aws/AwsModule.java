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
import com.google.inject.Binder;
import com.google.inject.Scopes;
import java.io.IOException;
import java.io.InputStream;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.asset.server.LittleTransaction;
import littleware.asset.server.bootstrap.AbstractServerModule;
import littleware.asset.server.db.AbstractLittleTransaction;
import littleware.asset.server.db.DbAssetManager;
import littleware.bootstrap.AppBootstrap;

/**
 * Configuration module for AWS database backend 
 */
public class AwsModule extends AbstractServerModule {

    private static final Logger log = Logger.getLogger(AwsModule.class.getName());

    public AwsModule(AppBootstrap.AppProfile profile) {
        super(profile);
    }

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

    @Override
    public void configure(Binder binder) {
        log.log(Level.FINE, "Configuring AWS database access");
        binder.bind(LittleTransaction.class).to(AwsTransaction.class);
        binder.bind(DbAssetManager.class).to(AwsDbAssetManager.class).in(Scopes.SINGLETON);
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
            final AmazonSimpleDB  db = new AmazonSimpleDBClient( creds );
            binder.bind( AmazonSimpleDB.class ).toInstance( db );
        } catch (IOException ex) {
            throw new IllegalStateException("Failed to load AWS credentials", ex);
        }
    }
}

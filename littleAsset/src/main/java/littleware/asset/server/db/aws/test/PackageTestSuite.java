/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.asset.server.db.aws.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestSuite;
import littleware.asset.server.db.aws.AwsModule;
import littleware.asset.server.db.test.DbAssetManagerTester;
import littleware.base.Options;
import littleware.bootstrap.AppBootstrap;
import littleware.bootstrap.AppBootstrap.AppProfile;
import littleware.bootstrap.AppModule;
import littleware.bootstrap.AppModuleFactory;
import littleware.test.TestFactory;

/**
 * AWS db package tests
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );
    
    @Inject
    public PackageTestSuite(
            AwsConnectTester connectTester,
            Provider<DbAssetManagerTester> mgrTestFactory ) {
        setName(getClass().getName());
        this.addTest(connectTester);
        this.addTest( mgrTestFactory.get() );
        this.addTest( mgrTestFactory.get().putName( "testCreateUpdateDelete" ) );
        this.addTest( mgrTestFactory.get().putName( "testHomeIdsQuery" ) );
        this.addTest( mgrTestFactory.get().putName( "testByNameQuery" ) );
    }

    /**
     * Bootstrap test suite outside of an asset server environment
     */
    public static TestSuite suite() {
        try {
            AwsModule.dbDomainOverride = Options.some( "littleTestDomain" );
            final AppBootstrap boot = AppBootstrap.appProvider.get().addModuleFactory(
                    new AppModuleFactory() {

                        @Override
                        public AppModule build(AppProfile profile) {
                            return new AwsModule(profile);
                        }
                    }).build();

            return (new TestFactory()).build(boot, PackageTestSuite.class);
        } catch ( RuntimeException ex) {
            log.log(Level.WARNING, "Failed test bootstrap", ex);
            throw ex;
        }
    }
}

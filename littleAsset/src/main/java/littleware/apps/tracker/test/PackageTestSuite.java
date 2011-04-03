/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.tracker.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.asset.client.bootstrap.ClientBootstrap;
import littleware.asset.server.bootstrap.ServerBootstrap;
import littleware.asset.test.AssetTestFactory;

/**
 * Test the apps.tracker packages
 */
public class PackageTestSuite extends TestSuite {

    private static final Logger log = Logger.getLogger(PackageTestSuite.class.getName());

    @Inject
    public PackageTestSuite(
            Provider<QueryManagerTester> provideQueryTester,
            Provider<ProductSetupTester> provideProductTester,
            Provider<ZipUtilTester> provideZipTester,
            Provider<ProductManagerTester> providePMTester,
            Provider<CommentTester> provideCommentTester
            ) {
        super(PackageTestSuite.class.getName());
        boolean bRun = true;
        if ( bRun ) {
            this.addTest( provideCommentTester.get() );
        }
        if (bRun) {
            this.addTest(provideQueryTester.get());
        }
        if (bRun) {
            this.addTest(provideProductTester.get());
        }
        if (bRun) {
            this.addTest( provideZipTester.get() );
        }
        if (bRun) {
            this.addTest( providePMTester.get() );
        }
    }

    public static Test suite() {
        try {
            return (new AssetTestFactory()).build(ServerBootstrap.provider.get().build(),
                    ClientBootstrap.clientProvider.get().build(),
                    PackageTestSuite.class);
        } catch (RuntimeException ex) {
            log.log(Level.SEVERE, "Test setup failed", ex);
            throw ex;
        }
    }

    public static void main(String[] ignore) {
        final String[] args = new String[]{"-noloading", PackageTestSuite.class.getName()};
        junit.swingui.TestRunner.main(args);
    }
}

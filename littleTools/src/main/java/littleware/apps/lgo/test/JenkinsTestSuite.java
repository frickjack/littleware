/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.lgo.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.asset.client.test.AssetTestFactory;
import littleware.bootstrap.AppBootstrap;

/**
 * littleware.apps.lgo package test suite safe for run in
 * Hudson server environment
 */
public class JenkinsTestSuite extends TestSuite {

    @Inject
    public JenkinsTestSuite(
            Provider<DeleteAssetTester> factoryDeleteTester,
            Provider<ListChildrenTester> factoryChildrenTester,
            Provider<GetAssetTester> factoryGetTester,
            Provider<CreateFolderTester> factoryCreateTester,
            Provider<GetByNameTester> factoryByNameTester,
            Provider<RootPathCommandTest> factoryRootPathTest,
            Provider<GsonTester> provideGsonTester) {
        super(JenkinsTestSuite.class.getName());
        final boolean go = true;
        if (go) {
            this.addTest(factoryRootPathTest.get());
        }
        if (go) {
            this.addTest(factoryDeleteTester.get());
            this.addTest(factoryChildrenTester.get());
            this.addTest(factoryGetTester.get());
            this.addTest(factoryCreateTester.get());
        }
        if (go) {
            this.addTest(factoryByNameTester.get());
        }
        if (go) {
            this.addTest(provideGsonTester.get());
        }
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        return (new AssetTestFactory()).build(
                AppBootstrap.appProvider.get().build(),
                JenkinsTestSuite.class);
    }
}

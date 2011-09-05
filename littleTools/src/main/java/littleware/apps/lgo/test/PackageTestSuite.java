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
import littleware.asset.client.test.AssetTestFactory;
import littleware.asset.server.bootstrap.ServerBootstrap;

/**
 * littleware.apps.lgo package test suite
 */
public class PackageTestSuite extends JenkinsTestSuite {

    @Inject
    public PackageTestSuite( 
            Provider<DeleteAssetTester> factoryDeleteTester,
            Provider<ListChildrenTester> factoryChildrenTester,
            Provider<GetAssetTester> factoryGetTester,
            Provider<CreateFolderTester> factoryCreateTester,
            Provider<GetByNameTester> factoryByNameTester,
            Provider<RootPathCommandTest> factoryRootPathTest,
            Provider<GsonTester> provideGsonTester
            )
    {
        super( factoryDeleteTester,
                factoryChildrenTester, factoryGetTester,
                factoryCreateTester, factoryByNameTester,
                factoryRootPathTest, provideGsonTester
                );
        setName( PackageTestSuite.class.getName() );

    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        return (new AssetTestFactory()).build(
                ServerBootstrap.provider.get().build(),
                PackageTestSuite.class
                );
    }

    public static void main( String[] argv ) {
        junit.swingui.TestRunner.main( new String[] { "-noloading", PackageTestSuite.class.getName() } );
    }

}

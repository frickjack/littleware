/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import junit.framework.Test;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.bootstrap.server.ServerBootstrap;
import littleware.test.TestFactory;

/**
 * littleware.apps.lgo package test suite
 */
public class PackageTestSuite extends HudsonTestSuite {

    @Inject
    public PackageTestSuite( 
            Provider<DeleteAssetTester> factoryDeleteTester,
            Provider<SetImageTester> factoryImageTester,
            Provider<ListChildrenTester> factoryChildrenTester,
            Provider<GetAssetTester> factoryGetTester,
            Provider<CreateFolderTester> factoryCreateTester,
            Provider<GetByNameTester> factoryByNameTester,
            Provider<BrowserCommandTest> factoryBrowserTest,
            Provider<RootPathCommandTest> factoryRootPathTest,
            Provider<GsonTester> provideGsonTester
            )
    {
        super( factoryDeleteTester,
                factoryImageTester,
                factoryChildrenTester, factoryGetTester,
                factoryCreateTester, factoryByNameTester,
                factoryRootPathTest, provideGsonTester
                );
        setName( PackageTestSuite.class.getName() );

        if ( true ) {
            this.addTest( factoryBrowserTest.get() );
        }
    }

    /**
     * Return the singleton set by the constructor as part
     * of the OSGi bootstrap process
     */
    public static Test suite() {
        return (new TestFactory()).build(
                ServerBootstrap.provider.get().profile(ServerBootstrap.ServerProfile.Standalone).build(),
                ClientBootstrap.clientProvider.get().build(),
                PackageTestSuite.class
                );
    }

    public static void main( String[] argv ) {
        junit.swingui.TestRunner.main( new String[] { "-noloading", PackageTestSuite.class.getName() } );
    }

}

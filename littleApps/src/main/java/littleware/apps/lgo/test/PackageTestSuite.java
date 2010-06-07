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
import littleware.apps.client.ClientSyncModule;
import littleware.test.TestFactory;

/**
 * littleware.apps.lgo package test suite
 */
public class PackageTestSuite extends HudsonTestSuite {

    @Inject
    public PackageTestSuite( Provider<ArgParserTester> factoryArgTester,
            Provider<DeleteAssetTester> factoryDeleteTester,
            Provider<XmlLgoHelpTester> factoryXmlHelpTester,
            Provider<SetImageTester> factoryImageTester,
            Provider<ListChildrenTester> factoryChildrenTester,
            Provider<GetAssetTester> factoryGetTester,
            Provider<CreateFolderTester> factoryCreateTester,
            Provider<GetByNameTester> factoryByNameTester,
            Provider<BrowserCommandTest> factoryBrowserTest,
            Provider<RootPathCommandTest> factoryRootPathTest,
            Provider<LgoServerTester> factoryServerTest,
            Provider<GsonTester> provideGsonTester
            )
    {
        super( factoryArgTester, factoryDeleteTester,
                factoryXmlHelpTester, factoryImageTester,
                factoryChildrenTester, factoryGetTester,
                factoryCreateTester, factoryByNameTester,
                factoryRootPathTest, factoryServerTest, provideGsonTester
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
        return (new TestFactory()).build( new ClientSyncModule(), PackageTestSuite.class );
    }

}

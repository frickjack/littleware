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
import junit.framework.TestSuite;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.test.TestFactory;

/**
 * littleware.apps.lgo package test suite safe for run in
 * Hudson server environment
 */
public class HudsonTestSuite extends TestSuite {

    @Inject
    public HudsonTestSuite(Provider<ArgParserTester> factoryArgTester,
            Provider<DeleteAssetTester> factoryDeleteTester,
            Provider<XmlLgoHelpTester> factoryXmlHelpTester,
            Provider<SetImageTester> factoryImageTester,
            Provider<ListChildrenTester> factoryChildrenTester,
            Provider<GetAssetTester> factoryGetTester,
            Provider<CreateFolderTester> factoryCreateTester,
            Provider<GetByNameTester> factoryByNameTester,
            Provider<RootPathCommandTest> factoryRootPathTest,
            Provider<GsonTester> provideGsonTester) {
        super(HudsonTestSuite.class.getName());
        final boolean go = true;
        if (go) {
            this.addTest(factoryRootPathTest.get());
        }
        if (go) {
            this.addTest(factoryArgTester.get());
            this.addTest(factoryDeleteTester.get());
            this.addTest(factoryXmlHelpTester.get());
            this.addTest(factoryImageTester.get());
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
        return (new TestFactory()).build(
                ClientBootstrap.clientProvider.get().build().test(),
                HudsonTestSuite.class);
    }
}

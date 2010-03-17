/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.apps.swingbase.test;

import com.google.inject.Inject;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;
import junit.framework.Test;
import junit.framework.TestSuite;
import littleware.apps.client.NullBootstrap;
import littleware.apps.swingbase.SwingBaseGuice;
import littleware.test.TestFactory;

public class PackageTestSuite extends TestSuite {

    @Inject
    public PackageTestSuite(SwingBaseTester baseTester) {
        this.addTest(baseTester);
    }

    public static Test suite() {
        final NullBootstrap boot = new NullBootstrap();
        try {
            boot.getGuiceModule().add(new SwingBaseGuice("regressionTest", "v0.0",
                    new URL("http://frickjack.com"), new Properties()));
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("URL exception", ex);
        }
        return (new TestFactory()).build(boot, PackageTestSuite.class);
    }
}

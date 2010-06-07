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
import littleware.apps.client.ExecutorModule;
import littleware.apps.swingbase.SwingBaseActivator;
import littleware.apps.swingbase.SwingBaseGuice;
import littleware.test.TestFactory;

public class PackageTestSuite extends TestSuite {

    @Inject
    public PackageTestSuite(SwingBaseTester baseTester,
            BaseToolTester toolTester
            ) {
        this.addTest( toolTester );
        this.addTest(baseTester);
    }

    public static Test suite() {
        final ExecutorModule boot = new ExecutorModule();
        try {
            final Properties props = new Properties();
            props.put("testProp", "bla bla bla" );
            boot.getGuiceModule().add(
                    new SwingBaseGuice("regressionTest", "v0.0",
                    new URL("http://code.google.com/p/littleware/"), props
                    )
                    );
            boot.getOSGiActivator().add( SwingBaseActivator.class );
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("URL exception", ex);
        }
        return (new TestFactory()).build(boot, PackageTestSuite.class);
    }
}

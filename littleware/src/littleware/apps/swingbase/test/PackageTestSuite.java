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
import littleware.apps.swingbase.SwingBaseModule;
import littleware.bootstrap.AppBootstrap;
import littleware.test.TestFactory;

public class PackageTestSuite extends TestSuite {

    @Inject
    public PackageTestSuite(SwingBaseTester baseTester,
            BaseToolTester toolTester
            ) {
        setName( getClass().getName() );
        this.addTest( toolTester );
        this.addTest(baseTester);
    }

    /**
     * Register the SwingBaseModule with the app-bootstrap builder ready
     * to run the regression tests.
     * 
     * @param builder to register with
     * @return builder with SwingModule added
     */
    public static AppBootstrap.AppBuilder registerSwingBase( AppBootstrap.AppBuilder builder ) {
        try {
            final Properties props = new Properties();
            props.put("testProp", "bla bla bla" );
            return builder.addModuleFactory(
                    (new SwingBaseModule.Factory()).appName( "regressionTest"
                    ).version( "v0.0"
                    ).helpUrl(new URL("http://code.google.com/p/littleware/")
                    ).properties( props )
                    );
        } catch (MalformedURLException ex) {
            throw new IllegalStateException("URL exception", ex);
        }
    }

    public static Test suite() {
        final AppBootstrap.AppBuilder bootBuilder = registerSwingBase( AppBootstrap.appProvider.get() );
        return (new TestFactory()).build(
                    bootBuilder.profile(AppBootstrap.AppProfile.SwingApp).build(),
                    PackageTestSuite.class
                    );
    }
}

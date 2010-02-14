/*
 * Copyright 2007-2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;


/**
 * Just little utility class that packages up a test suite
 * for the littleware.web package.
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    @Inject
    public PackageTestSuite ( Provider<BeanTester> provideBeanTester,
            Provider<BrowserTypeTester> provideBrowserTypeTester,
            Provider<ThumbServletTester> provideThumbServTester,
            Provider<LgoServletTester> provideLgoServTester
            ) {
        super( PackageTestSuite.class.getName() );

        log.log(Level.INFO, "Trying to setup littleware.web test suite");

        // This should get the SimpleSessionManager up and listening on the default port
        boolean b_run = true;

        if (b_run) {
            this.addTest(provideBeanTester.get());
        }
        if (b_run) {
            this.addTest( provideBrowserTypeTester.get() );
        }
        if ( b_run ) {
            this.addTest( provideThumbServTester.get() );
        }
        if ( b_run ) {
            this.addTest( provideLgoServTester.get() );
        }

        log.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
}

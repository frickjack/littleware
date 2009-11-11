/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.web.test;

import java.util.logging.Logger;
import java.util.logging.Level;
import junit.framework.*;
import littleware.web.*;


/**
 * Just little utility class that packages up a test suite
 * for the littleware.web package.
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger olog = Logger.getLogger( PackageTestSuite.class.getName() );

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    public PackageTestSuite () {
        super( PackageTestSuite.class.getName() );

        olog.log(Level.INFO, "Trying to setup littleware.web test suite");

        // This should get the SimpleSessionManager up and listening on the default port
        boolean b_run = true;

        if (b_run) {
            this.addTest(new BeanTester("testLoginBean"));
        }
        if (b_run) {
            this.addTest(new BeanTester("testDefaultsBean"));
        }
        if (b_run) {
            this.addTest(new BeanTester("testNewUserBean"));
        }
        if ( false ) {
            this.addTest(new BeanTester("testNewUserEmail"));
        }
        if (b_run) {
            this.addTest(new BeanTester("testBasicSession"));
        }
        if (b_run) {
            this.addTest(new BeanTester("testUpdateContactBean"));
        }
        if (b_run) {
            this.addTest(new BrowserTypeTester("testUserAgent"));
        }

        olog.log(Level.INFO, "PackageTestSuite.suite () returning ok ...");
    }
}

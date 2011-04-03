/*
 * Copyright 2007-2008 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.db.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.util.logging.Logger;

import junit.framework.*;


/**
 * Just little utility class that packages up a test suite
 * for the littleware.db package.
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger log = Logger.getLogger( PackageTestSuite.class.getName() );

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    @Inject
    public PackageTestSuite( Provider<ConnectionFactoryTester> provideFactoryTester ) {
        super( PackageTestSuite.class.getName() );

        this.addTest( provideFactoryTester.get() );
        this.addTest( provideFactoryTester.get().putName("testProxy"));
    }


}



/*
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS HEADER.
 *
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
import com.google.inject.name.Named;
import java.util.logging.Logger;

import javax.sql.DataSource;
import junit.framework.*;


/**
 * Just little utility class that packages up a test suite
 * for the littleware.db package.
 */
public class PackageTestSuite extends TestSuite {
    private static final Logger olog = Logger.getLogger( PackageTestSuite.class.getName() );

    /**
     * Setup a test suite to exercise this package -
     * junit.swingui.TestRunner looks for this.
     */
    @Inject
    public PackageTestSuite( @Named( "datasource.littleware" ) DataSource dsource ) {
        super( PackageTestSuite.class.getName() );

        this.addTest(new ConnectionFactoryTester("testQuery", dsource, "SELECT 'Hello'"));
    }

}



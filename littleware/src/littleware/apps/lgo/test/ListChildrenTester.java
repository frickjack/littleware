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
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.client.LoggerUiFeedback;
import littleware.apps.lgo.LgoException;
import littleware.apps.lgo.ListChildrenCommand;
import littleware.test.LittleTest;

/**
 * Test lgo.ListChildrenCommand - just get the children under littleware.test_home
 */
public class ListChildrenTester extends LittleTest {
    private static Logger olog = Logger.getLogger( ListChildrenTester.class.getName() );

    private final ListChildrenCommand ocomTest;

    @Inject
    ListChildrenTester( ListChildrenCommand comTest ) {
        setName( "testListChildren" );
        ocomTest = comTest;
    }

    public void testListChildren() {
        try {
            final String sResult = ocomTest.runCommandLine( new LoggerUiFeedback(), getTestHome() );
            olog.log( Level.INFO, "List children under " + getTestHome() + " + got: " + sResult );
            assertTrue( "Found some children under " + getTestHome(),
                    sResult.split( "\n" ).length > 1
                    );
        } catch ( LgoException ex ) {
            olog.log( Level.WARNING, "Failed test", ex );
            assertTrue( "Caught exception: " + ex, false );
        }
    }
}

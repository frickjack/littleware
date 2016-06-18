/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import littleware.base.Option;
import littleware.base.Whatever;
import littleware.test.LittleTest;

/**
 * Test the Whatever utility methods
 */
public class WhateverTester extends LittleTest {

    public WhateverTester() {
        setName( "testWhatever" );
    }

    public enum TestEnum { Uga, BooGa, GooGoo, Ga };

    public void testWhatever() {
        final Option<TestEnum> maybe = Whatever.get().findEnumIgnoreCase("booga", TestEnum.values() );
        assertTrue( "findEnumIgnoreCase ok", maybe.isSet() && maybe.get().equals( TestEnum.BooGa ) );
        assertTrue( "findEnumIgnoreCase found empty", Whatever.get().findEnumIgnoreCase("frick", TestEnum.values() ).isEmpty() );
        assertTrue( "equalsSafe ok", Whatever.get().equalsSafe(null, null) && (! Whatever.get().equalsSafe( "bla", null ) ) );
        assertTrue( "empty test ok", Whatever.get().empty(null) && Whatever.get().empty( "" ) && (! Whatever.get().empty("foo")));

        for( Whatever.Folder folder : Whatever.Folder.values() ) {
            assertTrue( "Folder exists " + folder + ": " + folder.getFolder(),
                    folder.getFolder().exists()
                    );
        }
    }
}

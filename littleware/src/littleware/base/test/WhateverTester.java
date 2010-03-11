/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import littleware.base.Maybe;
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
        Maybe<TestEnum> maybe = Whatever.get().findEnumIgnoreCase("booga", TestEnum.values() );
        assertTrue( "findEnumIgnoreCase ok", maybe.isSet() && maybe.get().equals( TestEnum.BooGa ) );
        assertTrue( "findEnumIgnoreCase found empty", Whatever.get().findEnumIgnoreCase("frick", TestEnum.values() ).isEmpty() );
        assertTrue( "equalsSafe ok", Whatever.get().equalsSafe(null, null) && (! Whatever.get().equalsSafe( "bla", null ) ) );
        assertTrue( "empty test ok", Whatever.get().empty(null) && Whatever.get().empty( "" ) && (! Whatever.get().empty("foo")));
    }
}

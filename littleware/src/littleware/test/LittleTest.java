/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.test;

import junit.framework.TestCase;

/**
 * Slight specialization of junit.framework.TestCase
 * adds putName method to allow simultaneously setting
 * a TestCase test-method name and register the test-case
 * with a suite:  suite.addTest ( provider.get().putName( "testWhatever" ) )
 */
public abstract class LittleTest extends TestCase {

    /**
     * Call setName(s_name) and return this
     * @param s_name of test-method to run
     * @return this
     */
    public LittleTest putName(String s_name) {
        setName(s_name);
        return this;
    }

    /**
     * Extension function for TestCase instances not
     * derived from LittleTest.
     * 
     * @param test to setName( s_name ) on
     * @param s_name to assign to test.setName( s_name )
     * @return test after setName( s_name ) call
     */
    public static TestCase putName(TestCase test, String s_name) {
        test.setName(s_name);
        return test;
    }

    /**
     * Make public to simplify delegation and decoration
     */
    @Override
    public void setUp() throws Exception {
        super.setUp();
    }

    /**
     * Make public to simplify delegation and decoration
     */
    @Override
    public void tearDown() throws Exception {
        super.tearDown();
    }

    /**
     * Make public to simplify delegation and decoration
     */
    @Override
    public void runTest() throws Throwable {
        super.runTest();
    }
}

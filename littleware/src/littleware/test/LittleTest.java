/*
 * Copyright 2011 http://code.google.com/p/littleware
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.test;

import junit.framework.TestCase;
import junit.framework.TestResult;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Slight specialization of junit.framework.TestCase
 * adds putName method to allow simultaneously setting
 * a TestCase test-method name and register the test-case
 * with a suite:  suite.addTest ( provider.get().putName( "testWhatever" ) )
 */
public abstract class LittleTest extends TestCase {
    /** Every test wants a logger */
    public final Logger log = Logger.getLogger( getClass().getName() );
    
    /**
     * Typical exception handler
     */
     public void handle( Exception ex ) {
         log.log( Level.WARNING, "Failed test", ex );
         fail( "Caught: " + ex );
     }
    
    /**
     * Call setName(name) and return this
     * @param name of test-method to run
     * @return this
     */
    public final LittleTest putName(String name) {
        setName(name);
        return this;
    }
    /** Alias for putName */
    public final LittleTest withName( String name ) { return putName( name ); }

    /**
     * Extension function for TestCase instances not
     * derived from LittleTest.
     * 
     * @param test to setName( name ) on
     * @param name to assign to test.setName( name )
     * @return test after setName( name ) call
     */
    public static TestCase putName(TestCase test, String name) {
        test.setName(name);
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

    /**
     * Make public to simplify delegation and decoration
     */
     @Override
     public TestResult createResult() {
         return super.createResult();
     }
}

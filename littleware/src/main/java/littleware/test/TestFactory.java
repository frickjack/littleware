/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.bootstrap.LittleBootstrap;



/**
 * Utility to setup TestSuite that can bootstrap
 * a littleware environment in various configurations.
 */
public class TestFactory {

    private static final Logger log = Logger.getLogger(TestFactory.class.getName());


    /**
     * Bootstrap a littleware environment, and
     * return a test suite ready to run against that
     * environment that shuts down
     * the environment in the last test.
     */
    public <T> T build(final LittleBootstrap bootstrap,
            final Class<T> testSuiteClass) {
        try {
            final T suite = bootstrap.bootstrap( testSuiteClass );
            /*
            suite.addTest(new TestCase( "shutdownTest" ) {
                @Override
                public void runTest() {
                    bootstrap.shutdown();
                }
            });
            */
            log.log(Level.INFO, "Returning TestSuite");
            return suite;
        } catch (Exception ex) {
            log.log(Level.SEVERE, "TestFactory failed", ex);
            throw new IllegalStateException("Test setup interrupted", ex);
        }
    }

}

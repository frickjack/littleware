/*
 * Copyright 2009 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.demo.simpleCL;

import com.google.inject.Inject;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.test.LittleTest;

/**
 * Test the SimpleCL app
 */
public class SimpleCLTester extends LittleTest {
    private static final Logger log = Logger.getLogger( SimpleCLTester.class.getName() );
    private final SimpleCLBuilder builder;

    @Inject
    public SimpleCLTester ( SimpleCLBuilder builder ) {
        this.builder = builder;
        setName( "testSimpleCL" );
    }

    public void testSimpleCL() {
        try {
            final String result = builder.argv( Arrays.asList( "/littleware.home" ) ).
                    build().call();
            log.log( Level.INFO, "SimpleCL result: " + result );
            assertTrue( "littleware.home/ includes Users in children",
                    result.indexOf( "Users" ) > 0 
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

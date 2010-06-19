/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.base.test;

import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import littleware.base.PropertiesLoader;

/**
 *
 * @author pasquini
 */
public class PropLoaderTester extends TestCase {
    private final Logger log = Logger.getLogger( PropLoaderTester.class.getName() );

    public PropLoaderTester() {
        super( "testPropLoader" );
    }

    public void testPropLoader() {
        final PropertiesLoader loader = PropertiesLoader.get();
        try {
            final String path = loader.classToResourcePath(getClass() );
            assertTrue( "Got expected resource path:" + path,
                    path.equals( "littleware/base/test/PropLoaderTester.properties" )
                    );
            final Properties props = loader.loadProperties( getClass() );
            assertTrue( "Test property set has one member", props.size() == 1 );
            final String testProp1 = props.getProperty( "testProp1", "ugh!" );
            assertTrue( "Property has expected value: " + testProp1,
                    testProp1.equals( "blaBlaBla" )
                    );
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Test failed", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

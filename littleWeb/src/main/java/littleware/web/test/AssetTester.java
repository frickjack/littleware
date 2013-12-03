/*
 * Copyright 2013 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.web.test;

import littleware.web.servlet.asset.AssetMgrServlet.ETagInfo;

/**
 * Some tests for the AssetMgrServlet
 */
public class AssetTester extends littleware.test.LittleTest {
    {
        setName( "testEtagParser" );
    }
    
    
    public void testEtagParser() {
        try {
            final ETagInfo info1 = ETagInfo.parse( "12345");
            assertTrue( "Got expected ETagInfo 1: " + info1, 
                    info1.cacheTimestamp == 12345L &&
                    info1.sizeInCache == 0
                    );
            final ETagInfo info2 = ETagInfo.parse( "54321-3" );
            assertTrue( "Got expected ETagInfo 2: " + info2,
                    info2.cacheTimestamp == 54321L &&
                    info2.sizeInCache == 3
                    );
        } catch ( Exception ex ) {
            handle( ex );
        }
    }
}

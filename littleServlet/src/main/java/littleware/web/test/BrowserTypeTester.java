/*
 * Copyright 2007-2010 Reuben Pasquini All rights reserved.
 *
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.web.test;

import java.util.*;

import java.util.logging.Logger;
import java.util.logging.Level;


import littleware.test.LittleTest;
import littleware.web.beans.BrowserType;



/**
 * TestFixture instantiates different littleware.web.beans beans,
 * and exercises them a bit.
 */
public class BrowserTypeTester extends LittleTest {

    private static final Logger log = Logger.getLogger(BrowserTypeTester.class.getName());

    /**
     * Do nothing constructor
     */
    public BrowserTypeTester() {
        setName( "testUserAgent" );
    }

    private static Map<BrowserType, String> browserAgentMap = new EnumMap<BrowserType, String>(BrowserType.class);

    static {
        browserAgentMap.put(BrowserType.WEBKIT41x,
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/418.9.1 (KHTML, like Gecko) Safari/419.3");
        browserAgentMap.put(BrowserType.WEBKIT42x,
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X; en) AppleWebKit/420+ (KHTML, like Gecko) Safari/419.3");

        browserAgentMap.put(BrowserType.IE7,
                "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1; .NET CLR 1.1.4322)");
        browserAgentMap.put(BrowserType.IE6,
                "Mozilla/4.0 (compatible; MSIE 6.0; Windows NT 5.1; .NET CLR 1.1.4322)");
        browserAgentMap.put(BrowserType.FIREFOX2,
                "Mozilla/5.0 (Macintosh; U; PPC Mac OS X Mach-O; en-US; rv:1.8.1) Gecko/20061010 Firefox/2.0");
    }

    /**
     * Test BrowserType.getBrowserFromUserAgent ()
     */
    public void testUserAgent() {
        for (Map.Entry<BrowserType, String> map_entry : browserAgentMap.entrySet()) {
            log.log(Level.INFO, "Testing agent: " + map_entry.getValue());
            BrowserType n_result = BrowserType.getBrowserFromAgent(map_entry.getValue());
            assertTrue("Agent should be: " + map_entry.getKey() + ", got " + n_result
                    + " from: " + map_entry.getValue(),
                    map_entry.getKey().equals(n_result));
        }
    }
}


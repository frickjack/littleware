/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.bootstrap.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import junit.framework.TestCase;
import littleware.bootstrap.client.ClientBootstrap;
import littleware.bootstrap.server.ServerBootstrap;

public class BootstrapTester extends TestCase {

    private static final Logger log = Logger.getLogger(BootstrapTester.class.getName());

    public BootstrapTester() {
        super("testModuleLoad");
    }

    /**
     * Just simple test to verify that ClientBootstrap
     * and ServerBootstrap load some modules
     */
    public void testModuleLoad() {
        try {
            assertTrue("Found a server module",
                    !ServerBootstrap.provider.get().build().getModuleSet().isEmpty()
                    );
            assertTrue("Found a client module",
                    !ClientBootstrap.clientProvider.get().getModuleSet().isEmpty()
                    );

        } catch (Exception ex) {
            log.log(Level.WARNING, "Test failed", ex);
            fail("Caught exception: " + ex);
        }
    }
}

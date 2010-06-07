/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.lgo.test;

import com.google.inject.Inject;
import com.meterware.httpunit.WebConversation;
import com.meterware.httpunit.WebResponse;
import java.util.logging.Level;
import java.util.logging.Logger;
import littleware.apps.lgo.JettyServerBuilder;
import littleware.apps.lgo.LgoServer;
import littleware.apps.lgo.LgoServer.ServerBuilder;
import littleware.test.LittleTest;

/**
 * Test the embedded LgoServer
 */
public class LgoServerTester extends LittleTest {
    private static final Logger log = Logger.getLogger( LgoServerTester.class.getName() );
    private final ServerBuilder serverBuilder;

    @Inject
    public LgoServerTester( LgoServer.ServerBuilder serverBuilder ) {
        this.serverBuilder = serverBuilder;
        this.setName( "testLgoServer" );
    }

    public void testLgoServer() {
        final LgoServer    server = serverBuilder.launch();
        final WebConversation wc = new WebConversation();
        try {
            final String urlString = "http://localhost:" +
                    JettyServerBuilder.serverPort + "/n9n/lgo/ls?path=littleware.test_home";
            final WebResponse response = wc.getResponse( urlString );
            final String      data = response.getText();
            log.log( Level.INFO, "Response from " + urlString + " -- " + data );
            assertTrue( "Server response includes data block",
                    data.matches( "(?s).*<data>.+</data>.*" )
                    );
            try {
                // Verify that cannot launch 2 servers on same port
                serverBuilder.launch();
                fail( "Second server launch succeeded - should have failed" );
            } catch ( Exception ex ) {
            }
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "Test failed", ex );
            fail( "Caught exception: " + ex );
        } finally {
            server.shutdown();
        }
    }
}

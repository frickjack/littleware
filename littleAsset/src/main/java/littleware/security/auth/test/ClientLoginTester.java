/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.test;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import junit.framework.TestCase;
import littleware.security.auth.SessionHelper;
import littleware.security.auth.SimpleCallbackHandler;
import littleware.security.auth.client.ClientLoginModule;

/**
 * Test ClientLoginModule
 */
public class ClientLoginTester extends TestCase {

    private static final Logger log = Logger.getLogger(ClientLoginTester.class.getName());

    public ClientLoginTester() {
        super("testClientLogin");
    }

    public void testClientLogin() {
        try {
            final Configuration config = ClientLoginModule.newBuilder().build();
            final LoginContext context = new LoginContext( "littleware.login",
                    new Subject(),
                    new SimpleCallbackHandler( "littleware.test_user", "password" ),
                    config
                    );
            context.login();
            assertTrue( "Authenticated subject includes SessionHelper in credentials",
                    ! context.getSubject().getPrivateCredentials( SessionHelper.class ).isEmpty()
                    );
        } catch (Exception ex) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

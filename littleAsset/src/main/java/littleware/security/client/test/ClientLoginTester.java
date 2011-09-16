/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.client.test;

import com.google.inject.Inject;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.LoginContext;
import junit.framework.TestCase;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.security.auth.LittleSession;
import littleware.base.LoginCallbackHandler;
import littleware.security.auth.client.ClientLoginModule;
import littleware.security.auth.client.ClientLoginModule.ConfigurationBuilder;

/**
 * Test ClientLoginModule
 */
public class ClientLoginTester extends TestCase {

    private static final Logger log = Logger.getLogger(ClientLoginTester.class.getName());
    private final ConfigurationBuilder configBuilder;

    @Inject
    public ClientLoginTester( ClientLoginModule.ConfigurationBuilder configBuilder ) {
        super("testClientLogin");
        this.configBuilder = configBuilder;
    }

    public void testClientLogin() {
        try {
            final LoginContext context = new LoginContext( "littleware.login",
                    new Subject(),
                    new LoginCallbackHandler( AbstractAssetTest.getTestUserName(), "password" ),
                    configBuilder.build()
                    );
            context.login();
            assertTrue( "Authenticated subject includes LittleSession in credentials",
                    ! context.getSubject().getPrivateCredentials( LittleSession.class ).isEmpty()
                    );
        } catch (Exception ex) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

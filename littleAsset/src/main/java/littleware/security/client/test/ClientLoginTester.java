/*
 * Copyright 2011 http://code.google.com/p/littleware/
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.client.test;

import com.google.inject.Inject;
import com.google.inject.Provider;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.login.Configuration;
import javax.security.auth.login.LoginContext;
import junit.framework.Assert;
import junit.framework.TestCase;
import littleware.asset.client.test.AbstractAssetTest;
import littleware.security.auth.LittleSession;
import littleware.base.LoginCallbackHandler;
import littleware.security.auth.client.KeyChain;

/**
 * Test ClientLoginModule
 */
public class ClientLoginTester extends TestCase {

    private static final Logger log = Logger.getLogger(ClientLoginTester.class.getName());
    private final Provider<javax.security.auth.login.Configuration> configProvider;
    private final KeyChain keychain;

    @Inject
    public ClientLoginTester( Provider<Configuration> configProvider, KeyChain keychain ) {
        super("testClientLogin");
        this.configProvider = configProvider;
        this.keychain = keychain;
    }

    public static class Listener implements PropertyChangeListener {
        public boolean eventFired = false;
        
        @Override
        public void propertyChange(PropertyChangeEvent evt) {
            Assert.assertTrue( "Got expected event: " + evt.getPropertyName(), evt.getPropertyName().equals( "defaultSessionId" ));
            eventFired = true;
        }
        
    }
    
    public void testClientLogin() {
        try {
            final LoginContext context = new LoginContext( "littleware.login",
                    new Subject(),
                    new LoginCallbackHandler( AbstractAssetTest.getTestUserName(), "password" ),
                    configProvider.get()
                    );
            final Listener listener = new Listener();
            try {
                keychain.addPropertyChangeListener( listener );
                context.login();
            } finally {
               keychain.removePropertyChangeListener(listener);  
            }
            assertTrue( "Keychain fired property change on fresh login", listener.eventFired );
            assertTrue( "Authenticated subject includes LittleSession in credentials",
                    ! context.getSubject().getPublicCredentials( LittleSession.class ).isEmpty()
                    );
        } catch (Exception ex) {
            log.log( Level.WARNING, "Failed test", ex );
            fail( "Caught exception: " + ex );
        }
    }
}

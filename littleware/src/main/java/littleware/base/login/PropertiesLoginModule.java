/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.base.login;

import java.io.IOException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.spi.LoginModule;
import littleware.base.AssertionFailedException;

/**
 * LoginModule authenticates against user-name:password properties
 * file for littleware.base.login.PropertiesLoginModule.class 
 * loaded via PropertiesLoader
 */
public class PropertiesLoginModule implements LoginModule {
    private static final Logger log = Logger.getLogger( PropertiesLoginModule.class.getName () );
    private static final java.util.Properties loginProps;
    static {
        try {
            loginProps = littleware.base.PropertiesLoader.get().loadProperties( PropertiesLoginModule.class );
        } catch ( RuntimeException ex ) { throw ex;
        } catch ( Exception ex ) { 
            throw new littleware.base.AssertionFailedException( "Unexpected exception", ex );
        }
    }
    
    private Subject subject;
    private CallbackHandler callbackHandler;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler, Map<String, ?> sharedState, Map<String, ?> options) {
        this.subject = subject;
        this.callbackHandler = callbackHandler;
    }
    
    /**
     * Principal implementation specific to this module
     */
    public static class SimpleUser implements java.security.Principal {
        private final String name;
        
        public SimpleUser( String name ) {this.name = name;}
        
        @Override
        public String getName() { return name; }
    }

    @Override
    public boolean login() throws LoginException {
      final NameCallback     nameCallback = new NameCallback("Enter username" );
      final PasswordCallback passwordCallback = new PasswordCallback("Enter password", false);
        try {
            // Collect username and password via callbacks
            this.callbackHandler.handle( 
              new Callback[]{
                nameCallback,
                passwordCallback
              } );
        } catch (RuntimeException ex) { throw ex;
        } catch (Exception ex) {
            throw new AssertionFailedException( "Unexpected exception", ex );
        }

      final String name = nameCallback.getName();
      final String password = new String(passwordCallback.getPassword());
        
      final String expected = loginProps.getProperty( name );
      if ( null != expected && expected.equals( password ) ) {
          subject.getPrincipals().add( new SimpleUser( name ) );
          return true;
      }
      return false;
    }

    @Override
    public boolean commit() throws LoginException { return true; }

    @Override
    public boolean abort() throws LoginException { return true; }

    @Override
    public boolean logout() throws LoginException { return true; }
    
}

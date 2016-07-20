package littleware.base.login;

import java.util.Map;
import java.util.logging.Logger;
import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;

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
            throw new IllegalStateException( "Unexpected exception", ex );
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
            throw new IllegalStateException( "Unexpected exception", ex );
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

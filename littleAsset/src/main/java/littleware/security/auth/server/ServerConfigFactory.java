/*
 * Copyright 2011 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */
package littleware.security.auth.server;

import com.google.inject.Provider;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.security.auth.login.AppConfigurationEntry;
import javax.security.auth.login.Configuration;


/**
 * This is the login.Configuration source for the server-side SessionManager.
 * The SessionManager attempts to authentication with the "littleware.login"
 * JAAS login configuration entry.  If no entry exists for "littleware.login",
 * then SessionManager assumes authentication is not needed, and logs in
 * all users.  The SessionManager auto-creates a new littleware user object
 * if it does not already exist for any user that successfully authenticates.
 * 
 * At startup this class just calls Configuration.getConfiguration, 
 * so the client can use the standard JAAS config-file to register LDAP, file, SQL login modules.
 * An application may optionally override the default configuration by calling 
 * setConfiguration (note that Configuration.setConfiguration does not work in a 
 * J2EE container, so we manage our own Configuration here).
 * 
 * Littleware provides a few custom LoginModules.  The LittleLoginModule tests a password
 * against the password-hash stored with a LittleUser object.   
 * The TrustLoginModule allows some other in-memory mechanism to register a one-time secret
 * for authenticating a particular user.
 */
public class ServerConfigFactory implements Provider<Configuration> {
    private static final Logger log = Logger.getLogger( ServerConfigFactory.class.getName() );
    
    private Configuration config;
    {
        try {
            config = Configuration.getConfiguration();
        } catch ( Exception ex ) {
            log.log( Level.WARNING, "No default login configuration available ..." );
            config = new Configuration() {
                @Override
                public AppConfigurationEntry[] getAppConfigurationEntry(String name) {
                    return null;
                }
            };
        }
    }
    
    public void setConfig( Configuration value ) {
        this.config = value;
    }
    
    public Configuration getConfig() { return config; }
    @Override
    public final Configuration get() { return getConfig(); }
}

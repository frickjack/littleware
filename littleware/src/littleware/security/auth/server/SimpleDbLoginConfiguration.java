package littleware.security.auth;

import java.util.HashMap;
import javax.security.auth.login.*;

import littleware.security.*;

/**
 * Implementation of javax.security.auth.login.Configuration -
 * instance should be registered via
 *       Configuration.setConfiguration ()
 * at application startup time to enable the SimpleDbLoginModule
 * with the applicaiton LoginContext.
 * The SimpleDbLoginModule will expect to be able to pull
 * a database connection pool out of the global
 *     littleware.db.GuardedSqlResources resource bundle.
 */
public class SimpleDbLoginConfiguration extends javax.security.auth.login.Configuration {
	
	/**
	 * Default constructor - for now we require every littleware app
	 * to authenticate via the SimpleDbLoginModule.
	 * The application takes care of tracking the login-context and tickets
	 * over the session - the Login stuff just does the login protocol.
	 */
	public SimpleDbLoginConfiguration () {
		super ();
	}
	
	/**
	 * Always return a reference to the SimpleDbLoginModule
	 *
	 * @param s_application_name to get login-configuration info for
	 */
	public AppConfigurationEntry[] getAppConfigurationEntry ( String s_application_name ) {
		AppConfigurationEntry[] v_entry = new AppConfigurationEntry[ 1 ];
		
		v_entry[ 0 ] = new AppConfigurationEntry ( "littleware.security.SimpleDbLoginModule",
												   AppConfigurationEntry.LoginModuleControlFlag.REQUIRED,
												   new HashMap () );
		return v_entry;
	}
	
	/**
	 * Do-nothing refresh method
	 */
	public void refresh () {}
}

// littleware asset management system
// Copyright (C) 2007 Reuben Pasquini http://littleware.frickjack.com


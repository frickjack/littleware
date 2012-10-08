/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.message
package server
package internal

import com.google.inject
import javax.security.auth

/**
 * LoginStrategy implementation delegates to littleware client login - which
 * just sends a (name,password) pair to the littleware server (which
 * may actually be in process), and the littleware server delegates to
 * its own JAAS configuration for authentication - which may hit LDAP,
 * use internal name-password database, and support littleId validation depending on
 * its configuration.
 */
class JAASLoginStrategy @inject.Inject() ( 
     littlewareConfigProvider:inject.Provider[auth.login.Configuration] 
) extends LoginStrategy {
  lazy val loginConfig = littlewareConfigProvider.get()
  
  def acceptsCreds( creds:model.Credentials ):Boolean = {
    false
  }
  
  def login( creds:model.Credentials ):model.ClientSession = {
    val callbackHandler:auth.callback.CallbackHandler = new littleware.base.LoginCallbackHandler( "name", "password" )
    val ctx = new auth.login.LoginContext( "littleware.login", new auth.Subject(), 
                               callbackHandler,
                               loginConfig
         )
         
    throw new UnsupportedOperationException( "not yet implemented" )
  }

}

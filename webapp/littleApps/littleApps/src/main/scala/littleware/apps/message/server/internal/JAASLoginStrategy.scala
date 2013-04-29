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
import java.util.UUID
import java.util.logging.{Level,Logger}
import javax.security.auth
import littleware.bootstrap
import org.joda.{time => jtime}
import scala.collection.JavaConversions._


/**
 * LoginStrategy implementation delegates to
 * system default JAAS configuration for authentication.
 */
class JAASLoginStrategy ( 
     loginConfig:auth.login.Configuration
) extends LoginStrategy {
  private val log = Logger.getLogger( getClass.getName )
  //lazy val loginConfig = configBuilder.build()   // littlewareConfigProvider.get()
    
  def login( creds:model.Credentials ):model.ClientSession = {
    val callbackHandler:auth.callback.CallbackHandler = new littleware.base.login.LoginCallbackHandler( "name", "password" )
    val subject = new auth.Subject()
    val ctx = new auth.login.LoginContext( "littleware.login", subject, 
                               callbackHandler, loginConfig
         )
         
    ctx.login();
    val userName = subject.getPrincipals().head.getName
    val sessionId = java.util.UUID.randomUUID()
    val now = jtime.DateTime.now()
    model.ClientSession( userName, sessionId, now, now.plusDays(1) )
  }

  def lookup( sessionId:UUID ):bootstrap.SessionInjector = throw new UnsupportedOperationException( "not yet implemented" )  
}

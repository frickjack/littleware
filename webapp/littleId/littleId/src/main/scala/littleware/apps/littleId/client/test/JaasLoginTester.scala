/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */


package littleware.apps.littleId.client.test

import com.google.inject.Inject
import java.util.logging.Level
import javax.security.auth.Subject
import javax.security.auth.login.LoginContext
import javax.security.auth.login.LoginException
import junit.framework.Assert
import junit.framework.TestCase
import littleware.apps.littleId.client.controller.JaasLoginModule
import littleware.base.login.LoginCallbackHandler
import java.util.logging.{Level,Logger}


class JaasLoginTester @Inject()( loginConfig:JaasLoginModule.Config ) extends TestCase( "testJaas" ) {
  val log = Logger.getLogger( getClass.getName )

  def testJaas():Unit = try {
    (new LoginContext( "littleId", new Subject(),
                                          new LoginCallbackHandler( "email@bogus", "secret" ),
                                          loginConfig)
      ).login()
      Assert.fail( "Validation succeeded with bogus credentials" )
  } catch {
    case ex:LoginException => {
      log.log( Level.INFO, "Authentication failed as expected!")
    }
  }
}

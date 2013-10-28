/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId
package test

import com.google.inject
import java.util.logging.Level
import junit.framework.Assert._
import java.util.logging.{Level,Logger}


class OIdToolTester @inject.Inject()( 
  tool:server.controller.OpenIdTool,
  verifyTool:client.controller.VerifyTool,
  requestFactory:inject.Provider[server.model.AuthRequest.Builder],
  credsFactory:inject.Provider[common.model.OIdUserCreds.Builder]
) extends littleware.scala.test.LittleTest {
  setName(  "testOIdTool" )

  /**
   * Just test the buildRequest method
   */
  def testOIdTool():Unit = try {
    import server.model.AuthState._
    
    for( provider <- common.model.OIdProvider.values ) {
      val request = requestFactory.get.openIdProvider( provider ).replyToURL( new java.net.URL( "http://bla/bla" ) ).build() 
      val providerData = tool.startOpenIdAuth( request )
      assertTrue( "OpenId auth starts in Running state", providerData.request == request )
    }
    // verify that we can generate and validate credentials tokens
    val testCred = credsFactory.get.email( "reuben@frickjack.com" ).openId( new java.net.URL( "http://reuben.frickjack.com")).build
    val secret = tool.credsToToken( testCred )
    val optVerifiedCred = verifyTool.verify( secret )
    assertTrue( "Cred secret verified ok", optVerifiedCred.isDefined )
    assertTrue( "Verified cred matches generated cred: " + testCred + " ?= " + optVerifiedCred.get, 
               testCred == optVerifiedCred.get 
    )
  } catch basicHandler
  
}

/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.test

import com.google.inject.{Inject,Provider}
import java.net.URL
import java.util.logging.Level
import junit.framework.Assert
import junit.framework.TestCase
import littleware.apps.littleId
import littleware.apps.littleId.server.controller
import littleware.scala.LazyLogger

class VerifyToolTester @Inject() (
  verifyTool:controller.AuthVerifyTool,
  openIdBuilder:Provider[littleId.OIdUserCreds.Builder]
  ) extends TestCase( "testVerifyTool" ) {
  private val log = LazyLogger( getClass )

  def testVerifyTool():Unit = try {
    val creds1:littleId.OIdUserCreds = openIdBuilder.get.email( "frickjack@google.com" ).openId( new URL( "http://google.com/frickjack" )).build
    val creds2:littleId.OIdUserCreds = openIdBuilder.get.email( "frickjack@google.com" ).openId( new URL( "http://google.com/frickjack" )).build
    val creds3:littleId.OIdUserCreds = openIdBuilder.get.email( "bla@google.com" ).openId( new URL( "http://google.com/frickjack" )).build

    verifyTool.cacheCreds( "secret1", creds1 )
    verifyTool.cacheCreds( "secret3", creds3 )
    Assert.assertTrue( "Did not verify bogus secret", ! verifyTool.verifyCreds( "bogus", creds2 ) )
    Assert.assertTrue( "Verified equal creds with same secret ok", verifyTool.verifyCreds( "secret1", creds2 ))
    Assert.assertTrue( "2nd verification does not go through", ! verifyTool.verifyCreds( "secret1", creds2 ))
    Assert.assertTrue( "Did not verify unequals creds", ! verifyTool.verifyCreds( "secret3", creds2 ) )
    Assert.assertTrue( "Again, 2nd verification does not go through", ! verifyTool.verifyCreds( "secret3", creds3 ))
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Failed test", ex )
        Assert.fail( "Caught exception: " + ex )
    }
  }
}

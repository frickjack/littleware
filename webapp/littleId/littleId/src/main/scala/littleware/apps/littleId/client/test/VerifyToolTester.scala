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
import junit.framework.Assert
import junit.framework.TestCase
import littleware.apps.littleId.client.controller.VerifyTool
import littleware.scala.LazyLogger

class VerifyToolTester @Inject()( tool:VerifyTool ) extends TestCase( "testVerifyTool" ) {
  val log = LazyLogger( getClass )

  def testVerifyTool():Unit = try {
    Assert.assertTrue( "Verify should fail on bogus data",
                      ! tool.verify( "bogus secret", Map( "email" -> "bogus" ))
                      )
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Test failed", ex )
        Assert.fail( "Caught exception: " + ex )
    }
  }
}

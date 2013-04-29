/*
 * Copyright 2011 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.littleId.server.test

import com.google.inject.Inject
import java.util.logging.Level
import junit.framework.Assert
import junit.framework.TestCase
import littleware.apps.littleId.common.model.OIdProvider
import littleware.apps.littleId.server.controller
import java.util.logging.{Level,Logger}

class OIdToolTester @Inject()( tool:controller.OpenIdTool ) extends TestCase( "testOIdTool" ) {
  val log = Logger.getLogger( getClass.getName )

  /**
   * Just test the buildRequest method
   */
  def testOIdTool():Unit = try {
    for( provider <- OIdProvider.values ) {
      tool.buildRequest( provider )
    }
  } catch {
    case ex:Exception => {
        log.log( Level.WARNING, "Test failed", ex )
        Assert.fail( "Caught exception: " + ex )
      }
  }
}

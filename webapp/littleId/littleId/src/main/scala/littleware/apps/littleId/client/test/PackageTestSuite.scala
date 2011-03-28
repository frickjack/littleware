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
import junit.framework.TestSuite
import littleware.base.AssertionFailedException
import littleware.bootstrap.client.AppBootstrap
import littleware.scala.LazyLogger
import littleware.test.TestFactory


class PackageTestSuite @Inject()(
  verifyTester:VerifyToolTester,
  jaasTester:JaasLoginTester
) extends TestSuite {
  setName( getClass.getName )
  addTest( verifyTester )
  addTest( jaasTester )
}

object PackageTestSuite {
  val log = LazyLogger( getClass )
  def suite():TestSuite = try {
    (new TestFactory()).build(
      AppBootstrap.appProvider.get().build(),
      classOf[PackageTestSuite]
    )
  } catch {
    case ex => {
      log.log( Level.WARNING, "Failed to launch test suite", ex )
      throw new AssertionFailedException( "Failed to launch test", ex )
    }
  }
}


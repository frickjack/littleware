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

import com.google.inject.Inject
import java.util.logging.Level
import junit.framework.TestSuite
import littleware.base.AssertionFailedException
import littleware.bootstrap.client.AppBootstrap
import littleware.scala.LazyLogger
import littleware.test.TestFactory

class PackageTestSuite @Inject() (
  openIdToolTester:OIdToolTester
) extends TestSuite {
  setName( getClass.getName )
  addTest( openIdToolTester )
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


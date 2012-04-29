/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.message.test


import com.google.inject
import java.io
import java.util.logging.Level
import junit.framework.TestSuite
import littleware.base.AssertionFailedException
import littleware.bootstrap.AppBootstrap
import littleware.scala.LazyLogger
import littleware.test.TestFactory


class PackageTestSuite @inject.Inject()(
) extends TestSuite {
  setName( getClass.getName )


}

object PackageTestSuite {
  val log = LazyLogger( getClass )

  def suite():TestSuite = try {
    log.log( Level.INFO, "Launching test suite ..." )
    val suite = (new TestFactory()).build(
      AppBootstrap.appProvider.get().build(),
      classOf[PackageTestSuite]
    )
    log.log( Level.INFO, "Returning test suite to test runner ..." )
    suite
  } catch {
    case ex => {
        log.log( Level.WARNING, "Failed to launch test suite", ex )
        throw new AssertionFailedException( "Failed to launch test", ex )
      }
  }
  
}
/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.s3Copy
package test


import com.google.inject
import java.io
import java.util.logging.{Level,Logger}
import junit.framework.TestSuite
import littleware.asset.server.bootstrap.ServerBootstrap
import littleware.base.AssertionFailedException
import littleware.bootstrap.AppBootstrap
import littleware.test.TestFactory


class PackageTestSuite @inject.Inject()(
  pathToolTestFactory:inject.Provider[PathToolTester]
) extends TestSuite {
  setName( getClass.getName )

  Seq( 
    "testPathLs"
    ,"testPathParts"
    ,"testMimeTypes"
    ,"testPathCopy"
  ).foreach(
    (testName) => addTest( pathToolTestFactory.get.withName( testName ) )
  )
  
}

object PackageTestSuite {
  val log = Logger.getLogger( getClass.getName )

  def suite():TestSuite = try {
    log.log( Level.INFO, "Launching test suite in " + new java.io.File( "." ).getAbsolutePath + "..." )
    val suite = {
      val boot = AppBootstrap.appProvider.get().build()
      (new TestFactory()).build( boot, classOf[PackageTestSuite] )
    }
    log.log( Level.INFO, "Returning test suite to test runner ..." )
    suite
  } catch {
    case ex:Throwable => {
        log.log( Level.WARNING, "Failed to launch test suite", ex )
        throw new AssertionFailedException( "Failed to launch test", ex )
      }
  }
  
}
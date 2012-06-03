/*
 * Copyright 2012 http://code.google.com/p/littleware
 * 
 * The contents of this file are available subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.message
package test


import com.google.inject
import java.io
import java.util.logging.{Level,Logger}
import junit.framework.TestSuite
import littleware.base.AssertionFailedException
import littleware.bootstrap.AppBootstrap
import littleware.test.TestFactory


class PackageTestSuite @inject.Inject()(
  processTestFactory:inject.Provider[MessageProcessTester]
) extends TestSuite {
  setName( getClass.getName )

  addTest( processTestFactory.get.putName( "testMessageProcess" ) )
}

object PackageTestSuite {
  val log = Logger.getLogger( getClass.getName )

  def suite():TestSuite = try {
    log.log( Level.INFO, "Launching test suite ..." )
    val suite = (new TestFactory()).build(
      AppBootstrap.appProvider.get(
      ).addModuleFactory( new littleware.asset.webproxy.JettyModule.AppFactory 
      ).addModuleFactory( new JettyModuleFactory
      ).build(),
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
/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser.test

import com.google.inject.Inject
import junit.framework.TestSuite
import littleware.asset.client.bootstrap.ClientBootstrap
import littleware.asset.test.AssetTestFactory


object PackageTestSuite {
  class Suite @Inject() ( hoodTester:NeighborhoodTester ) extends TestSuite( PackageTestSuite.getClass.getName ) {
      this.addTest( hoodTester )
  }

  def suite:TestSuite = {
    val boot = ClientBootstrap.clientProvider.get.build.test
    (new AssetTestFactory).build( boot, classOf[Suite] )
  }

}

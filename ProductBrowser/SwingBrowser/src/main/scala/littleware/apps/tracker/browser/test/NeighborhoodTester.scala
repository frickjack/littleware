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
import java.util.logging.Level
import junit.framework.Assert
import littleware.apps.tracker.browser.controller.Controller
import littleware.asset.AssetSearchManager
import littleware.scala.LazyLogger
import littleware.security.LittleUser
import littleware.test.LittleTest

class NeighborhoodTester @Inject() ( controller:Controller, 
                                    user:LittleUser,
                                    search:AssetSearchManager
  ) extends LittleTest {
  setName( "testNeighborhood")

  val log = LazyLogger( getClass )

  /**
   * Simple neighborhood load test
   */
  def testNeighborhood():Unit = {
    try {
      val neighbor = controller.loadNeighborhood( user.getId )
      Assert.assertTrue( "neighbor references correct asset",
                        neighbor.asset.model.getAsset.getId == user.getId
        )
      Assert.assertTrue( "neighbor references correct home",
                        neighbor.neighbors.find( (info) => info.model.getAsset.getId == user.getHomeId ).isDefined
                )
    } catch {
      case ex => {
        log.log( Level.WARNING, "Test failed", ex )
        Assert.fail( "Caught exception: " + ex )
      }
    }
  }
}

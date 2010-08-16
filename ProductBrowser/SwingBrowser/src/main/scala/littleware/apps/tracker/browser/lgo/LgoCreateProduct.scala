/*
 * Copyright 2010 Reuben Pasquini All rights reserved.
 * 
 * The contents of this file are subject to the terms of the
 * Lesser GNU General Public License (LGPL) Version 2.1.
 * You may not use this file except in compliance with the
 * License. You can obtain a copy of the License at
 * http://www.gnu.org/licenses/lgpl-2.1.html.
 */

package littleware.apps.tracker.browser.lgo

import com.google.inject.Inject
import com.google.inject.Provider
import littleware.apps.tracker
import littleware.apps.tracker.browser.controller.Controller
import littleware.apps.tracker.browser.model.ProductData
import littleware.base.feedback.Feedback
import littleware.lgo.AbstractLgoBuilder
import littleware.lgo.AbstractLgoCommand
import scala.collection.JavaConversions._

object LgoCreateProduct {
  object Option {
    val parent = "parent"
    val name = "name"
    val comment = "comment"
  }
}

import LgoCreateProduct._

class LgoCreateProduct @Inject() (
  controller:Controller,
  dataProvider:Provider[ProductData.Builder]
  ) extends AbstractLgoBuilder[ProductData]( classOf[LgoCreateProduct].getName ) {
    class Command( data:ProductData ) extends AbstractLgoCommand[ProductData,tracker.Product]( classOf[LgoCreateProduct].getName, data ) {
        override def runCommand( feedback:Feedback ):tracker.Product = controller.createProduct( data )
    }

  /**
   * Build up command from command-line arguments
   */
  override def buildFromArgs( args:java.util.List[String] ):Command = {
    val argMap = AbstractLgoBuilder.processArgs( args, Option.parent, Option.name, Option.comment )
    buildSafe(
      dataProvider.get.name( argMap.get( Option.name )
      ).parentPath( argMap.get( Option.parent )
      ).comment( argMap.get( Option.comment )
      ).build
        )
  }
  override def buildSafe( data:ProductData ):Command = new Command( data )
}

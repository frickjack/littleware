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
import littleware.apps.lgo.AbstractAssetCommand
import littleware.apps.tracker
import littleware.apps.tracker.browser.controller.Controller
import littleware.apps.tracker.browser.model.ProductData
import littleware.asset.AssetPathFactory
import littleware.asset.pickle.HumanPicklerProvider
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



  class Builder @Inject() (
    controller:Controller,
    dataProvider:Provider[ProductData.Builder],
    pathFactory:AssetPathFactory,
    pickleProvider:HumanPicklerProvider
  ) extends AbstractLgoBuilder[ProductData]( classOf[LgoCreateProduct].getName ) {

    /**
     * Build up command from command-line arguments
     */
    override def buildFromArgs( args:java.util.List[String] ):LgoCreateProduct = {
      val argMap = AbstractLgoBuilder.processArgs( args, Option.parent, Option.name, Option.comment )
      buildSafe(
        dataProvider.get.name( argMap.get( Option.name )
        ).parentPath( pathFactory.createPath( argMap.get( Option.parent ) )
        ).comment( argMap.get( Option.comment )
        ).build
      )
    }
    override def buildSafe( data:ProductData ):LgoCreateProduct = new LgoCreateProduct( controller, pickleProvider, data )
  }
}

class LgoCreateProduct( controller:Controller, 
                       pickleProvider:HumanPicklerProvider,
                       data:ProductData
) extends AbstractAssetCommand[ProductData,tracker.Product]( classOf[LgoCreateProduct].getName, pickleProvider, data ) {
  override def runCommand( feedback:Feedback ):tracker.Product = controller.createProduct( data )
}
